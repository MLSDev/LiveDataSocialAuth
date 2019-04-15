package com.mlsdev.livedatasocialauth.library.auth

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import com.mlsdev.livedatasocialauth.library.common.AuthResult
import com.mlsdev.livedatasocialauth.library.common.Status
import com.mlsdev.livedatasocialauth.library.fragment.AuthFragment
import java.lang.ref.WeakReference

abstract class SocialAuth(
        val authConditions: AuthConditions,
        private val activityReference: WeakReference<FragmentActivity>
) {

    protected val socialAuthFragment: AuthFragment = getAuthFragment()

    abstract fun signIn(): LiveData<AuthResult>

    abstract fun signOut(): LiveData<Status>

    abstract fun getAuthFragmentTag(): String

    protected abstract fun getNewAuthFragmentInstance(): AuthFragment

    protected fun removeAuthFragment() {
        val fragmentManager: FragmentManager? = activityReference.get()?.supportFragmentManager
        activityReference.get()?.supportFragmentManager?.findFragmentByTag(getAuthFragmentTag())?.let {
            fragmentManager?.beginTransaction()?.remove(it)?.commit()
        }
    }

    private fun getAuthFragment(): AuthFragment {
        val fragmentManager: FragmentManager? = activityReference.get()?.supportFragmentManager
        val authFragment: AuthFragment?
        val fragment = activityReference.get()?.supportFragmentManager?.findFragmentByTag(getAuthFragmentTag())

        authFragment = if (fragment == null) {
            getNewAuthFragmentInstance().apply {
                fragmentManager
                    ?.beginTransaction()
                    ?.add(this, getAuthFragmentTag())
                    ?.commitNowAllowingStateLoss()
                fragmentManager?.executePendingTransactions()
            }
        } else fragment as AuthFragment

        return authFragment
    }

}