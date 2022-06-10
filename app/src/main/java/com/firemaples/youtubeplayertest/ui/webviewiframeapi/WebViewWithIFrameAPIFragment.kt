package com.firemaples.youtubeplayertest.ui.webviewiframeapi

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.firemaples.youtubeplayertest.databinding.FragmentWebViewWithIframeApiBinding
import com.firemaples.youtubeplayertest.ui.webviewiframeapi.player.*
import com.firemaples.youtubeplayertest.utils.Utils
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.utils.TimeUtilities

class WebViewWithIFrameAPIFragment : Fragment() {

    companion object {
        private val TAG = WebViewWithIFrameAPIFragment::class.java.simpleName
    }

    private val viewModel: WebViewWithIFrameAPIViewModel by viewModels()

    private var _binding: FragmentWebViewWithIframeApiBinding? = null
    private val binding: FragmentWebViewWithIframeApiBinding get() = _binding!!

    private val args: WebViewWithIFrameAPIFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentWebViewWithIframeApiBinding.inflate(layoutInflater, container, false).also {
            _binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        WebView.setWebContentsDebuggingEnabled(true)

        with(binding.player) {
            addListener(listener)
            init {
                onPlayerReady(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.player.removeListener(listener)
        _binding = null
    }

    private var currentSec: Float = 0f
    private var durationSec: Float = -1f

    private val listener = object : YouTubePlayerListener {
        override fun onReady(youTubePlayer: YouTubePlayer) {
            setEvent("onReady")
            onPlayerReady(youTubePlayer)
        }

        override fun onStateChange(
            youTubePlayer: YouTubePlayer,
            state: PlayerState
        ) {
            binding.state.text = "onStateChange: $state"
            Log.i(TAG, "onStateChange: $state")
        }

        override fun onPlaybackQualityChange(
            youTubePlayer: YouTubePlayer,
            playbackQuality: PlaybackQuality
        ) {
            setEvent("onPlaybackQualityChange: $playbackQuality")
        }

        override fun onPlaybackRateChange(
            youTubePlayer: YouTubePlayer, playbackRate: Float
        ) {
            setEvent("onPlaybackRateChange: $playbackRate")
        }

        override fun onError(youTubePlayer: YouTubePlayer, error: PlayerError) {
            setEvent("onError: $error")
        }

        override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
//                setEvent("onCurrentSecond: $second")
            binding.currentTime.text = TimeUtilities.formatTime(second)
            currentSec = second
        }

        override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
            setEvent("onVideoDuration: $duration")
            binding.totalTime.text = TimeUtilities.formatTime(duration)
            durationSec = duration
        }

        override fun onVideoLoadedFraction(
            youTubePlayer: YouTubePlayer,
            loadedFraction: Float
        ) {
            setEvent("onVideoLoadedFraction: $loadedFraction")
        }

        override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
            setEvent("onVideoId: $videoId")
        }

        override fun onApiChange(youTubePlayer: YouTubePlayer) {
            setEvent("onApiChange")
        }

        private fun setEvent(event: String) {
            binding.event.text = "State: $event"
            Log.i(TAG, "setEvent: $event")
        }
    }

    private fun onPlayerReady(player: YouTubePlayer) {
        val videoId = Utils.extractYoutubeVideoId(args.url) ?: return
        player.loadVideo(videoId, 0f)

        binding.play.setOnClickListener {
            player.play()
        }

        binding.pause.setOnClickListener {
            player.pause()
        }

        binding.prev10.setOnClickListener {
            player.seekTo(currentSec - 10f)
        }

        binding.next10.setOnClickListener {
            player.seekTo(currentSec + 10f)
        }

        binding.seekToMiddle.setOnClickListener {
            player.seekTo(durationSec / 2f)
        }

        binding.seekToEnd.setOnClickListener {
            player.seekTo(durationSec - 2f)
        }

        binding.setRate.setOnClickListener {
            showPlaybackRateDialog(binding.player.availablePlaybackRate) {
                player.setPlaybackRate(it)
            }
        }
    }

    private fun showPlaybackRateDialog(
        playbackRates: FloatArray,
        onSelected: (Float) -> Unit
    ) {
        AlertDialog.Builder(requireActivity())
            .setItems(playbackRates.map { it.toString() }.toTypedArray()) { _, which ->
                onSelected.invoke(playbackRates[which])
            }
            .show()
    }
}
