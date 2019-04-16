package com.mlsdev.livedatasocialauth.library.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mlsdev.livedatasocialauth.library.App
import com.mlsdev.livedatasocialauth.library.R
import com.mlsdev.livedatasocialauth.library.common.Account
import com.mlsdev.livedatasocialauth.library.common.AuthProvider
import com.mlsdev.livedatasocialauth.library.common.Status
import com.mlsdev.livedatasocialauth.library.smartlock.SmartLock
import com.mlsdev.livedatasocialauth.library.util.JsonParser
import com.mlsdev.livedatasocialauth.library.util.JsonParser.AUTH_ACCOUNT_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

object SocialAuthManager {
    private val context = App.contextReference
    private val currentAccountLiveData = MutableLiveData<Account>()
    val sharedPreferences: SharedPreferences?

    init {
        sharedPreferences = context.get()
            ?.getSharedPreferences(context.get()?.getString(R.string.social_auth_prefs), Context.MODE_PRIVATE)
    }

    fun signOut(activity: FragmentActivity): LiveData<Status> {
        val facebookAuth = FacebookAuth.Builder(activity).build()
        val googleAuth = GoogleAuth.Builder(activity).build()
        val smartLock = SmartLock.Builder(WeakReference(activity)).build()
        val status = MediatorLiveData<Status>()


        return status.apply {
            GlobalScope.launch(Dispatchers.Main) {
                when (getCurrentAccountSync()?.authProvider) {
                    AuthProvider.FACEBOOK -> status.addSource(Transformations.switchMap(facebookAuth.signOut()) {
                        smartLock.disableAutoSignIn()
                    }) { status.postValue(it) }
                    AuthProvider.GOOGLE -> status.addSource(Transformations.switchMap(googleAuth.signOut()) {
                        smartLock.disableAutoSignIn()
                    }) { status.postValue(it) }
                    else -> status.addSource(smartLock.disableAutoSignIn()) {
                        removeCurrentAccount()
                        status.postValue(it)
                    }
                }
            }
        }
    }

    fun getCurrentAccountSync(): Account? =
        sharedPreferences?.getString(AUTH_ACCOUNT_KEY, null)?.let { JsonParser.parseAccountJson(it) }

    fun getCurrentAccount(): LiveData<Account> {
        GlobalScope.launch(Dispatchers.Main) {
            currentAccountLiveData.postValue(getCurrentAccountSync() ?: Account())
        }

        return currentAccountLiveData
    }

    fun saveAccount(account: Account) {
        GlobalScope.launch(Dispatchers.Main) {
            sharedPreferences
                ?.edit()
                ?.putString(AUTH_ACCOUNT_KEY, JsonParser.accountToJson(account))
                ?.apply()
        }
    }

    fun removeCurrentAccount() {
        GlobalScope.launch(Dispatchers.Main) {
            sharedPreferences
                ?.edit()
                ?.remove(AUTH_ACCOUNT_KEY)
                ?.apply()
        }
    }
}