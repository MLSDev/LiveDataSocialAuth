package com.mlsdev.livedatasocialauth.library.auth

import com.google.android.gms.auth.api.credentials.Credential

class AuthConditions {
    val permissions = ArrayList<String>()
    val values = HashMap<Key, String>()
    var googleAuthCredential: Credential? = null

    enum class Key(val value: String) {
        REQUEST_EMAIL("email"),
        REQUEST_PROFILE("public_profile"),
        CLIENT_ID("client_id"),
        DISABLE_AUTO_SIGN_IN("disable_auto_sign_in"),
        ENABLE_SMART_LOCK("enable_smart_lock")
    }
}