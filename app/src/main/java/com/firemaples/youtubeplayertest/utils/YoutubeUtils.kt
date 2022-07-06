package com.firemaples.youtubeplayertest.utils

import android.util.Log
import java.io.IOException
import java.net.URL
import kotlin.time.Duration.Companion.milliseconds

object YoutubeUtils {
    private val TAG = YoutubeUtils::class.java.simpleName
    private val REG_EXPIRED = "expire[=/]([^&/]*)".toRegex()

    // https://stackoverflow.com/a/66786241/2906153
//    private val videoIdReg =
//        "^((?:https?:)?//)?((?:www|m)\\.)?((?:youtube\\.com|youtu.be|youtube-nocookie.com))(/(?:[\\w\\-]+\\?v=|feature=|watch\\?|e/|embed/|v/)?)([\\w\\-]+)(\\S+)?\$"
//            .toRegex()
    // https://stackoverflow.com/a/27728417/2906153
    private val videoIdReg =
        "^.*(?:(?:youtu\\.be\\/|v\\/|vi\\/|u\\/\\w\\/|embed\\/|shorts\\/)|(?:(?:watch)?\\?v(?:i)?=|\\&v(?:i)?=))([^#\\&\\?]*).*"
            .toRegex()

    fun extractYoutubeVideoId(url: String): String? {
        val result = videoIdReg.find(url) ?: return null

//        return result.groupValues.getOrNull(5)
        return result.groupValues.getOrNull(1)
    }

    fun toMmSs(millis: Long): String =
        millis.milliseconds.toComponents { hours, minutes, seconds, _ ->
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        }

    var userAgent: String = ""

    fun retrieveInnerTubeAPIKey(videoId: String, userAgent: String): String? {
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

    fun findExpireTime(url: String): Long? =
        REG_EXPIRED.find(url)?.let { it.groupValues.getOrNull(1)?.toLongOrNull() }
}
