package com.mlsdev.livedatasocialauth.sample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.mlsdev.livedatasocialauth.library.auth.FacebookAuth
import com.mlsdev.livedatasocialauth.library.auth.GoogleAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button_with_facebook).setOnClickListener {
            FacebookAuth.Builder(this)
                .requestEmail()
                .requestProfile()
                .build()
                .signIn()
                .observe(this, Observer {
                    if (it.isSuccess) {
                        startActivity(Intent(this, LogOutActivity::class.java))
                        finish()
                    } else {
                        Log.e("Facebook.SignIn", it.exception?.message)
                    }
                })
        }

        findViewById<Button>(R.id.button_with_google).setOnClickListener {
            GoogleAuth.Builder(this)
                .clientId("659203926601-fsnm6aeu2egvgd3vqdcducfqb0mjkqe0.apps.googleusercontent.com")
                .requestEmail()
                .requestProfile()
                .build()
                .signIn()
                .observe(this, Observer {
                    if (it.isSuccess) {
                        startActivity(Intent(this, LogOutActivity::class.java))
                        finish()
                    } else {
                        Log.e("Google.SignIn", it.exception?.message)
                    }
                })
        }
    }
}
