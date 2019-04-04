package com.mlsdev.livedatasocialauth.library.util

import com.mlsdev.livedatasocialauth.library.common.Account
import com.mlsdev.livedatasocialauth.library.common.AuthProvider
import org.json.JSONException
import org.json.JSONObject

object JsonParser {

    private const val ID_KEY = "id"
    private const val EMAIL_KEY = "email"
    private const val DISPLAY_NAME_KEY = "display_name"
    private const val FIRST_NAME_KEY = "first_name"
    private const val LAST_NAME_KEY = "last_name"
    private const val AUTH_PROVIDER_KEY = "auth_provider"
    const val AUTH_ACCOUNT_KEY = "auth_account"

    fun accountToJson(account: Account): String {
        val accountJsonObject = JSONObject()
        accountJsonObject.put(ID_KEY, account.id)
        accountJsonObject.put(EMAIL_KEY, account.email)
        accountJsonObject.put(DISPLAY_NAME_KEY, account.displayName)
        accountJsonObject.put(FIRST_NAME_KEY, account.firstName)
        accountJsonObject.put(LAST_NAME_KEY, account.lastName)
        accountJsonObject.put(AUTH_PROVIDER_KEY, account.authProvider?.value)
        return accountJsonObject.toString()
    }

    fun parseAccountJson(json: String): Account? {
        try {
            val account = Account()
            val accountJsonObject = JSONObject(json)
            account.id = accountJsonObject.getString(ID_KEY)
            account.email = accountJsonObject.getString(EMAIL_KEY)
            account.displayName = accountJsonObject.getString(DISPLAY_NAME_KEY)
            account.firstName = accountJsonObject.getString(FIRST_NAME_KEY)
            account.lastName = accountJsonObject.getString(LAST_NAME_KEY)
            account.authProvider = accountJsonObject.getString(AUTH_PROVIDER_KEY)?.let {
                AuthProvider.valueOf(it.toUpperCase())
            }
            return account
        } catch (jsonException: JSONException) {
            jsonException.printStackTrace()
        }
        return null
    }

}