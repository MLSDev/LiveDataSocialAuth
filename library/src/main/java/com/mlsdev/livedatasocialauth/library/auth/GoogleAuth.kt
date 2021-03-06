package com.mlsdev.livedatasocialauth.library.auth

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.auth.api.credentials.Credential
import com.mlsdev.livedatasocialauth.library.common.AuthResult
import com.mlsdev.livedatasocialauth.library.common.Status
import com.mlsdev.livedatasocialauth.library.fragment.AuthFragment
import com.mlsdev.livedatasocialauth.library.fragment.GoogleAuthFragment
import java.lang.ref.WeakReference

class GoogleAuth private constructor(
    authConditions: AuthConditions,
    activityReference: WeakReference<FragmentActivity>
) : SocialAuth(authConditions, activityReference) {

    override fun signIn(): LiveData<AuthResult> =
        Transformations.map(socialAuthFragment.signIn()) {
            removeAuthFragment()
            return@map it
        }

    override fun signOut(): LiveData<Status> =
        Transformations.map(socialAuthFragment.signOut()) {
            removeAuthFragment()
            return@map it
        }

    fun revokeAccess(): LiveData<Status> = (socialAuthFragment as GoogleAuthFragment).revokeAccess()

    override fun getAuthFragmentTag(): String = GoogleAuth::class.java.simpleName

    override fun getNewAuthFragmentInstance(): AuthFragment = GoogleAuthFragment.newInstance(authConditions)

    class Builder(activity: FragmentActivity) : AuthBuilder(activity) {

        fun requestEmail(): Builder {
            authConditions.permissions.add(AuthConditions.Key.REQUEST_EMAIL.value)
            return this
        }

        fun requestProfile(): Builder {
            authConditions.permissions.add(AuthConditions.Key.REQUEST_PROFILE.value)
            return this
        }

        fun clientId(clientId: String): Builder {
            authConditions.values[AuthConditions.Key.CLIENT_ID] = clientId
            return this
        }

        fun credential(credential: Credential): Builder {
            authConditions.googleAuthCredential = credential
            return this
        }

        fun enableSmartLock(): Builder {
            authConditions.smartLockEnabled = true
            return this
        }

        override fun build(): SocialAuth {
            return GoogleAuth(authConditions, activityReference)
        }

    }


}