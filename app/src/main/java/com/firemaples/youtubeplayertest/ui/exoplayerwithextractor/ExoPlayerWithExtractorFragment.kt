package com.firemaples.youtubeplayertest.ui.exoplayerwithextractor

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.util.forEach
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.firemaples.youtubeplayertest.databinding.FragmentExoPlayerWithExtractorBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * ## Media file URL extractor
 *
 * [HaarigerHarald/android-youtubeExtractor](https://github.com/HaarigerHarald/android-youtubeExtractor)
 *
 * ## References
 *
 * - [https://stackoverflow.com/a/70552455/2906153](https://stackoverflow.com/a/70552455/2906153)
 * - [Can I play YouTube video in ExoPlayer](https://github.com/google/ExoPlayer/issues/5466)
 */
class ExoPlayerWithExtractorFragment : Fragment() {

    companion object {
        private val TAG = ExoPlayerWithExtractorFragment::class.java.simpleName
    }

    private val viewModel: ExoPlayerWithExtractorViewModel by viewModels()

    private var _binding: FragmentExoPlayerWithExtractorBinding? = null
    private val binding: FragmentExoPlayerWithExtractorBinding get() = _binding!!

    private val args: ExoPlayerWithExtractorFragmentArgs by navArgs()

    private var player: ExoPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentExoPlayerWithExtractorBinding.inflate(inflater, container, false).also {
            _binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val player = setPlayer()
        this.player = player
        lifecycleScope.launch {
            val (file, format) = extractVideo(args.url) ?: return@launch
            ensureActive()
            Log.i(TAG, "extractVideo: $file")
            file ?: return@launch

            Toast.makeText(requireContext(), "Select ${file.format}", Toast.LENGTH_SHORT).show()

            binding.format.text = format
            loadMedia(player, file.url)
        }
    }

    override fun onResume() {
        super.onResume()
        player?.play()
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        player?.release()
        player = null
    }

    private fun setPlayer(): ExoPlayer {
        val player = ExoPlayer.Builder(requireContext()).build()
        binding.exoPlayer.player = player

        return player
    }

    private fun loadMedia(player: ExoPlayer, url: String) {
        val mediaItem = MediaItem.fromUri(url)
//        player.setMediaItem(mediaItem)

        val mediaSource = ProgressiveMediaSource
            .Factory(DefaultHttpDataSource.Factory())
            .createMediaSource(mediaItem)

        player.setMediaSource(mediaSource)
        player.prepare()
        player.play()
    }

    @SuppressLint("StaticFieldLeak")
    private suspend fun extractVideo(url: String): Pair<YtFile?, String>? =
        suspendCoroutine { c ->
            object : YouTubeExtractor(requireActivity()) {
                override fun onExtractionComplete(
                    ytFiles: SparseArray<YtFile>?,
                    videoMeta: VideoMeta?
                ) {
                    if (ytFiles == null) {
                        c.resume(null)
                        return
                    }

                    var file: YtFile? = null
                    val stringBuilder = StringBuilder()

                    ytFiles.forEach { itag, ytFile ->
                        Log.i(TAG, "ytFile($itag): $ytFile")
                        if (file == null) {
                            file = ytFile
                        }

                        if (stringBuilder.isNotEmpty()) {
                            stringBuilder.append("\n\n")
                        }
                        stringBuilder.append(ytFile.format.toString())
                    }

                    c.resume(file to stringBuilder.toString())
                }

            }.extract(url)
        }
}
