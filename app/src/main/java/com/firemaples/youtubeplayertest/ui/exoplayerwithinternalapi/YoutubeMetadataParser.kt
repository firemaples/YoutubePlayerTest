package com.firemaples.youtubeplayertest.ui.exoplayerwithinternalapi

import com.firemaples.youtubeplayertest.utils.YoutubeUtils
import org.json.JSONObject


object YoutubeMetadataParser {
    private const val MIME_TYPE_PREFIX_AUDIO = "audio"
    private const val MIME_TYPE_PREFIX_VIDEO = "video"
    private const val FIELD_DELIMITER = "&"
    private val REG_EXPIRED = "expire[=/]([^&/]*)".toRegex()

    fun parseMetadata(root: JSONObject): YoutubeMetadata? {
        val streamingData = root.getJSONObject("streamingData")
        if (!streamingData.has("adaptiveFormats")) return null

        val videoDetails = root.getJSONObject("videoDetails")
        val lengthSeconds = videoDetails.getLong("lengthSeconds")

        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        sb.append("<MPD xmlns=\"urn:mpeg:dash:schema:mpd:2011\" profiles=\"urn:mpeg:dash:profile:full:2011\" minBufferTime=\"PT1.5S\" type=\"static\" mediaPresentationDuration=\"PT")
        sb.append(lengthSeconds)
        sb.append("S\">")
        sb.append("<Period>")

        val adaptiveFormats = streamingData.getJSONArray("adaptiveFormats").let { array ->
            (0 until array.length()).map { i ->
                AdaptiveFormat.parse(array.getJSONObject(i))
            }
        }

        var expiredAt: Long? = null
        var index = 0
        adaptiveFormats.groupBy { it.mimeType.split("; ")[0] }.forEach { (type, list) ->
            sb.append("<AdaptationSet id=\"")
            sb.append(index++)
            sb.append("\" mimeType=\"")
            sb.append(type)
            sb.append("\" startWithSAP=\"1\" subsegmentAlignment=\"true\"")
            if (type.contains(MIME_TYPE_PREFIX_VIDEO)) {
                sb.append(" scanType=\"progressive\"")
            }
            sb.append(">")

            list.forEach list@{ format ->
                format.indexRange ?: return@list
                sb.append("<Representation id=\"")
                sb.append(format.itag)
                sb.append("\" ")
                sb.append(format.mimeType.split("; ")[1])
                sb.append(" bandwidth=\"")
                sb.append(format.bitrate)
                sb.append("\"")
                if (type.contains(MIME_TYPE_PREFIX_VIDEO)) {
                    sb.append(" width=\"")
                    sb.append(format.width)
                    sb.append("\" height=\"")
                    sb.append(format.height)
                    sb.append("\" maxPlayoutRate=\"1\" frameRate=\"")
                    sb.append(format.fps)
                    sb.append("\">")
                } else {
                    sb.append(">")
                    sb.append("<AudioChannelConfiguration schemeIdUri=\"urn:mpeg:dash:23003:3:audio_channel_configuration:2011\" value=\"2\"/>")
                }
                sb.append("<BaseURL>")
                sb.append(
                    format.url.replace(FIELD_DELIMITER, "&amp;")
                )
                sb.append("</BaseURL>")
                sb.append("<SegmentBase indexRange=\"")
                sb.append(format.indexRange.start)
                sb.append("-")
                sb.append(format.indexRange.end)
                sb.append("\">")
                sb.append("<Initialization range=\"")
                sb.append(format.initRange.start)
                sb.append("-")
                sb.append(format.initRange.end)
                sb.append("\"/>")
                sb.append("</SegmentBase>")
                sb.append("</Representation>")

                if (expiredAt == null) {
                    expiredAt = YoutubeUtils.findExpireTime(format.url)
                }
            }

            sb.append("</AdaptationSet>")
        }

        if (root.has("captions")) {
            val trackArray = root.getJSONObject("captions")
                .getJSONObject("playerCaptionsTracklistRenderer")
                .getJSONArray("captionTracks")
            for (i in 0 until trackArray.length()) {
                val next = trackArray.getJSONObject(i)
                sb.append("<AdaptationSet mimeType=\"text/vtt\" lang=\"")
                sb.append(next.getString("languageCode"))
                sb.append("\">")
                sb.append("<Representation bandwidth=\"0\">")
                sb.append("<BaseURL>")
                sb.append(
                    next.getString("baseUrl")
                        .replace("&fmt=srv3", "&fmt=vtt")
                        .replace(FIELD_DELIMITER, "&amp;")
                )
                sb.append("</BaseURL>")
                sb.append("</Representation></AdaptationSet>")
            }
        }

        sb.append("</Period>")
        sb.append("</MPD>")

        return YoutubeMetadata(
            dashManifest = sb.toString(),
            expiredAt = expiredAt,
        )
    }

