package com.mlsdev.livedatasocialauth.library.smartlock

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.api.GoogleApiClient

class SmartLockFragment : Fragment(), GoogleApiClient.ConnectionCallbacks {
    private lateinit var smartLockAction: SmartLockAction
    private val credentialsApiClient: GoogleApiClient by lazy {
        GoogleApiClient.Builder(context!!)
            .addConnectionCallbacks(this)
            .addApi(Auth.CREDENTIALS_API)
            .build()
    }

    override fun onConnected(data: Bundle?) {
        when (smartLockAction) {
            SmartLockAction.REQUEST -> requestCredentials()
            SmartLockAction.REQUEST_AND_AUTO_SIGN_IN -> requestCredentialsAndSignIn()
            SmartLockAction.SAVE -> saveCredentials()
            SmartLockAction.DELETE -> deleteCredentials()
            SmartLockAction.DISABLE_AUTO_SIGN_IN -> disableAuthSignIn()
        }
    }

    private fun requestCredentials() {

    }

    private fun requestCredentialsAndSignIn() {

    }

    private fun saveCredentials() {

    }

    private fun deleteCredentials() {

    }

    private fun disableAuthSignIn() {

    }

    override fun onConnectionSuspended(code: Int) {
    }

}