package com.mlsdev.livedatasocialauth.library.auth

class AuthConditions {
    val permissions = ArrayList<String>()
    val values = HashMap<Key, String>()

    enum class Key(val value: String) {
        REQUEST_EMAIL("email"),
        REQUEST_PROFILE("public_profile"),
        CLIENT_ID("client_id"),
        DISABLE_AUTO_SIGN_IN("disable_auto_sign_in")
    }
}