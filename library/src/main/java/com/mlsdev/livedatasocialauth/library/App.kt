package com.mlsdev.livedatasocialauth.library

import android.app.Application
import android.content.Context
import java.lang.ref.WeakReference

class App : Application() {

    companion object {
        lateinit var contextReference: WeakReference<Context>
            private set
    }

    override fun onCreate() {
        super.onCreate()
        contextReference = WeakReference(this)
    }

}