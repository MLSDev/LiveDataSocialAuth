package com.mlsdev.livedatasocialauth.sample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
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
                        Log.d("Facebook.SignIn", "id:           ${it.account?.id}")
                        Log.d("Facebook.SignIn", "email:        ${it.account?.email}")
                        Log.d("Facebook.SignIn", "display name: ${it.account?.displayName}")
                        Log.d("Facebook.SignIn", "first name:   ${it.account?.firstName}")
                        Log.d("Facebook.SignIn", "last name:    ${it.account?.lastName}")
                    } else {
                        Log.e("Facebook.SignIn", it.exception?.message)
                        Toast.makeText(
                            this, it.exception?.message
                                ?: "Facebook sign in error", Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }

        findViewById<Button>(R.id.button_with_google).setOnClickListener {
            GoogleAuth.Builder(this)
                .requestEmail()
                .requestProfile()
                .build()
                .signIn()
                .observe(this, Observer {
                    if (it.isSuccess) {
                        startActivity(Intent(this, LogOutActivity::class.java))
                        finish()
                        Log.d("Google.SignIn", "id:           ${it.account?.id}")
                        Log.d("Google.SignIn", "email:        ${it.account?.email}")
                        Log.d("Google.SignIn", "display name: ${it.account?.displayName}")
                        Log.d("Google.SignIn", "first name:   ${it.account?.firstName}")
                        Log.d("Google.SignIn", "last name:    ${it.account?.lastName}")
                    } else {
                        Toast.makeText(
                            this, it.exception?.message
                                ?: "Google sign in error", Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }
}