    data class YoutubeMetadata(
        val expiredAt: Long?,
//        val mediaUrls: Map<Int, String>,
        val dashManifest: String,
    )

    private data class AdaptiveFormat(
        val approxDurationMs: String,
        val audioChannels: Int?,
        val audioQuality: String?,
        val audioSampleRate: String?,
        val averageBitrate: Int,
        val bitrate: Int,
        val colorInfo: ColorInfo?,
        val contentLength: String,
        val fps: Int?,
        val height: Int?,
        val highReplication: Boolean,
        val indexRange: IndexRange?,
        val initRange: InitRange,
        val itag: Int,
        val lastModified: String,
        val mimeType: String,
        val projectionType: String,
        val quality: String,
        val qualityLabel: String?,
        val type: String?,
        val url: String,
        val width: Int?,
    ) {
        companion object {
            fun parse(obj: JSONObject): AdaptiveFormat {
                return AdaptiveFormat(
                    approxDurationMs = obj.getString("approxDurationMs"),
                    audioChannels = obj.getIntOrNull("audioChannels"),
                    audioQuality = obj.getStringOrNull("audioQuality"),
                    audioSampleRate = obj.getStringOrNull("audioSampleRate"),
                    averageBitrate = obj.getInt("averageBitrate"),
                    bitrate = obj.getInt("bitrate"),
                    colorInfo = ColorInfo.parse(obj),
                    contentLength = obj.getString("contentLength"),
                    fps = obj.getIntOrNull("fps"),
                    height = obj.getIntOrNull("height"),
                    highReplication = false, //TODO chenlo check this field
                    indexRange = IndexRange.parse(obj),
                    initRange = InitRange.parse(obj),
                    itag = obj.getInt("itag"),
                    lastModified = obj.getString("lastModified"),
                    mimeType = obj.getString("mimeType"),
                    projectionType = obj.getString("projectionType"),
                    quality = obj.getString("quality"),
                    qualityLabel = obj.getStringOrNull("qualityLabel"),
                    type = null, //TODO chenlo check this field
                    url = obj.getString("url"),
                    width = obj.getIntOrNull("width"),
                )
            }
        }
    }

    private data class ColorInfo(
        val matrixCoefficients: String,
        val primaries: String,
        val transferCharacteristics: String,
    ) {
        companion object {
            fun parse(obj: JSONObject): ColorInfo? =
                if (obj.has("colorInfo"))
                    obj.getJSONObject("colorInfo").let {
                        ColorInfo(
                            matrixCoefficients = it.getString("matrixCoefficients"),
                            primaries = it.getString("matrixCoefficients"),
                            transferCharacteristics = it.getString("transferCharacteristics"),
                        )
                    }
                else null
        }
    }

    private data class IndexRange(
        val end: String,
        val start: String,
    ) {
        companion object {
            fun parse(obj: JSONObject): IndexRange? =
                if (obj.has("indexRange"))
                    obj.getJSONObject("indexRange").let {
                        IndexRange(
                            end = it.getString("end"),
                            start = it.getString("start"),
                        )
                    }
                else null
        }
    }

    private data class InitRange(
        val end: String,
        val start: String,
    ) {
        companion object {
            fun parse(obj: JSONObject): InitRange =
                obj.getJSONObject("initRange").let {
                    InitRange(
                        end = it.getString("end"),
                        start = it.getString("start"),
                    )
                }
        }
    }

    private fun JSONObject.getStringOrNull(name: String): String? =
        if (has(name)) getString(name) else null

    private fun JSONObject.getIntOrNull(name: String): Int? =
        if (has(name)) getInt(name) else null
}
