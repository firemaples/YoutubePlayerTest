package com.firemaples.youtubeplayertest.ui.webviewiframeapi.player.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.firemaples.youtubeplayertest.R
import com.firemaples.youtubeplayertest.ui.webviewiframeapi.player.YouTubePlayer
import com.firemaples.youtubeplayertest.ui.webviewiframeapi.player.YouTubePlayerListener

class YoutubePlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : YoutubeViewWrapper(context, attrs) {

    companion object {
        private val TAG = YoutubePlayerView::class.java.simpleName
    }

    private val player: YoutubePlayerWebView = YoutubePlayerWebView(context)

    init {
        addView(player, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        addView(View.inflate(context, R.layout.view_player_control, null)) //The intercept the touch event
    }

    fun init(initializedCallback: (YouTubePlayer) -> Unit) {
        player.init(initializedCallback)
    }

    fun addListener(listener: YouTubePlayerListener): Boolean =
        player.addListener(listener)

    fun removeListener(listener: YouTubePlayerListener): Boolean =
        player.removeListener(listener)
}
