package com.mlsdev.livedatasocialauth.library.auth

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.mlsdev.livedatasocialauth.library.common.AuthResult
import com.mlsdev.livedatasocialauth.library.common.Status
import com.mlsdev.livedatasocialauth.library.fragment.AuthFragment
import com.mlsdev.livedatasocialauth.library.fragment.FacebookAuthFragment
import java.lang.ref.WeakReference

class FacebookAuth private constructor(
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

    override fun getAuthFragmentTag(): String = FacebookAuth::class.java.simpleName

    override fun getNewAuthFragmentInstance(): AuthFragment = FacebookAuthFragment.newInstance(authConditions)

    class Builder(activity: FragmentActivity) : AuthBuilder(activity) {

        fun requestEmail(): Builder {
            authConditions.permissions.add(AuthConditions.Key.REQUEST_EMAIL.value)
            return this
        }

        fun requestProfile(): Builder {
            authConditions.permissions.add(AuthConditions.Key.REQUEST_PROFILE.value)
            return this
        }

        fun enableSmartLock(): Builder {
            authConditions.smartLockEnabled = true
            return this
        }

        override fun build(): SocialAuth {
            return FacebookAuth(authConditions, activityReference)
        }

    }

}