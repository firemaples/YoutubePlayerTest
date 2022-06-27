package com.firemaples.youtubeplayertest.utils

import kotlin.time.Duration.Companion.milliseconds

object YoutubeUtils {
    // https://stackoverflow.com/a/66786241/2906153
//    private val videoIdReg =
//        "^((?:https?:)?//)?((?:www|m)\\.)?((?:youtube\\.com|youtu.be|youtube-nocookie.com))(/(?:[\\w\\-]+\\?v=|feature=|watch\\?|e/|embed/|v/)?)([\\w\\-]+)(\\S+)?\$"
//            .toRegex()
    // https://stackoverflow.com/a/27728417/2906153
    private val videoIdReg =
    "^.*(?:(?:youtu\\\\.be\\\\|v\\\\|vi\\\\|u\\\\\\\\w\\\\|embed\\\\|shorts\\\\)|(?:(?:watch)?\\\\?v(?:i)?=|\\\\&v(?:i)?=))([^#\\\\&\\\\?]*).*"
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
}
