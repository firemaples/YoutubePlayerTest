package com.firemaples.youtubeplayertest.ui.exoplayerwithinternalapi

import android.content.Context
import android.util.Log
import android.webkit.WebView
import com.firemaples.youtubeplayertest.utils.YoutubeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object YoutubeHlsRetriever {
    private val TAG = YoutubeHlsRetriever::class.java.simpleName
    private const val USER_AGENT =
        "com.google.ios.youtube/16.28.2 (iPhone12,3; U; CPU iOS 13_5 like Mac OS X; en_US)"

    suspend fun retrieve(context: Context, videoId: String): HlsMediaInfo? {
        val appContext = context.applicationContext

        return coroutineScope {
            val userAgent = withContext(Dispatchers.Main) {
                WebView(appContext).settings.userAgentString
            }

            val apiKey = withContext(Dispatchers.IO) {
                YoutubeUtils.retrieveInnerTubeAPIKey(
                    videoId,
                    userAgent
                )
            } ?: return@coroutineScope null

            withContext(Dispatchers.IO) { retrieveMediaInfo(videoId, apiKey) }
        }
    }

    private fun retrieveMediaInfo(
        videoId: String,
        apiKey: String
    ): HlsMediaInfo? {
        val payload =
            "{\"context\":{\"client\":{\"clientName\":\"IOS\",\"deviceModel\":\"iPhone\",\"hl\":\"en\",\"clientVersion\":\"16.30.34\"}},\"videoId\":\"$videoId\"}"

        val response = try {
            val conn =
                URL("https://www.youtube.com/youtubei/v1/player?key=$apiKey")
                    .openConnection() as HttpsURLConnection
            with(conn) {
                useCaches = false
                doInput = true
                doOutput = true
                requestMethod = "POST"
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("content-type", "application/json")
                setRequestProperty("accept", "*/*")
                setRequestProperty("Host", "www.youtube.com")
                setRequestProperty("accept-language", "en-US,en;q=0.9")

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

        try {
            val root = JSONObject(response)

            val streamingData = root.getJSONObject("streamingData")
            val hlsManifestUrl = streamingData.getString("hlsManifestUrl")
            val expiredTime: Long? = YoutubeUtils.findExpireTime(hlsManifestUrl)

            val videoDetails = root.getJSONObject("videoDetails")
            val title = videoDetails.getString("title")

            return HlsMediaInfo(
                videoId = videoId,
                title = title,
                expiredTime = expiredTime,
                hlsManifestUrl = hlsManifestUrl,
                userAgent = USER_AGENT,
            )
        } catch (e: Exception) {
            Log.e(TAG, "", e)
            return null
        }
    }

    data class HlsMediaInfo(
        val videoId: String,
        val title: String,
        val expiredTime: Long?,
        val userAgent: String,
        val hlsManifestUrl: String,
    )
}
