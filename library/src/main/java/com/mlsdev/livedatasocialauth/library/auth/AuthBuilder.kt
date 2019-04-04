package com.mlsdev.livedatasocialauth.library.auth

import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

abstract class AuthBuilder(activity: FragmentActivity) {
    protected val authConditions = AuthConditions()
    protected val activityReference = WeakReference<FragmentActivity>(activity)

    abstract fun build(): SocialAuth

}