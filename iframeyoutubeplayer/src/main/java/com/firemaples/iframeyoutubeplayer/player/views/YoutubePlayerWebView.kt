package com.firemaples.iframeyoutubeplayer.player.views

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.webkit.*
import com.firemaples.iframeyoutubeplayer.player.YouTubePlayer
import com.firemaples.iframeyoutubeplayer.player.YouTubePlayerBridge
import com.firemaples.iframeyoutubeplayer.player.YouTubePlayerListener
import java.io.SequenceInputStream
import java.net.URL
import java.util.*

class YoutubePlayerWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : WebView(context, attrs), YouTubePlayer, YouTubePlayerBridge.YouTubePlayerBridgeCallbacks {

    companion object {
        private val TAG = YoutubePlayerWebView::class.java.simpleName

        private const val PARAM_CONTROLS = "controls"
    }

    private val youTubePlayerListeners = HashSet<YouTubePlayerListener>()
    private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())

    private var initializedCallback: ((YouTubePlayer) -> Unit)? = null

    private var _availablePlaybackRates: FloatArray = floatArrayOf()
    override val availablePlaybackRates: FloatArray
        get() = _availablePlaybackRates

    private var hideExtraUI: Boolean = false

    @SuppressLint("SetJavaScriptEnabled")
    fun init(
        enableControls: Boolean,
        hideExtraUI: Boolean,
        initializedCallback: (YouTubePlayer) -> Unit,
    ) {
        this.hideExtraUI = hideExtraUI
        this.initializedCallback = initializedCallback

        with(settings) {
            javaScriptEnabled = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
        }

        addJavascriptInterface(YouTubePlayerBridge(this), "YouTubePlayerBridge")

        webChromeClient = object : WebChromeClient() {}
        webViewClient = MyWebViewClient()

        val url = Uri.parse("file:///android_asset/youtube_iframe.html")
            .buildUpon()
            .appendQueryParameter(PARAM_CONTROLS, if (enableControls) "1" else "0")
            .build()

        loadUrl(url.toString())
    }

    override fun loadVideo(videoId: String, startSeconds: Float) {
        mainThreadHandler.post { loadUrl("javascript:loadVideo('$videoId', $startSeconds)") }
    }

    override fun cueVideo(videoId: String, startSeconds: Float) {
        mainThreadHandler.post { loadUrl("javascript:cueVideo('$videoId', $startSeconds)") }
    }

    override fun play() {
        mainThreadHandler.post { loadUrl("javascript:playVideo()") }
    }

    override fun pause() {
        mainThreadHandler.post { loadUrl("javascript:pauseVideo()") }
    }

    override fun mute() {
        mainThreadHandler.post { loadUrl("javascript:mute()") }
    }

    override fun unMute() {
        mainThreadHandler.post { loadUrl("javascript:unMute()") }
    }

    override fun setVolume(volumePercent: Int) {
        require(!(volumePercent < 0 || volumePercent > 100)) { "Volume must be between 0 and 100" }

        mainThreadHandler.post { loadUrl("javascript:setVolume($volumePercent)") }
    }

    override fun seekTo(time: Float) {
        mainThreadHandler.post { loadUrl("javascript:seekTo($time)") }
    }

    override fun setPlaybackRate(playbackRate: Float) {
        post { loadUrl("javascript:setPlaybackRate(${playbackRate})") }
    }

//    override fun setPlaybackRate(playbackRate: PlayerConstants.PlaybackRate) {
//        post { loadUrl("javascript:setPlaybackRate(${playbackRate.toFloat()})") }
//    }

    override fun addListener(listener: YouTubePlayerListener): Boolean =
        youTubePlayerListeners.add(listener)

    override fun removeListener(listener: YouTubePlayerListener): Boolean =
        youTubePlayerListeners.remove(listener)

    override fun getInstance(): YouTubePlayer = this

    override fun getListeners(): Collection<YouTubePlayerListener> = youTubePlayerListeners.toList()

    override fun onReady(playbackRates: FloatArray) {
        _availablePlaybackRates = playbackRates
        initializedCallback?.invoke(this)
    }

    override fun destroy() {
        youTubePlayerListeners.clear()
        mainThreadHandler.removeCallbacksAndMessages(null)
        super.destroy()
    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            val url = request?.url
            Log.i(TAG, "shouldInterceptRequest: $url")

            if (hideExtraUI) {
                if (url != null && url.toString().contains("www-player.css")) {
                    Log.i(TAG, "intercept url: $url")

                    val cssIs = URL(url.toString()).openStream()
                    val itemsToHide = arrayOf(
                        ".ytp-chrome-top.ytp-show-cards-title", //Title bar
                        ".ytp-pause-overlay.ytp-scroll-min", //Relative videos on pausing
                        ".ytp-gradient-top", //Top gradient on pausing
                        "a.ytp-watermark.yt-uix-sessionlink", //Logo
                        ".html5-endscreen.ytp-player-content.videowall-endscreen.ytp-endscreen-paginate.ytp-show-tiles", //Relative videos on the end
                        "button.ytp-button.ytp-endscreen-previous", //Previous button for relative videos on the end
                        "button.ytp-button.ytp-endscreen-next", //Next button for relative videos on the end
                    ).joinToString(separator = ",")
                    val css = "$itemsToHide { visibility: hidden; }"

                    val isArray = listOf(cssIs, css.byteInputStream())

                    val sis = SequenceInputStream(Collections.enumeration(isArray))

                    return WebResourceResponse("text/css", "UTF-8", sis)
                }
            }

            return super.shouldInterceptRequest(view, request)
        }
    }
}
