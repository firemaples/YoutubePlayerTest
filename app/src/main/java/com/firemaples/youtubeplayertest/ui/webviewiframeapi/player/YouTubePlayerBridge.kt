package com.firemaples.youtubeplayertest.ui.webviewiframeapi.player

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.webkit.JavascriptInterface

class YouTubePlayerBridge(private val callback: YouTubePlayerBridgeCallbacks) {

    companion object {
        private val TAG = YouTubePlayerBridge::class.java.simpleName
    }

    private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())

    interface YouTubePlayerBridgeCallbacks {
        fun getInstance(): YouTubePlayer
        fun getListeners(): Collection<YouTubePlayerListener>
        fun onReady()
    }

//    @JavascriptInterface
//    fun sendYouTubeIFrameAPIReady() {
//        Log.i(TAG, "sendYouTubeIFrameAPIReady()")
//        mainThreadHandler.post { callback.onYouTubeIFrameAPIReady() }
//    }

    @JavascriptInterface
    fun sendReady() {
        mainThreadHandler.post {
            callback.onReady()
            for (listener in callback.getListeners())
                listener.onReady(callback.getInstance())
        }
    }

    @JavascriptInterface
    fun sendStateChange(state: String) {
//        val playerState = parsePlayerState(state)
//
//        mainThreadHandler.post {
//            for (listener in callback.getListeners())
//                listener.onStateChange(callback.getInstance(), playerState)
//        }
    }

    @JavascriptInterface
    fun sendPlaybackQualityChange(quality: String) {
//        val playbackQuality = parsePlaybackQuality(quality)
//
//        mainThreadHandler.post {
//            for (listener in callback.getListeners())
//                listener.onPlaybackQualityChange(callback.getInstance(), playbackQuality)
//        }
    }

    @JavascriptInterface
    fun sendPlaybackRateChange(rate: String) {
//        val playbackRate = parsePlaybackRate(rate)
//
//        mainThreadHandler.post {
//            for (listener in callback.getListeners())
//                listener.onPlaybackRateChange(callback.getInstance(), playbackRate)
//        }
    }

    @JavascriptInterface
    fun sendError(error: String) {
//        val playerError = parsePlayerError(error)
//
//        mainThreadHandler.post {
//            for (listener in callback.getListeners())
//                listener.onError(callback.getInstance(), playerError)
//        }
    }

    @JavascriptInterface
    fun sendApiChange() {
//        mainThreadHandler.post {
//            for (listener in callback.getListeners())
//                listener.onApiChange(callback.getInstance())
//        }
    }

    @JavascriptInterface
    fun sendVideoCurrentTime(seconds: String) {
        val currentTimeSeconds: Float
        try {
            currentTimeSeconds = seconds.toFloat()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            return
        }

        mainThreadHandler.post {
            for (listener in callback.getListeners())
                listener.onCurrentSecond(callback.getInstance(), currentTimeSeconds)
        }
    }

    @JavascriptInterface
    fun sendVideoDuration(seconds: String) {
        val videoDuration: Float
        try {
            val finalSeconds = if (TextUtils.isEmpty(seconds)) "0" else seconds
            videoDuration = finalSeconds.toFloat()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            return
        }

        mainThreadHandler.post {
            for (listener in callback.getListeners())
                listener.onVideoDuration(callback.getInstance(), videoDuration)
        }
    }

    @JavascriptInterface
    fun sendVideoLoadedFraction(fraction: String) {
        val loadedFraction: Float
        try {
            loadedFraction = fraction.toFloat()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            return
        }

//        mainThreadHandler.post {
//            for (listener in callback.getListeners())
//                listener.onVideoLoadedFraction(callback.getInstance(), loadedFraction)
//        }
    }

    @JavascriptInterface
    fun sendVideoId(videoId: String) {
//        mainThreadHandler.post {
//            for (listener in callback.getListeners())
//                listener.onVideoId(callback.getInstance(), videoId)
//        }
    }
}