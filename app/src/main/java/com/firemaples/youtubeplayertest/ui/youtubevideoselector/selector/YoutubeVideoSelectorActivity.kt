package com.firemaples.youtubeplayertest.ui.youtubevideoselector.selector

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.firemaples.youtubeplayertest.R
import com.firemaples.youtubeplayertest.databinding.ActivityYoutubeVideoSelectorBinding
import com.firemaples.youtubeplayertest.utils.YoutubeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.Serializable
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class YoutubeVideoSelectorActivity : AppCompatActivity() {
    companion object {
        private val TAG = YoutubeVideoSelectorActivity::class.java.simpleName
        private const val YOUTUBE_VIDEO_LIST_URL = "https://m.youtube.com/"

        private const val EXTRA_VIDEO_INFO = "video_info"

        fun getIntent(context: Context): Intent =
            Intent(context, YoutubeVideoSelectorActivity::class.java)

        fun getVideoInfo(data: Intent?): YoutubeVideoInfo? =
            data?.getSerializableExtra(EXTRA_VIDEO_INFO) as? YoutubeVideoInfo
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
                if (canGoBack()) {
                    goBack()
                }
                onVideoSelected(videoId, url)
            }

            loadUrl(YOUTUBE_VIDEO_LIST_URL)
        }
    }

    override fun onBackPressed() {
        with(binding.youtubeVideoSelector) {
            if (canGoBack()) {
                goBack()
                return
            }
        }

        super.onBackPressed()
    }

    private fun onVideoSelected(videoId: String, url: String) {
        lifecycleScope.launch {
            val videoInfo = withContext(Dispatchers.IO) { retrieveVideoInfo(videoId, url) }

            val result = showDialog(videoInfo.toInfoString())

            if (result) {
                setResult(RESULT_OK, Intent().apply {
                    putExtra(EXTRA_VIDEO_INFO, videoInfo)
                })
                finish()
            }
        }
    }

    private fun retrieveVideoInfo(videoId: String, url: String): YoutubeVideoInfo {
        val fields = arrayOf(
            "snippet", // For 'title' or 'localized.title' and 'liveBroadcastContent'
            "contentDetails", // For 'regionRestriction.allowed' and 'regionRestriction.blocked'
            "status", // For 'embeddable'
            "player", // For 'embedHtml'
            "liveStreamingDetails", // For 'liveStreamingDetails' itself
        ).joinToString(separator = ",")

        val uri = Uri.parse("https://www.googleapis.com/youtube/v3/videos")
            .buildUpon()
            .appendQueryParameter("key", getString(R.string.youtube_data_api_key))
            .appendQueryParameter("id", videoId)
            .appendQueryParameter("part", fields)
            .build()
        Log.i(TAG, "retrieveVideoInfo: $uri")
        val conn = URL(uri.toString()).openConnection()
        val text = conn.getInputStream().use { it.bufferedReader().readText() }
        Log.i(TAG, "retrieveVideoInfo: $text")

        val root = JSONObject(text)
        val item = root.getJSONArray("items").getJSONObject(0)
        val snippetObj = item.getJSONObject("snippet")
        val title = snippetObj.getString("title")
        val liveBroadcastContent =
            if (snippetObj.has("liveBroadcastContent"))
                LiveBroadcastContent.fromValue(snippetObj.getString("liveBroadcastContent"))
            else LiveBroadcastContent.DEFAULT
        val statusObj = item.getJSONObject("status")
        val embeddable = statusObj.getBoolean("embeddable")
        val embeddable1 = retrieveVideoIsEmbeddable(videoId)
        val isLiveStreaming = item.has("liveStreamingDetails")

        return YoutubeVideoInfo(
            videoId = videoId,
            url = url,
            title = title,
            liveBroadcastContent = liveBroadcastContent,
            embeddable = embeddable1,
            isLiveStreaming = isLiveStreaming,
        )
    }

    /**
     * https://stackoverflow.com/a/71975098/2906153
     * https://support.google.com/youtube/thread/20562339?hl=en&msgid=32097030
     */
    private fun retrieveVideoIsEmbeddable(videoId: String): Boolean {
        val text = URL("https://www.youtube.com/embed/$videoId")
            .openStream().use { it.bufferedReader().readText() }
        return !text.contains("UNPLAYABLE", ignoreCase = true)
    }

    private suspend fun showDialog(text: String): Boolean =
        suspendCoroutine { c ->
            var resumed = false
            AlertDialog.Builder(this)
                .setMessage(text)
                .setPositiveButton("Okay") { dialog, which ->
                    resumed = true
                    c.resume(true)
                }
                .setOnDismissListener {
                    if (!resumed) {
                        c.resume(false)
                    }
                }
                .show()
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

    data class YoutubeVideoInfo(
        val videoId: String,
        val url: String,
        val title: String,
        val liveBroadcastContent: LiveBroadcastContent,
        val embeddable: Boolean,
        val isLiveStreaming: Boolean,
    ) : Serializable {
        fun toInfoString(): String =
            "videoId: $videoId\ntitle: $title\nliveBroadcastContent: $liveBroadcastContent\nembeddable: $embeddable\nisLiveStreaming: $isLiveStreaming"
    }

    enum class LiveBroadcastContent(val value: String) {
        NONE("none"),
        LIVE("live"),
        UPCOMING("upcoming"),
        UNKNOWN("");

        companion object {
            val DEFAULT = UNKNOWN

            fun fromValue(value: String?): LiveBroadcastContent =
                values().firstOrNull { it.value == value } ?: DEFAULT
        }
    }
}
