package com.firemaples.youtubeplayertest.ui.webviewiframeapi.player

/**
 * https://developers.google.com/youtube/iframe_api_reference#onStateChange
 */
enum class PlayerState(private val state: String) {
    UNSTARTED("UNSTARTED"),
    ENDED("ENDED"),
    PLAYING("PLAYING"),
    PAUSED("PAUSED"),
    BUFFERING("BUFFERING"),
    CUED("CUED"),
    UNKNOWN("");

    companion object {
        fun parse(state: String): PlayerState =
            values().firstOrNull { it.state.equals(state, ignoreCase = true) } ?: UNKNOWN
    }
}

/**
 * https://developers.google.com/youtube/iframe_api_reference#onError
 */
enum class PlayerError(private val code: String) {
    INVALID_VIDEO_ID("2"),
    HTML5_PLAYER_ERROR("5"),
    VIDEO_NOT_FOUND("100"),
    CANT_PLAY_IN_EMBEDDED_PLAYER_1("101"),
    CANT_PLAY_IN_EMBEDDED_PLAYER_2("150"),
    UNKNOWN("");

    companion object {
        fun parse(code: String): PlayerError =
            values().firstOrNull { it.code == code } ?: UNKNOWN
    }
}

/**
 * https://developers.google.com/youtube/iframe_api_reference#onPlaybackQualityChange
 */
enum class PlaybackQuality(private val quality: String) {
    SMALL("small"),
    MEDIUM("medium"),
    LARGE("large"),
    HD720("hd720"),
    HD1080("hd1080"),
    HIGHRES("highres"),
    UNKNOWN("");

    companion object {
        fun parse(quality: String): PlaybackQuality =
            values().firstOrNull { it.quality.equals(quality, ignoreCase = true) } ?: UNKNOWN
    }
}
