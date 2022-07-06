package com.firemaples.youtubeplayertest.ui.exoplayerwithinternalapi

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.firemaples.youtubeplayertest.databinding.FragmentExoPlayerWithInternalApiBinding
import com.firemaples.youtubeplayertest.utils.YoutubeUtils
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.ExoTrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs

class ExoPlayerWithInternalAPIFragment : Fragment() {

    companion object {
        private val TAG = ExoPlayerWithInternalAPIFragment::class.java.simpleName

        private const val DEFAULT_WIDTH = 1280
    }

    private var _binding: FragmentExoPlayerWithInternalApiBinding? = null
    private val binding: FragmentExoPlayerWithInternalApiBinding get() = _binding!!
    private val viewModel: ExoPlayerWithInternalAPIViewModel by viewModels()
    private val args: ExoPlayerWithInternalAPIFragmentArgs by navArgs()

    private var player: ExoPlayer? = null
    private var playOnResume: Boolean = false

    private val playerListener = object : Player.Listener {
        private var playWhenReady: Boolean = false
        private var lastState: Int = Player.STATE_IDLE

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            super.onPlayWhenReadyChanged(playWhenReady, reason)
            Log.i(TAG, "onPlayWhenReadyChanged(), playWhenReady: $playWhenReady")
            this.playWhenReady = playWhenReady
            if (lastState == Player.STATE_READY) {
//                callback?.onPlayingStateUpdated(
//                    if (playWhenReady) PlayingState.PLAYING
//                    else PlayingState.PAUSED
//                )
            }
        }

