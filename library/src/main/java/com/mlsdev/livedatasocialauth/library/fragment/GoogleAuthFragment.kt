package com.mlsdev.livedatasocialauth.library.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mlsdev.livedatasocialauth.library.auth.AuthConditions
import com.mlsdev.livedatasocialauth.library.common.AuthResult
import com.mlsdev.livedatasocialauth.library.common.Status

class GoogleAuthFragment : AuthFragment() {

    companion object {
        fun newInstance(authConditions: AuthConditions): AuthFragment {
            val fragment = GoogleAuthFragment()
            fragment.authConditions = authConditions
            return fragment
        }
    }

    override fun signIn(): LiveData<AuthResult> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun signOut(): LiveData<Status> {
        val stubResult = MutableLiveData<Status>()
        stubResult.postValue(Status(true, "Message", 1))
        return stubResult
    }

}