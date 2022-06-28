package com.firemaples.youtubeplayertest.ui.exoplayerwithinternalapi

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.firemaples.youtubeplayertest.databinding.FragmentExoPlayerWithInternalApiBinding
import com.firemaples.youtubeplayertest.utils.YoutubeUtils
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExoPlayerWithInternalAPIFragment : Fragment() {

    companion object {
        private val TAG = ExoPlayerWithInternalAPIFragment::class.java.simpleName
    }

    private var _binding: FragmentExoPlayerWithInternalApiBinding? = null
    private val binding: FragmentExoPlayerWithInternalApiBinding get() = _binding!!
    private val viewModel: ExoPlayerWithInternalAPIViewModel by viewModels()
    private val args: ExoPlayerWithInternalAPIFragmentArgs by navArgs()

    private var player: ExoPlayer? = null
    private var playOnResume: Boolean = false

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

        val videoId = YoutubeUtils.extractYoutubeVideoId(args.url) ?: return
        lifecycleScope.launch(Dispatchers.Main) {
            val mediaInfo = YoutubeMediaInfoRetriever.retrieve(requireContext(), videoId)

            Log.i(TAG, "mediaInfo: $mediaInfo")

            if (mediaInfo != null) {
                val item = mediaInfo.mediaList.filter { !it.isAdaptive }.maxBy { it.height ?: 0 }
                Log.i(TAG, "Selected media: $item")
                loadMedia(item.url)
            }
        }
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
        val player = ExoPlayer.Builder(requireContext()).build()
        this@ExoPlayerWithInternalAPIFragment.player = player
        binding.exoPlayer.player = player
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
}
