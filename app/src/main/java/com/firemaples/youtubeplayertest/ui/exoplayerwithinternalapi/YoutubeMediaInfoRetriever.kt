package com.firemaples.youtubeplayertest.ui.exoplayerwithinternalapi

import android.content.Context
import android.util.Log
import android.webkit.WebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object YoutubeMediaInfoRetriever {
    private val TAG = YoutubeMediaInfoRetriever::class.java.simpleName

    suspend fun retrieve(context: Context, videoId: String): MediaInfo? {
        val appContext = context.applicationContext

        return coroutineScope {
            val userAgent = withContext(Dispatchers.Main) {
                WebView(appContext).settings.userAgentString
            }

            val apiKey = withContext(Dispatchers.IO) { retrieveAPIKey(videoId, userAgent) }
                ?: return@coroutineScope null
            withContext(Dispatchers.IO) { retrieveMediaInfo(videoId, apiKey) }
        }
    }

    private fun retrieveAPIKey(videoId: String, userAgent: String): String? {
        try {
            val conn = URL("https://www.youtube.com/watch?v=$videoId").openConnection()
            conn.setRequestProperty("User-Agent", userAgent)
            val html = conn.getInputStream().use { it.bufferedReader().readText() }

            val reg =
                "\"INNERTUBE_API_KEY\":\"(\\w+)\",\"INNERTUBE_API_VERSION\":\"(\\w+)\"".toRegex()
            val result = reg.find(html)
            if (result != null) {
                return result.groupValues.getOrNull(1)
            }
        } catch (e: IOException) {
            Log.e(TAG, "", e)
        }

        return null
    }

    private fun retrieveMediaInfo(videoId: String, apiKey: String): MediaInfo? {
        val payload =
            "{\"context\":{\"client\":{\"hl\":\"en\",\"clientName\":\"ANDROID\",\"clientVersion\":\"16.30.34\",\"playbackContext\":{\"contentPlaybackContext\":{\"html5Preference\":\"HTML5_PREF_WANTS\"}}}},\"videoId\":\"$videoId\"}"

        val response = try {
            val conn =
                URL("https://youtubei.googleapis.com/youtubei/v1/player?key=$apiKey&alt=json")
                    .openConnection() as HttpsURLConnection
            with(conn) {
                useCaches = false
                doInput = true
                doOutput = true
                requestMethod = "POST"
                setRequestProperty(
                    "User-Agent",
                    "com.google.android.youtube/16.30.34 (Linux; U; Android 9; en_US)"
                )
                setRequestProperty("Content-Type", "application/json")

                outputStream.writer().use {
                    it.write(payload)
                    it.flush()
                }
            }

            conn.inputStream.use { it.bufferedReader().readText() }
        } catch (e: IOException) {
            Log.e(TAG, "", e)
            return null
        }

        Log.i(TAG, "response raw: $response")

        val root = JSONObject(response)
        val streamingData = root.getJSONObject("streamingData")
        val formats = streamingData.getJSONArray("formats")
        val mediaList = (0 until formats.length()).map {
            Media.fromJSONObject(formats.getJSONObject(it), isAdaptive = false)
        }.toMutableList()
        val adaptiveFormats = streamingData.getJSONArray("adaptiveFormats")
        mediaList.addAll((0 until adaptiveFormats.length()).map {
            Media.fromJSONObject(adaptiveFormats.getJSONObject(it), isAdaptive = true)
        })
        val expired = streamingData.getInt("expiresInSeconds") * 1000 + System.currentTimeMillis()

        val videoDetails = root.getJSONObject("videoDetails")
        val title = videoDetails.getString("title")
        val thumbnails = videoDetails.getJSONObject("thumbnail").getJSONArray("thumbnails")
        val thumbnailList =
            (0 until thumbnails.length())
                .map { Thumbnail.fromJSONObject(thumbnails.getJSONObject(it)) }
                .toList()

        return MediaInfo(
            videoId = videoId,
            title = title,
            expiredTime = expired,
            thumbnails = thumbnailList,
            mediaList = mediaList,
        )
    }


    data class MediaInfo(
        val videoId: String,
        val title: String,
        val expiredTime: Long,
        val thumbnails: List<Thumbnail>,
        val mediaList: List<Media>,
    )

    data class Thumbnail(
        val url: String,
        val width: Int,
        val height: Int,
    ) {
        companion object {
            fun fromJSONObject(item: JSONObject): Thumbnail =
                Thumbnail(
                    url = item.getString("url"),
                    width = item.getInt("width"),
                    height = item.getInt("height"),
                )
        }
    }

    data class Media(
        val isAdaptive: Boolean,
        val itag: Int,
        val url: String,
        val mimeType: String,
        val width: Int?,
        val height: Int?,
        val fps: Int?,
//        val vCodec: VCodec?,
//        val aCodec: ACodec?,
        val audioSampleRate: Int?,
//        val isDashContainer: Boolean,
//        val isHlsContent: Boolean,
    ) {
        companion object {
            fun fromJSONObject(item: JSONObject, isAdaptive: Boolean): Media =
                Media(
                    isAdaptive = isAdaptive,
                    itag = item.getInt("itag"),
                    url = item.getString("url"),
                    mimeType = item.getString("mimeType"),
                    width = if (item.has("width")) item.getInt("width") else null,
                    height = if (item.has("height")) item.getInt("height") else null,
                    fps = if (item.has("fps")) item.getInt("fps") else null,
                    audioSampleRate = if (item.has("audioSampleRate")) item.getInt("audioSampleRate") else null,
                )
        }
    }

//    enum class VCodec(val codec: String) {
//        H263, H264, MPEG4("mp4v"), VP8, VP9("vp9"), NONE
//    }
//
//    enum class ACodec(val codec: String) {
//        MP3, AAC("mp4a"), VORBIS, OPUS("opus"), NONE
//    }
}