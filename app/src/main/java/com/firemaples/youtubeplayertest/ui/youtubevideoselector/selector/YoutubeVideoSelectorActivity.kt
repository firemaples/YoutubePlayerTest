package com.firemaples.youtubeplayertest.ui.youtubevideoselector.selector

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.firemaples.youtubeplayertest.databinding.ActivityYoutubeVideoSelectorBinding
import com.firemaples.youtubeplayertest.utils.YoutubeUtils

class YoutubeVideoSelectorActivity : AppCompatActivity() {
    companion object {
        private val TAG = YoutubeVideoSelectorActivity::class.java.simpleName
        private const val YOUTUBE_VIDEO_LIST_URL = "https://m.youtube.com/"

        private const val EXTRA_VIDEO_ID = "video_id"
        private const val EXTRA_VIDEO_URL = "video_url"

        fun getIntent(context: Context): Intent =
            Intent(context, YoutubeVideoSelectorActivity::class.java)

        fun getVideoId(data: Intent?): String? = data?.getStringExtra(EXTRA_VIDEO_ID)
        fun getVideoURL(data: Intent?): String? = data?.getStringExtra(EXTRA_VIDEO_URL)
    }

    private lateinit var binding: ActivityYoutubeVideoSelectorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYoutubeVideoSelectorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setViews()
    }

    private fun setViews() {
        with(binding.youtubeVideoSelector) {
            with(settings) {
                javaScriptEnabled = true
            }

            webViewClient = MyWebViewClient { videoId, url ->
                setResult(RESULT_OK, Intent().apply {
                    putExtra(EXTRA_VIDEO_ID, videoId)
                    putExtra(EXTRA_VIDEO_URL, url)
                })
                finish()
            }

            loadUrl(YOUTUBE_VIDEO_LIST_URL)
        }
    }

    private inner class MyWebViewClient(val onVideoSelected: (videoId: String, url: String) -> Unit) :
        WebViewClient() {
        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            super.doUpdateVisitedHistory(view, url, isReload)
            val targetUrl = view?.url ?: return
            val videoId = YoutubeUtils.extractYoutubeVideoId(targetUrl)
            Log.i(TAG, "doUpdateVisitedHistory(), url: $targetUrl, videoId: $videoId")

            if (videoId != null) {
                onVideoSelected.invoke(videoId, targetUrl)
            }
        }
    }
}
