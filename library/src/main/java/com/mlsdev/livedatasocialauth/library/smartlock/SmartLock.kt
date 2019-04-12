package com.mlsdev.livedatasocialauth.library.smartlock

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.auth.api.credentials.IdentityProviders
import com.mlsdev.livedatasocialauth.library.common.Status
import java.lang.ref.WeakReference

class SmartLock private constructor(private val builder: Builder) {

    private val fragment: SmartLockFragment by lazy { getSmartLockFragment() }

    companion object {
        const val TAG = "smart.lock.fragment"
    }

    fun saveCredential(credential: Credential, smartLockOptions: SmartLockOptions): LiveData<Status> =
        fragment.saveCredentials(credential, smartLockOptions)

    class Builder(val activityReference: WeakReference<FragmentActivity>) {
        var disableAutoSignIn: Boolean = false
        val credentialsRequestBuilder = CredentialRequest.Builder().apply { setAccountTypes(IdentityProviders.GOOGLE) }

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