package com.firemaples.youtubeplayertest

import android.app.Application
import android.util.Log
import android.webkit.WebView
import com.firemaples.youtubeplayertest.utils.Utils

class CoreApplication : Application() {

    companion object {
        lateinit var INSTANCE: Application
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        Utils.userAgent = WebView(this).settings.userAgentString

        Log.i("CoreApplication", "userAgent: ${Utils.userAgent}")
    }
}