        override fun onPlaybackStateChanged(state: Int) {
            super.onPlaybackStateChanged(state)
            lastState = state
            when (state) {
                Player.STATE_READY -> {
                    Log.i(TAG, "STATE_READY")
//                    callback?.onPlayingStateUpdated(
//                        if (playWhenReady) PlayingState.PLAYING
//                        else PlayingState.PAUSED
//                    )
                }
                Player.STATE_BUFFERING -> {
                    Log.i(TAG, "STATE_BUFFERING")
//                    callback?.onPlayingStateUpdated(PlayingState.BUFFERING)
                }
                Player.STATE_ENDED -> {
                    Log.i(TAG, "STATE_ENDED")
//                    callback?.onPlayingStateUpdated(PlayingState.ENDED)
                }
                Player.STATE_IDLE -> {
                    Log.i(TAG, "STATE_IDLE")
//                    callback
                }
                else -> {
                    Log.w(TAG, "Unknown state: $state")
                }
            }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            super.onPositionDiscontinuity(oldPosition, newPosition, reason)
            if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                Log.i(TAG, "Seek to ${newPosition.positionMs} ms")
//                callback?.onSeekTo(
//                    newPosition.positionMs.milliseconds.toDouble(DurationUnit.SECONDS).toFloat()
//                )
            }
        }

        override fun onTracksChanged(
            trackGroups: TrackGroupArray,
            trackSelections: TrackSelectionArray
        ) {
            super.onTracksChanged(trackGroups, trackSelections)
            val selectedTracks = (0 until trackSelections.length).mapNotNull { i ->
                val item = (trackSelections[i] as? ExoTrackSelection)
                if (item != null) {
                    val format = item.selectedFormat
                    val reason = when (item.selectionReason) {
                        C.SELECTION_REASON_ADAPTIVE -> "adaptive"
                        C.SELECTION_REASON_CUSTOM_BASE -> "custom_base"
                        C.SELECTION_REASON_INITIAL -> "initial"
                        C.SELECTION_REASON_MANUAL -> "manual"
                        C.SELECTION_REASON_TRICK_PLAY -> "track_play"
                        C.SELECTION_REASON_UNKNOWN -> "unknown"
                        else -> null
                    }
                    "$format, reason: $reason"
                } else null
            }
            Log.i(TAG, "onTrackChanged: $selectedTracks")
            binding.selectedTracks.text = selectedTracks.joinToString(separator = "\n")

            C.SELECTION_REASON_ADAPTIVE
        }

        override fun onPlayerError(error: ExoPlaybackException) {
//        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            val msg =
                error.localizedMessage ?: error.message ?: "Unknown error, code: ${error.type}"
//                error.localizedMessage ?: error.message ?: "Unknown error, code: ${error.errorCode}"
            Log.e(TAG, "onPlayerError: $msg", error)
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
//            callback?.onError(msg)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentExoPlayerWithInternalApiBinding.inflate(inflater, container, false).also {
            _binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPlayer()
        loadMedia()
        binding.sourceType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                loadMedia()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun loadMedia() {
        val sourceType: SourceType = SourceType.values()[binding.sourceType.selectedItemPosition]
        val videoId = YoutubeUtils.extractYoutubeVideoId(args.url) ?: return
        lifecycleScope.launch(Dispatchers.Main) {
            var title = ""
            when (sourceType) {
                SourceType.DashManifest -> {
                    val metadata = YoutubeDashManifestCreator.retrieve(requireContext(), videoId)
                    Log.i(TAG, "DashManifest: $metadata")
                    if (metadata != null) {
                        title = metadata.title
                        playWithDashManifest(metadata)
                    }
                }

                SourceType.HlsManifest -> {
                    val hlsMediaInfo = YoutubeHlsRetriever.retrieve(requireContext(), videoId)
                    Log.i(TAG, "HlsManifest: $hlsMediaInfo")
                    if (hlsMediaInfo != null) {
                        title = hlsMediaInfo.title
                        playWithHlsManifest(hlsMediaInfo)
                    }
                }

                SourceType.MediaSources -> {
                    val mediaInfo = YoutubeMediaInfoRetriever.retrieve(requireContext(), videoId)
                    Log.i(TAG, "MediaSources: $mediaInfo")
                    if (mediaInfo != null) {
                        title = mediaInfo.title
                        playWithMediaSource(mediaInfo)
                    }
                }
            }

            binding.title.text = "${sourceType.name}: $title"
        }
    }

    private fun playWithMediaSource(
        mediaInfo: YoutubeMediaInfoRetriever.MediaInfo,
        adaptiveOnly: Boolean = false
    ) {
        val nonAdaptive = mediaInfo.mediaList
            .filter { !it.isAdaptive && it.audioSampleRate != null && it.width != null }
            .minByOrNull { abs(DEFAULT_WIDTH - it.width!!) }
        if (nonAdaptive != null && !adaptiveOnly) {
            Log.i(TAG, "Selected default non adaptive media: $nonAdaptive")
            loadMedia(nonAdaptive.toMediaSource(mediaInfo.userAgent))
        } else {
            val allAudios = mediaInfo.mediaList
                .filter { it.isAdaptive && it.audioSampleRate != null && it.width == null }
            val allVideos = mediaInfo.mediaList
                .filter { it.isAdaptive && it.width != null && it.audioSampleRate == null }

            Log.i(TAG, "audio list: ${allAudios.map { it.itag }}")
            Log.i(TAG, "video list: ${allVideos.map { it.itag }}")

            val audio = mediaInfo.mediaList
                .filter { it.isAdaptive && it.audioSampleRate != null && it.width == null }
                .filter { it.mimeType.contains("webm", ignoreCase = true) }
                .maxByOrNull { it.audioSampleRate!! }
            val video = mediaInfo.mediaList
                .filter { it.isAdaptive && it.width != null && it.audioSampleRate == null }
                .minByOrNull { abs(DEFAULT_WIDTH - it.width!!) }
            if (audio != null && video != null) {
                Log.i(TAG, "Selected default adaptive audio: $audio")
                Log.i(TAG, "Selected default adaptive video: $video")

//                        val mediaItem = MediaItem.fromUri(audio.url)
//                        val manifestFactory = DefaultHttpDataSource.Factory()
//                        val sourceFactory =
//                            DefaultDashChunkSource.Factory(DefaultHttpDataSource.Factory())
//                        val audioSource = DashMediaSource.Factory(sourceFactory, manifestFactory)
//                            .createMediaSource(mediaItem)

//                        val audioSource = DashMediaSource.Factory(DefaultHttpDataSource.Factory())
//                            .createMediaSource(MediaItem.fromUri(audio.url))

//                        val videoSource = DashMediaSource
//                            .Factory(DefaultDataSource.Factory(requireContext()))
//                            .createMediaSource(
//                                MediaItem.fromUri(video.url)
////                                MediaItem.Builder()
////                                    .setUri(video.url)
////                                    .setMimeType(MimeTypes.getMediaMimeType(video.mimeType))
////                                    .build()
//                            )
//
//                        val audioSource = DashMediaSource
//                            .Factory(DefaultDataSource.Factory(requireContext()))
//                            .createMediaSource(
////                                MediaItem.fromUri(audio.url)
//                                MediaItem.Builder()
//                                    .setUri(audio.url)
////                                    .setMimeType(MimeTypes.getMediaMimeType(audio.mimeType))
//                                    .setMimeType(MimeTypes.APPLICATION_MPD)
//                                    .build()
//                            )
//
//                        MimeTypes.APPLICATION_MPD
//
//                        val source =
//                            MergingMediaSource(true, audioSource, videoSource)

                val source =
                    MergingMediaSource(
                        video.toMediaSource(mediaInfo.userAgent),
                        audio.toMediaSource(mediaInfo.userAgent),
                    )

//                        val source =
//                            MergingMediaSource(true, videoSource)

                loadMedia(source)
            } else {
                Log.w(TAG, "No suitable media source found")
            }
        }
    }

    private fun playWithDashManifest(metadata: YoutubeDashManifestCreator.YoutubeMetadata) {
        Log.i(TAG, "DASH manifest: ${metadata.dashManifest}")
        val file = File(requireContext().cacheDir, "video_manifest.xml")
        file.writeText(metadata.dashManifest)
        Log.i(TAG, "Dash manifest file(${file.length()}): ${file.absolutePath}")

        val uri = Uri.fromFile(file)
        val source = DashMediaSource
            .Factory(DefaultDataSourceFactory(requireContext(), metadata.userAgent))
            .createMediaSource(
                MediaItem.fromUri(uri)
            )

        loadMedia(source)
    }

    private fun playWithHlsManifest(hlsMediaInfo: YoutubeHlsRetriever.HlsMediaInfo) {
        Log.i(TAG, "HLS manifest: ${hlsMediaInfo.hlsManifestUrl}")

        val dataSource = DefaultHttpDataSource.Factory().setUserAgent(hlsMediaInfo.userAgent)
        val source = HlsMediaSource.Factory(dataSource)
            .createMediaSource(MediaItem.fromUri(hlsMediaInfo.hlsManifestUrl))

        loadMedia(source)
    }

    override fun onResume() {
        super.onResume()
        if (playOnResume) {
            player?.play()
        }
    }

    override fun onPause() {
        super.onPause()
        if (player?.isPlaying == true) {
            playOnResume = true
        }
        player?.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        player?.release()
        player = null
    }

    private fun setupPlayer() {
//        val player = ExoPlayer.Builder(requireContext()).build()
        val player = SimpleExoPlayer.Builder(requireContext()).build()
        this@ExoPlayerWithInternalAPIFragment.player = player
        binding.exoPlayer.player = player
        player.addListener(playerListener)
    }

    /**
     * https://stackoverflow.com/a/70552455/2906153
     */
    private fun loadMedia(url: String) {
        val player = player ?: return

        val mediaItem = MediaItem.fromUri(url)
//        player.setMediaItem(mediaItem)

        val mediaSource = ProgressiveMediaSource
            .Factory(DefaultHttpDataSource.Factory())
            .createMediaSource(mediaItem)

        player.setMediaSource(mediaSource)
        player.prepare()
        player.play()
    }

    private fun loadMedia(mediaSource: MediaSource) {
        val player = player ?: return

        player.setMediaSource(mediaSource, true)
        player.prepare()
        player.play()
    }

    private fun YoutubeMediaInfoRetriever.Media.toMediaSource(userAgent: String? = null): MediaSource =
        ProgressiveMediaSource
            .Factory(DefaultHttpDataSource.Factory().setUserAgent(userAgent))
            .createMediaSource(MediaItem.fromUri(url))

    enum class SourceType {
        DashManifest,
        HlsManifest,
        MediaSources,
    }
}
