package com.firemaples.youtubeplayertest.ui.webviewiframeapi

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebSettings
import android.webkit.WebView

class IFrameWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : WebView(context, attrs) {

    @SuppressLint("SetJavaScriptEnabled")
    fun init() {
        with(settings) {
            javaScriptEnabled = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = WebSettings.LOAD_NO_CACHE
        }

        loadUrl("file:///android_asset/youtube_iframe.html")
    }
}
