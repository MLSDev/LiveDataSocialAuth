package com.mlsdev.livedatasocialauth.library.smartlock

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.auth.api.credentials.CredentialRequestResult
import com.google.android.gms.common.api.GoogleApiClient
import com.mlsdev.livedatasocialauth.library.common.Status

class SmartLockFragment : Fragment(), GoogleApiClient.ConnectionCallbacks {
    private lateinit var smartLockAction: SmartLockAction
    private lateinit var credentialRequest: CredentialRequest
    private var credential: Credential? = null
    private var disableAutoSignIn: Boolean = false
    private val credentialRequestResult = MutableLiveData<CredentialRequestResult>()
    private val status = MutableLiveData<Status>()
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

        
        TODO("implement")
    }

    private fun saveCredentialsOnConnected() {
        TODO("implement")
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

    fun saveCredentials(): LiveData<Status> {
        smartLockAction = SmartLockAction.SAVE
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

}