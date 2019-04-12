package com.mlsdev.livedatasocialauth.library.smartlock

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.auth.api.credentials.IdentityProviders
import java.lang.ref.WeakReference

class SmartLock private constructor(private val builder: Builder) {

    companion object {
        const val TAG = "smart.lock.fragment"
    }

    class Builder(val activityReference: WeakReference<FragmentActivity>) {
        var disableAutoSignIn: Boolean = false
        val credentialsRequestBuilder = CredentialRequest.Builder().apply { setAccountTypes(IdentityProviders.GOOGLE) }

        fun setPasswordLoginSupported(supported: Boolean): Builder {
            credentialsRequestBuilder.setPasswordLoginSupported(true)
            return this
        }

        fun setAccountTypes(vararg types: String): Builder {
            credentialsRequestBuilder.setAccountTypes(*types)
            return this
        }

        fun disableAutoSignIne(): Builder {
            disableAutoSignIn = true
            return this
        }

        fun build(): SmartLock = SmartLock(this)

    }

    private fun getSmartLockFragment(): SmartLockFragment {
        val fragmentManager: FragmentManager? = builder.activityReference.get()?.supportFragmentManager
        val smartLockFragment: SmartLockFragment?
        val fragment = builder.activityReference.get()?.supportFragmentManager?.findFragmentByTag(TAG)

        smartLockFragment = if (fragment == null) {
            SmartLockFragment.newInstance(builder).apply {
                fragmentManager
                    ?.beginTransaction()
                    ?.add(this, TAG)
                    ?.commit()
                fragmentManager?.executePendingTransactions()
            }
        } else fragment as SmartLockFragment

        return smartLockFragment
    }
}