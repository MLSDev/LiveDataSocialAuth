package com.mlsdev.livedatasocialauth.sample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.mlsdev.livedatasocialauth.library.auth.SocialAuthManager

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SocialAuthManager.getCurrentAccount().observe(this, Observer {
            if (it.authProvider != null)
                startActivity(Intent(this, LogOutActivity::class.java))
            else
                startActivity(Intent(this, MainActivity::class.java))

            finish()
        })
    }

}