package com.firemaples.youtubeplayertest

import android.app.Application
import android.util.Log
import android.webkit.WebView
import com.firemaples.youtubeplayertest.utils.YoutubeUtils

class CoreApplication : Application() {

    companion object {
        lateinit var INSTANCE: Application
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        YoutubeUtils.userAgent = WebView(this).settings.userAgentString

        Log.i("CoreApplication", "userAgent: ${YoutubeUtils.userAgent}")
    }
}
