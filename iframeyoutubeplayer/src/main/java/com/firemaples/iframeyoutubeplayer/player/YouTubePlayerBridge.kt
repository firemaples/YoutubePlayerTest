package com.firemaples.iframeyoutubeplayer.player

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.webkit.JavascriptInterface
import org.json.JSONArray

@Suppress("unused")
class YouTubePlayerBridge(private val callback: YouTubePlayerBridgeCallbacks) {

    companion object {
        private val TAG = YouTubePlayerBridge::class.java.simpleName
    }

    private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())

    interface YouTubePlayerBridgeCallbacks {
        fun getInstance(): YouTubePlayer
        fun getListeners(): Collection<YouTubePlayerListener>
        fun onReady(playbackRates: FloatArray)
    }

//    @JavascriptInterface
//    fun sendYouTubeIFrameAPIReady() {
//        Log.i(TAG, "sendYouTubeIFrameAPIReady()")
//        mainThreadHandler.post { callback.onYouTubeIFrameAPIReady() }
//    }

    @JavascriptInterface
    fun sendReady(availableRatesString: String) {
        Log.i(TAG, "sendReady: $availableRatesString")

        val availablePlaybackRates = JSONArray(availableRatesString).let {
            (0 until it.length()).map { i -> it.getDouble(i).toFloat() }
        }.toFloatArray()

        mainThreadHandler.post {
            callback.onReady(availablePlaybackRates)
            for (listener in callback.getListeners())
                listener.onReady(callback.getInstance())
        }
    }

    @JavascriptInterface
    fun sendStateChange(state: String) {
        val playerState = PlayerState.parse(state)

        mainThreadHandler.post {
            for (listener in callback.getListeners())
                listener.onStateChange(callback.getInstance(), playerState)
        }
    }

    @JavascriptInterface
    fun sendPlaybackQualityChange(quality: String) {
        val playbackQuality = PlaybackQuality.parse(quality)

        mainThreadHandler.post {
            for (listener in callback.getListeners())
                listener.onPlaybackQualityChange(callback.getInstance(), playbackQuality)
        }
    }

    @JavascriptInterface
    fun sendPlaybackRateChange(rate: String) {
        val rate = rate.toFloatOrNull() ?: return
//
        mainThreadHandler.post {
            for (listener in callback.getListeners())
                listener.onPlaybackRateChange(callback.getInstance(), rate)
        }
    }

    @JavascriptInterface
    fun sendError(error: String) {
        val playerError = PlayerError.parse(error)

        mainThreadHandler.post {
            for (listener in callback.getListeners())
                listener.onError(callback.getInstance(), playerError)
        }
    }

    @JavascriptInterface
    fun sendApiChange() {
        mainThreadHandler.post {
            for (listener in callback.getListeners())
                listener.onApiChange(callback.getInstance())
        }
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

        mainThreadHandler.post {
            for (listener in callback.getListeners())
                listener.onVideoLoadedFraction(callback.getInstance(), loadedFraction)
        }
    }

    @JavascriptInterface
    fun sendVideoId(videoId: String) {
        mainThreadHandler.post {
            for (listener in callback.getListeners())
                listener.onVideoId(callback.getInstance(), videoId)
        }
    }
}