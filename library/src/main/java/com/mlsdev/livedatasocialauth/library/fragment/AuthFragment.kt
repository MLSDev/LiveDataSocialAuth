package com.mlsdev.livedatasocialauth.library.fragment

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mlsdev.livedatasocialauth.library.auth.AuthConditions
import com.mlsdev.livedatasocialauth.library.common.AuthResult
import com.mlsdev.livedatasocialauth.library.common.Status

abstract class AuthFragment : Fragment() {
    protected val signInLiveData = MutableLiveData<AuthResult>()
    protected lateinit var authConditions: AuthConditions

    abstract fun signIn(): LiveData<AuthResult>

    abstract fun signOut(): LiveData<Status>

}