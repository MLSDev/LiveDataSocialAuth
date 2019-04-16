package com.mlsdev.livedatasocialauth.sample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.credentials.IdentityProviders
import com.mlsdev.livedatasocialauth.library.auth.FacebookAuth
import com.mlsdev.livedatasocialauth.library.auth.GoogleAuth
import com.mlsdev.livedatasocialauth.library.smartlock.SmartLock
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SmartLock.Builder(WeakReference(this))
            .setAccountTypes(IdentityProviders.GOOGLE, IdentityProviders.FACEBOOK)
            .build()
            .requestCredentialAndAutoSignIn()
            .observe(this, Observer {
                if (it.isSuccess) {
                    startActivity(Intent(this, LogOutActivity::class.java))
                    finish()
                } else {
                    Log.e("SmartLock.SignIn", it.exception?.message)
                }
            })


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
                .requestEmail()
                .requestProfile()
                .enableSmartLock()
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
