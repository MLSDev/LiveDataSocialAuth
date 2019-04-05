package com.mlsdev.livedatasocialauth.library.common

data class AuthResult(
    val account: Account?,
    val exception: Exception?,
    val isSuccess: Boolean
)