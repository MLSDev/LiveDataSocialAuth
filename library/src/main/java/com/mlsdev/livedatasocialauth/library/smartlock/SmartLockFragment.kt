package com.mlsdev.livedatasocialauth.library.smartlock

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.auth.api.credentials.CredentialRequestResult
import com.google.android.gms.auth.api.credentials.IdentityProviders.FACEBOOK
import com.google.android.gms.auth.api.credentials.IdentityProviders.GOOGLE
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.GoogleApiClient
import com.mlsdev.livedatasocialauth.library.R
import com.mlsdev.livedatasocialauth.library.auth.FacebookAuth
import com.mlsdev.livedatasocialauth.library.auth.GoogleAuth
import com.mlsdev.livedatasocialauth.library.auth.SocialAuthManager
import com.mlsdev.livedatasocialauth.library.common.Account
import com.mlsdev.livedatasocialauth.library.common.AuthResult
import com.mlsdev.livedatasocialauth.library.common.Status
import com.mlsdev.livedatasocialauth.library.util.JsonParser

class SmartLockFragment : Fragment(), GoogleApiClient.ConnectionCallbacks {
    private lateinit var smartLockAction: SmartLockAction
    private lateinit var credentialRequest: CredentialRequest
    private var credential: Credential? = null
    private var smartLockOptions: SmartLockOptions? = null
    private var disableAutoSignIn: Boolean = false
    private val credentialRequestResult = MutableLiveData<CredentialRequestResult>()
    private val status = MutableLiveData<Status>()
    private val account = MutableLiveData<AuthResult>()
    private var currentAccount: Account? = null

    private val credentialsApiClient: GoogleApiClient by lazy {
        GoogleApiClient.Builder(context!!)
            .addConnectionCallbacks(this)
            .addApi(Auth.CREDENTIALS_API)
            .build()
    }

    companion object {
        fun newInstance(smartLockBuilder: SmartLock.Builder): SmartLockFragment {
            val fragment = SmartLockFragment()
            fragment.credentialRequest = smartLockBuilder.credentialsRequestBuilder.build()
            fragment.disableAutoSignIn = smartLockBuilder.disableAutoSignIn
            return fragment
        }

        const val REQUEST_CODE_SAVE = 11
        const val REQUEST_CODE_SAVE_INTERNAL = 22
        const val REQUEST_CODE_READ = 33
    }

    override fun onConnected(data: Bundle?) {
        when (smartLockAction) {
            SmartLockAction.REQUEST -> requestCredentialsOnConnected()
            SmartLockAction.REQUEST_AND_AUTO_SIGN_IN -> requestCredentialsAndSignInOnConnected()
            SmartLockAction.SAVE -> saveCredentialsOnConnected()
            SmartLockAction.DELETE -> deleteCredentialsOnConnected()
            SmartLockAction.DISABLE_AUTO_SIGN_IN -> disableAuthSignInOnConnected()
        }
    }

    private fun requestCredentialsOnConnected() {
        if (disableAutoSignIn)
            Auth.CredentialsApi.disableAutoSignIn(credentialsApiClient)

        Auth.CredentialsApi.request(credentialsApiClient, credentialRequest).setResultCallback {
            credentialRequestResult.postValue(it)
        }
    }

    private fun requestCredentialsAndSignInOnConnected() {
        if (disableAutoSignIn)
            Auth.CredentialsApi.disableAutoSignIn(credentialsApiClient)


        Auth.CredentialsApi.request(credentialsApiClient, credentialRequest).setResultCallback {
            if (it.status.hasResolution()) {
                resolveRequestResult(it)
                return@setResultCallback
            }

            val accountType = it.credential.accountType
            when {
                it.status.isSuccess && accountType == GOOGLE -> handleRequestedGoogleAccountCredential(it.credential)
                it.status.isSuccess && accountType == FACEBOOK -> handleRequestedFacebookAccountCredential(it.credential)
                else -> resolveRequestResult(it)
            }
        }
    }

