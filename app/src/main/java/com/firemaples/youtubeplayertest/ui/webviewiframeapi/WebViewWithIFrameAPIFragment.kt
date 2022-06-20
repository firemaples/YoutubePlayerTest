package com.firemaples.youtubeplayertest.ui.webviewiframeapi

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.firemaples.iframeyoutubeplayer.player.*
import com.firemaples.youtubeplayertest.databinding.FragmentWebViewWithIframeApiBinding
import com.firemaples.youtubeplayertest.ui.webviewiframeapi.controller.YoutubeVideoController
import com.firemaples.youtubeplayertest.utils.YoutubeUtils
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.utils.TimeUtilities

class WebViewWithIFrameAPIFragment : Fragment() {

    companion object {
        private val TAG = WebViewWithIFrameAPIFragment::class.java.simpleName
    }

    private val viewModel: WebViewWithIFrameAPIViewModel by viewModels()

    private var _binding: FragmentWebViewWithIframeApiBinding? = null
    private val binding: FragmentWebViewWithIframeApiBinding get() = _binding!!

    private val args: WebViewWithIFrameAPIFragmentArgs by navArgs()

    private var availablePlaybackRate: FloatArray = floatArrayOf()
    private var rateAdapter: RateAdapter? = null
    private var baseUIFlag = -1

    private val controller: YoutubeVideoController by lazy {
        YoutubeVideoController(requireActivity(), lifecycleScope)
    }

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

        binding.enableControls.setOnCheckedChangeListener { _, isChecked ->
            binding.player.init(enableControls = isChecked) {
                enableCustomView(!isChecked)
                onPlayerReady(it)
            }
        }

        binding.player.addListener(listener)
        binding.player.init(
            enableControls = binding.enableControls.isChecked,
            hideExtraUI = true,
        ) {
            enableCustomView(!binding.enableControls.isChecked)
            controller.bindPlayer(it)
            onPlayerReady(it)
        }
    }

    private fun enableCustomView(enable: Boolean) {
        if (enable)
            binding.player.addCustomView(controller.getController())
        else binding.player.removeCustomView(controller.getController())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val landscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
        binding.portraitTools.isVisible = !landscape
        binding.player.updateLayoutParams<ViewGroup.LayoutParams> {
            height =
                if (landscape) ViewGroup.LayoutParams.MATCH_PARENT
                else ViewGroup.LayoutParams.WRAP_CONTENT
        }
        if (landscape) {
            enterFullScreen()
        } else {
            exitFullScreen()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.player.removeListener(listener)
        _binding = null
    }

    fun enterFullScreen() {
        if (baseUIFlag == -1) {
            baseUIFlag = requireActivity().window.decorView.systemUiVisibility
        }
        val flags = baseUIFlag or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        requireActivity().window.decorView.systemUiVisibility = flags
    }

    fun exitFullScreen() {
        if (baseUIFlag != -1) {
            requireActivity().window.decorView.systemUiVisibility = baseUIFlag
        }
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
            binding.state.text = state.name
//            binding.state.text = "onStateChange: $state"
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
        val videoId = YoutubeUtils.extractYoutubeVideoId(args.url) ?: return
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

        availablePlaybackRate = player.availablePlaybackRates

        rateAdapter = RateAdapter(player)
        with(binding.rateList) {
            adapter = rateAdapter
        }
    }

    private inner class RateAdapter(private val player: YouTubePlayer) :
        RecyclerView.Adapter<RateViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RateViewHolder =
            RateViewHolder(AppCompatButton(requireActivity()))

        override fun onBindViewHolder(holder: RateViewHolder, position: Int) {
            val rate = availablePlaybackRate[position]
            with(holder.button) {
                text = "Rate $rate"
                setOnClickListener {
                    player.setPlaybackRate(rate)
                }
            }
        }

        override fun getItemCount(): Int = availablePlaybackRate.size
    }

    class RateViewHolder(val button: Button) : RecyclerView.ViewHolder(button)
}
