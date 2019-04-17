package com.mlsdev.livedatasocialauth.library.fragment

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.credentials.Credential
import com.mlsdev.livedatasocialauth.library.auth.AuthConditions
import com.mlsdev.livedatasocialauth.library.common.Account
import com.mlsdev.livedatasocialauth.library.common.AuthResult
import com.mlsdev.livedatasocialauth.library.common.Status
import com.mlsdev.livedatasocialauth.library.smartlock.SmartLock
import com.mlsdev.livedatasocialauth.library.smartlock.SmartLockOptions
import java.lang.ref.WeakReference

abstract class AuthFragment : Fragment() {
    protected val signInLiveData = MutableLiveData<AuthResult>()
    protected lateinit var authConditions: AuthConditions

    /**
     * Sings in a user with Google or Facebook
     * @return a [LiveData] with [AuthResult]
     * */
    abstract fun signIn(): LiveData<AuthResult>

    /**
     * Signs out a user from all social providers
     * @return a [LiveData] with [Status]
     * */
    abstract fun signOut(): LiveData<Status>

    /**
     * Builds the [SmartLockOptions] for the user's [Credential] saving
     * @return [SmartLockOptions]
     * */
    protected abstract fun buildSmartLockCredentialOptions(): SmartLockOptions

    /**
     * Saves the user's [Credential] and posts the [AuthResult] to [signInLiveData]
     * @param account the [Account] which is created after success social authorization
     * */
    protected fun saveCredentialAndPostAuthResult(account: Account, provider: String) {
        if (!authConditions.smartLockEnabled) {
            signInLiveData.postValue(AuthResult(account, null, true))
            return
        }

        val credential = Credential.Builder(account.email)
            .setAccountType(provider)
            .setName(account.displayName)
            .setProfilePictureUri(account.avatar)
            .build()

        SmartLock.Builder(WeakReference(activity!!))
            .disableAutoSignIn()
            .setAccountTypes(provider)
            .build()
            .saveCredential(credential, buildSmartLockCredentialOptions())
            .observe(this, Observer {
                signInLiveData.apply {
                    this.postValue(AuthResult(account, null, true))
                }
            })
    }


}