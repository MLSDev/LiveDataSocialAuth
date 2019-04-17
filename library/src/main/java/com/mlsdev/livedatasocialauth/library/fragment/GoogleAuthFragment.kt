package com.mlsdev.livedatasocialauth.library.fragment

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.IdentityProviders
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.mlsdev.livedatasocialauth.library.auth.AuthConditions
import com.mlsdev.livedatasocialauth.library.auth.AuthConditions.Key.*
import com.mlsdev.livedatasocialauth.library.auth.SocialAuthManager
import com.mlsdev.livedatasocialauth.library.common.Account
import com.mlsdev.livedatasocialauth.library.common.AuthProvider
import com.mlsdev.livedatasocialauth.library.common.AuthResult
import com.mlsdev.livedatasocialauth.library.common.Status
import com.mlsdev.livedatasocialauth.library.fragment.GoogleAuthFragment.AuthAction.*
import com.mlsdev.livedatasocialauth.library.smartlock.SmartLockOptions

class GoogleAuthFragment : AuthFragment(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    lateinit var authAction: AuthAction
    private val googleApiClient: GoogleApiClient by lazy { buildGoogleApiClient(buildGoogleSignInOptions()) }
    private val statusLiveData = MutableLiveData<Status>()

    companion object {
        const val REQUEST_CODE_SIGN_IN = 1

        fun newInstance(authConditions: AuthConditions): AuthFragment {
            val fragment = GoogleAuthFragment()
            fragment.authConditions = authConditions
            return fragment
        }
    }

    override fun signIn(): LiveData<AuthResult> {
        authAction = if (authConditions.googleAuthCredential == null) SIGN_IN else SILENT_SIGN_IN
        googleApiClient.connect()
        return signInLiveData
    }

    override fun signOut(): LiveData<Status> {
        authAction = SIGN_OUT
        googleApiClient.connect()
        return statusLiveData
    }

    fun revokeAccess(): LiveData<Status> {
        authAction = REVOKE_ACCESS
        googleApiClient.connect()
        return statusLiveData
    }

    private fun buildGoogleSignInOptions(): GoogleSignInOptions =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).apply {
            if (authConditions.permissions.contains(REQUEST_EMAIL.value))
                requestEmail()

            if (authConditions.permissions.contains(REQUEST_PROFILE.value))
                requestProfile()

            if (authConditions.values[CLIENT_ID] != null)
                requestIdToken(authConditions.values[CLIENT_ID])

            authConditions.googleAuthCredential?.let { setAccountName(it.id) }
        }.build()

    private fun buildGoogleApiClient(options: GoogleSignInOptions): GoogleApiClient = GoogleApiClient.Builder(context!!)
        .addConnectionCallbacks(this)
        .addApi(Auth.GOOGLE_SIGN_IN_API, options)
        .build()

    override fun onConnected(data: Bundle?) {
        when (authAction) {
            SIGN_IN -> signInAfterOnConnected()
            SILENT_SIGN_IN -> silentSignInAfterOnConnected()
            SIGN_OUT -> signOutOnConnected()
            REVOKE_ACCESS -> revokeAccessAfterOnConnected()
        }
    }

    private fun signInAfterOnConnected() {
        if (authConditions.permissions.contains(DISABLE_AUTO_SIGN_IN.value))
            Auth.GoogleSignInApi.signOut(googleApiClient)

        startActivityForResult(Auth.GoogleSignInApi.getSignInIntent(googleApiClient), REQUEST_CODE_SIGN_IN)
    }

    private fun silentSignInAfterOnConnected() {
        val pendingResult = Auth.GoogleSignInApi.silentSignIn(googleApiClient)

        if (pendingResult.isDone) handleSignInResult(pendingResult.get())
        else pendingResult.setResultCallback { handleSignInResult(it) }
    }

    private val signOutCallback = ResultCallback<com.google.android.gms.common.api.Status> { status ->
        SocialAuthManager.removeCurrentAccount()
        googleApiClient.disconnect()
        statusLiveData.postValue(Status(status.isSuccess, status.statusMessage.toString(), status.statusCode))
    }

    private fun signOutOnConnected() {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(signOutCallback)
    }

    private fun revokeAccessAfterOnConnected() {
        Auth.GoogleSignInApi.revokeAccess(googleApiClient).setResultCallback(signOutCallback)
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        SocialAuthManager.removeCurrentAccount()
        googleApiClient.disconnect()
        statusLiveData.postValue(Status(false, result.errorMessage.toString(), result.errorCode))
    }

    override fun onConnectionSuspended(code: Int) {
    }

    private fun handleSignInResult(result: GoogleSignInResult) {
        if (result.isSuccess) {
            val account = Account().apply {
                id = result.signInAccount?.id
                displayName = result.signInAccount?.displayName
                firstName = result.signInAccount?.givenName
                lastName = result.signInAccount?.familyName
                email = result.signInAccount?.email
                authProvider = AuthProvider.GOOGLE
            }

            SocialAuthManager.saveAccount(account)
            saveCredentialAndPostAuthResult(account, IdentityProviders.GOOGLE)

        } else {
            if (authConditions.googleAuthCredential != null) {
                Auth.CredentialsApi.delete(googleApiClient, authConditions.googleAuthCredential).setResultCallback {
                    googleApiClient.disconnect()
                    signInLiveData.postValue(AuthResult(null, Exception(result.toString()), false))
                }
            } else {
                googleApiClient.disconnect()
                signInLiveData.postValue(AuthResult(null, Exception(result.toString()), false))
            }
        }
    }

    override fun buildSmartLockCredentialOptions(): SmartLockOptions = SmartLockOptions(
        authConditions.permissions.contains(REQUEST_EMAIL.value),
        authConditions.permissions.contains(REQUEST_PROFILE.value)
    )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SIGN_IN)
            handleSignInResult(Auth.GoogleSignInApi.getSignInResultFromIntent(data))
    }

    enum class AuthAction {
        SIGN_IN,
        SILENT_SIGN_IN,
        SIGN_OUT,
        REVOKE_ACCESS
    }
}