    private fun resolveRequestResult(result: CredentialRequestResult) {
        when (result.status.statusCode) {
            CommonStatusCodes.RESOLUTION_REQUIRED -> {
                try {
                    startIntentSenderForResult(
                        result.status.resolution.intentSender,
                        REQUEST_CODE_READ,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                } catch (exception: IntentSender.SendIntentException) {
                    account.postValue(AuthResult(null, exception, false))
                }
            }
            else -> {
                credentialsApiClient.disconnect()
                account.postValue(
                    AuthResult(
                        null,
                        Exception(result.status.statusMessage),
                        false
                    )
                )
            }
        }
    }

    private fun handleRequestedGoogleAccountCredential(credential: Credential) {
        val googleAuthBuilder = GoogleAuth.Builder(activity!!)
        val options: SmartLockOptions? =
            SocialAuthManager.sharedPreferences?.getString(JsonParser.SMART_LOCK_OPTIONS_KEY, null)?.let { json ->
                JsonParser.parseSmartLockOptions(json)?.let { options ->
                    if (options.requestEmail) googleAuthBuilder.requestEmail()
                    if (options.requestProfile) googleAuthBuilder.requestProfile()
                    options.clientId?.let { googleAuthBuilder.clientId(it) }
                    options
                }
            }

        if (options == null) {
            googleAuthBuilder.requestProfile().requestEmail()
        }

        googleAuthBuilder.credential(credential)

        googleAuthBuilder.build().signIn().observe(this, Observer {
            if (it.isSuccess && it.account != null) {
                currentAccount = it.account

                val updateCredential = Credential.Builder(it.account.email)
                    .setAccountType(GOOGLE)
                    .setName(it.account.displayName)
                    .setProfilePictureUri(it.account.avatar)
                    .build()
                saveCredentialInternal(
                    updateCredential,
                    options ?: SmartLockOptions(
                        requestEmail = true,
                        requestProfile = true
                    )
                )
            }
        })
    }

    private fun handleRequestedFacebookAccountCredential(credential: Credential) {
        val facebookAuthBuilder = FacebookAuth.Builder(activity!!)
        val options: SmartLockOptions? =
            SocialAuthManager.sharedPreferences?.getString(JsonParser.SMART_LOCK_OPTIONS_KEY, null)?.let { json ->
                JsonParser.parseSmartLockOptions(json)?.let { options ->
                    if (options.requestEmail) facebookAuthBuilder.requestEmail()
                    if (options.requestProfile) facebookAuthBuilder.requestProfile()

                    options
                }
            }


        facebookAuthBuilder.build().signIn().observe(this, Observer {
            if (it.isSuccess && it.account != null) {
                val updateCredential = Credential.Builder(it.account.email)
                    .setAccountType(FACEBOOK)
                    .setName(it.account.displayName)
                    .setProfilePictureUri(it.account.avatar)
                    .build()
                saveCredentialInternal(
                    updateCredential,
                    options ?: SmartLockOptions(
                        requestEmail = true,
                        requestProfile = true
                    )
                )
            }
        })
    }

    private fun saveCredentialsOnConnected() {
        Auth.CredentialsApi.save(credentialsApiClient, credential).setResultCallback { saveStatus ->
            when {
                saveStatus.isSuccess -> {
                    credentialsApiClient.disconnect()
                    smartLockOptions?.let {
                        SocialAuthManager.sharedPreferences?.edit()
                            ?.putString(JsonParser.SMART_LOCK_OPTIONS_KEY, JsonParser.smartLockOptionsToJson(it))
                            ?.apply()
                    }
                    status.postValue(
                        Status(
                            true,
                            getString(R.string.message_user_credential_saved),
                            CommonStatusCodes.SUCCESS
                        )
                    )
                }
                saveStatus.hasResolution() -> {
                    try {
                        saveStatus.startResolutionForResult(activity!!, REQUEST_CODE_SAVE)
                    } catch (exception: IntentSender.SendIntentException) {
                        status.postValue(Status(false, exception.message ?: "", CommonStatusCodes.ERROR))
                    }
                }
                else -> {
                    credentialsApiClient.disconnect()
                }
            }
        }
    }

    private fun saveCredentialInternal(credential: Credential, options: SmartLockOptions) {
        Auth.CredentialsApi.save(credentialsApiClient, credential).setResultCallback { saveStatus ->
            when {
                saveStatus.isSuccess -> {
                    SocialAuthManager.sharedPreferences?.edit()
                        ?.putString(JsonParser.SMART_LOCK_OPTIONS_KEY, JsonParser.smartLockOptionsToJson(options))
                        ?.apply()

                    credentialsApiClient.disconnect()
                    account.postValue(AuthResult(currentAccount, null, true))
                    currentAccount = null
                }
                saveStatus.hasResolution() -> {
                    try {
                        saveStatus.startResolutionForResult(activity!!, REQUEST_CODE_SAVE_INTERNAL)
                    } catch (exception: IntentSender.SendIntentException) {
                        currentAccount = null
                        account.postValue(AuthResult(null, exception, false))
                    }
                }
                else -> credentialsApiClient.disconnect()
            }
        }
    }

    private fun deleteCredentialsOnConnected() {
        Auth.CredentialsApi.delete(credentialsApiClient, credential).setResultCallback {
            credentialsApiClient.disconnect()
            status.postValue(Status(it.isSuccess, it.statusMessage ?: "", it.statusCode))
        }
    }

    private fun disableAuthSignInOnConnected() {
        Auth.CredentialsApi.disableAutoSignIn(credentialsApiClient).setResultCallback {
            credentialsApiClient.disconnect()
            status.postValue(Status(it.isSuccess, it.statusMessage ?: "", it.statusCode))
        }
    }

    override fun onConnectionSuspended(code: Int) {
    }

    fun requestCredentials(): LiveData<CredentialRequestResult> {
        smartLockAction = SmartLockAction.REQUEST
        credentialsApiClient.connect()
        return credentialRequestResult
    }

    fun requestCredentialsAndSignIn(): LiveData<AuthResult> {
        smartLockAction = SmartLockAction.REQUEST_AND_AUTO_SIGN_IN
        credentialsApiClient.connect()
        return account
    }

    fun saveCredentials(credential: Credential, smartLockOptions: SmartLockOptions): LiveData<Status> {
        smartLockAction = SmartLockAction.SAVE
        this.credential = credential
        this.smartLockOptions = smartLockOptions
        credentialsApiClient.connect()
        return status
    }

    fun deleteCredentials(credential: Credential): LiveData<Status> {
        this.credential = credential
        smartLockAction = SmartLockAction.DELETE
        credentialsApiClient.connect()
        return status
    }

    fun disableAuthSignIn(): LiveData<Status> {
        smartLockAction = SmartLockAction.DISABLE_AUTO_SIGN_IN
        credentialsApiClient.connect()
        return status
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val isSuccess = resultCode == Activity.RESULT_OK

        when (requestCode) {
            REQUEST_CODE_READ -> {
                if (isSuccess && data != null) {
                    val credential = data.getParcelableExtra<Credential>(Credential.EXTRA_KEY)
                    when (credential.accountType) {
                        GOOGLE -> handleRequestedGoogleAccountCredential(credential)
                        FACEBOOK -> handleRequestedFacebookAccountCredential(credential)
                        else -> account.postValue(
                            AuthResult(
                                null,
                                Exception(getString(R.string.error_account_type_not_set)),
                                false
                            )
                        )
                    }
                } else {
                    credentialsApiClient.disconnect()
                    account.postValue(AuthResult(null, Exception(getString(R.string.error_sign_in_manually)), false))
                }
            }
            REQUEST_CODE_SAVE -> {
                status.postValue(
                    Status(
                        isSuccess,
                        getString(if (isSuccess) R.string.message_user_credential_saved else R.string.error_save_credential),
                        if (isSuccess) CommonStatusCodes.SUCCESS else CommonStatusCodes.ERROR
                    )
                )
                credentialsApiClient.disconnect()
            }
            REQUEST_CODE_SAVE_INTERNAL -> {
                credentialsApiClient.disconnect()
                account.postValue(AuthResult(currentAccount, null, isSuccess))
                currentAccount = null
            }
        }
    }

}