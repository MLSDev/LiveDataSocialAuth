package com.mlsdev.livedatasocialauth.library.auth

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import com.mlsdev.livedatasocialauth.library.common.AuthResult
import com.mlsdev.livedatasocialauth.library.common.Status
import com.mlsdev.livedatasocialauth.library.fragment.AuthFragment
import com.mlsdev.livedatasocialauth.library.fragment.GoogleAuthFragment
import java.lang.ref.WeakReference

class GoogleAuth private constructor(
        authConditions: AuthConditions,
        activityReference: WeakReference<FragmentActivity>
) : SocialAuth(authConditions, activityReference) {

    override fun signIn(): LiveData<AuthResult> {
        return socialAuthFragment.signIn()
    }

    override fun signOut(): LiveData<Status> {
        return socialAuthFragment.signOut()
    }

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

        override fun build(): SocialAuth {
            return GoogleAuth(authConditions, activityReference)
        }

    }

}