package com.mlsdev.livedatasocialauth.sample

import android.annotation.SuppressLint
import android.content.Intent
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
                if (it.success) {
                    val intent = Intent(this, SplashActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } else Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            })
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()
        SocialAuthManager.getCurrentAccount().observe(this, Observer {
            it.authProvider?.let { provider ->
                findViewById<TextView>(R.id.text_auth_provider).text = getString(R.string.template_signed_in_with, provider.value)
            }

            findViewById<TextView>(R.id.text_id).text = getString(R.string.template_id, it.id.toString())
            findViewById<TextView>(R.id.text_display_name).text = getString(R.string.template_display_name, it.displayName.toString())
            findViewById<TextView>(R.id.text_first_name).text = getString(R.string.template_first_name, it.firstName.toString())
            findViewById<TextView>(R.id.text_last_name).text = getString(R.string.template_last_name, it.lastName.toString())
        })
    }

}