package com.mlsdev.livedatasocialauth.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.mlsdev.livedatasocialauth.library.auth.SocialAuthManager

class LogOutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_out)

        findViewById<Button>(R.id.button_log_out).setOnClickListener {
            SocialAuthManager.signOut(this).observe(this, Observer {
                if (it.success) finish()
                else Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            })
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()
        SocialAuthManager.getCurrentAccount().observe(this, Observer {
            it.authProvider?.let { provider ->
                findViewById<TextView>(R.id.text_auth_provider).text = "Signed in with ${provider.value}"
            }
        })
    }

}