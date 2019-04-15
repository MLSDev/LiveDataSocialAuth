package com.mlsdev.livedatasocialauth.library.smartlock

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.auth.api.credentials.CredentialRequestResult
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.GoogleApiClient
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

        const val REQUEST_CODE_SAVE = 1
        const val REQUEST_CODE_SAVE_INTERNAL = 2
        const val REQUEST_CODE_READ = 3
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
            credentialRequestResult.postValue(it)
        }
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
                    status.postValue(Status(true, "User credentials have been saved", CommonStatusCodes.SUCCESS))
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

    fun requestCredentialsAndSignIn() {
        smartLockAction = SmartLockAction.REQUEST_AND_AUTO_SIGN_IN
        credentialsApiClient.connect()
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

            }
            REQUEST_CODE_SAVE -> {
                status.postValue(
                    Status(
                        isSuccess,
                        if (isSuccess) "User credentials have been saved" else "Error",
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