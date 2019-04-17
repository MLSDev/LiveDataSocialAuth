package com.mlsdev.livedatasocialauth.library.common

import android.net.Uri

class Account {
    var id: String? = null
    var displayName: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var email: String? = null
    var authProvider: AuthProvider? = null
    var avatar: Uri? = null
}

enum class AuthProvider(val value: String) {
    FACEBOOK("Facebook"),
    GOOGLE("Google")
}