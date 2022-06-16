package com.firemaples.iframeyoutubeplayer.player.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.contains
import com.firemaples.iframeyoutubeplayer.player.YouTubePlayer
import com.firemaples.iframeyoutubeplayer.player.YouTubePlayerListener

class YoutubePlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : YoutubeViewWrapper(context, attrs) {

    companion object {
        private val TAG = YoutubePlayerView::class.java.simpleName
    }

    private val player: YoutubePlayerWebView = YoutubePlayerWebView(context)
    private val touchInterceptor: View by lazy {
        View(context).apply {
            isClickable = true
            isFocusable = true
        }
    }

    init {
        addView(player, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    fun init(
        enableControls: Boolean = true,
        hideExtraUI: Boolean = false,
        initializedCallback: (YouTubePlayer) -> Unit,
    ) {
        player.init(
            enableControls = enableControls,
            hideExtraUI = hideExtraUI,
            initializedCallback = initializedCallback,
        )

        if (enableControls) {
            removeView(touchInterceptor)
        } else {
            if (!contains(touchInterceptor)) {
                addView(touchInterceptor)
            }
        }
    }

    fun addListener(listener: YouTubePlayerListener): Boolean =
        player.addListener(listener)

    fun removeListener(listener: YouTubePlayerListener): Boolean =
        player.removeListener(listener)

    fun addCustomView(view: View) {
        if (view.parent != null) return
        addView(view)
    }

    fun removeCustomView(view: View) {
        removeView(view)
    }
}
