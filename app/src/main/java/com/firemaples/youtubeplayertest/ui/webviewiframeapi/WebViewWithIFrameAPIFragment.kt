package com.firemaples.youtubeplayertest.ui.webviewiframeapi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.firemaples.youtubeplayertest.databinding.FragmentWebViewWithIframeApiBinding
import com.firemaples.youtubeplayertest.ui.webviewiframeapi.player.YouTubePlayer
import com.firemaples.youtubeplayertest.ui.webviewiframeapi.player.YouTubePlayerListener
import com.firemaples.youtubeplayertest.utils.Utils
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.utils.TimeUtilities

class WebViewWithIFrameAPIFragment : Fragment() {

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
        }

        override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
            binding.currentTime.text = TimeUtilities.formatTime(second)
            currentSec = second
        }

        override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
            binding.totalTime.text = TimeUtilities.formatTime(duration)
            durationSec = duration
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
            showPlaybackRateDialog {
                player.setPlaybackRate(it)
            }
        }
    }

    private fun showPlaybackRateDialog(onSelected: (Float) -> Unit) {
        val values: Array<Float> = arrayOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f)

        AlertDialog.Builder(requireActivity())
            .setItems(values.map { it.toString() }.toTypedArray()) { _, which ->
                onSelected.invoke(values[which])
            }
            .show()
    }
}
