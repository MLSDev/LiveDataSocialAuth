package com.mlsdev.livedatasocialauth.library.fragment

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.mlsdev.livedatasocialauth.library.R
import com.mlsdev.livedatasocialauth.library.auth.AuthConditions
import com.mlsdev.livedatasocialauth.library.auth.SocialAuthManager
import com.mlsdev.livedatasocialauth.library.common.Account
import com.mlsdev.livedatasocialauth.library.common.AuthProvider
import com.mlsdev.livedatasocialauth.library.common.AuthResult
import com.mlsdev.livedatasocialauth.library.common.Status
import org.json.JSONException

class FacebookAuthFragment : AuthFragment() {

    private val callbackManager = CallbackManager.Factory.create()

    private val callback = object : FacebookCallback<LoginResult> {
        override fun onSuccess(result: LoginResult?) {

            if (Profile.getCurrentProfile() == null) {
                object : ProfileTracker() {
                    override fun onCurrentProfileChanged(oldProfile: Profile?, currentProfile: Profile?) {
                        Profile.setCurrentProfile(currentProfile)
                        stopTracking()
                    }
                }
            }

            if (result != null) handleAuthResult(result)
            else signInLiveData.postValue(
                AuthResult(
                    null,
                    Exception(getString(R.string.error_get_facebook_account)),
                    false
                )
            )

        }

        override fun onCancel() {
            signInLiveData.postValue(AuthResult(null, null, false))
        }

        override fun onError(error: FacebookException?) {
            signInLiveData.postValue(AuthResult(null, error, false))
        }
    }

    companion object {
        fun newInstance(authConditions: AuthConditions): AuthFragment {
            val fragment = FacebookAuthFragment()
            fragment.authConditions = authConditions
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LoginManager.getInstance().registerCallback(callbackManager, callback)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun signIn(): LiveData<AuthResult> {
        LoginManager.getInstance().logInWithReadPermissions(this, authConditions.permissions)
        return signInLiveData
    }

    override fun signOut(): LiveData<Status> = MutableLiveData<Status>().apply {
        LoginManager.getInstance().logOut()
        LoginManager.getInstance().unregisterCallback(callbackManager)
        SocialAuthManager.removeCurrentAccount()
        this.postValue(Status(true, getString(R.string.message_signed_out), 0))
        onDetach()
    }

    private fun handleAuthResult(result: LoginResult) {
        val graphRequest = GraphRequest.newMeRequest(result.accessToken) { jsonObject, _ ->
            try {
                val profile: Profile? = Profile.getCurrentProfile()

                if (profile == null) {
                    signInLiveData.postValue(AuthResult(null, Exception("Can't sign in with Facebook"), false))
                    return@newMeRequest
                }

                val authAccount = Account().apply {
                    id = profile.id
                    email = jsonObject.getString(AuthConditions.Key.REQUEST_EMAIL.value)
                    firstName = profile.firstName
                    lastName = profile.lastName
                    displayName = profile.name
                    authProvider = AuthProvider.FACEBOOK
                }

                SocialAuthManager.saveAccount(authAccount)
                signInLiveData.postValue(AuthResult(authAccount, null, true))

            } catch (exception: JSONException) {
                exception.printStackTrace()
                signInLiveData.postValue(AuthResult(null, exception, false))
            }
        }

        graphRequest.parameters = Bundle().apply { putString("fields", "id,email,first_name,last_name,name") }
        graphRequest.executeAsync()
    }

}