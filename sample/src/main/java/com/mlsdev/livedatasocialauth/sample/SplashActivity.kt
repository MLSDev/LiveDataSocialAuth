package com.mlsdev.livedatasocialauth.sample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mlsdev.livedatasocialauth.library.auth.SocialAuthManager
import com.mlsdev.livedatasocialauth.library.common.Account

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val socialAuthManager = SocialAuthManager(this)
        val account: Account? = socialAuthManager.getCurrentAccountSync()

        if (account != null) {
            if (account.authProvider != null)
                startActivity(Intent(this, LogOutActivity::class.java))
            else
                startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }

        finish()
    }

}