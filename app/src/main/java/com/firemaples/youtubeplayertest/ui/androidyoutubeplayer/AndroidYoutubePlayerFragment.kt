package com.firemaples.youtubeplayertest.ui.androidyoutubeplayer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.firemaples.youtubeplayertest.R
import com.firemaples.youtubeplayertest.databinding.FragmentAndroidYoutubePlayerBinding
import com.firemaples.youtubeplayertest.utils.Utils
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.utils.TimeUtilities

/**
 * ## Official site
 *
 * [https://pierfrancescosoffritti.github.io/android-youtube-player/](https://pierfrancescosoffritti.github.io/android-youtube-player/)
 * [https://github.com/PierfrancescoSoffritti/android-youtube-player](https://github.com/PierfrancescoSoffritti/android-youtube-player)
 *
 * [https://www.appbrain.com/stats/libraries/details/android_youtube_player/android-youtube-player](https://www.appbrain.com/stats/libraries/details/android_youtube_player/android-youtube-player)
 */
class AndroidYoutubePlayerFragment : Fragment() {

    companion object {
        private val TAG = AndroidYoutubePlayerFragment::class.java.simpleName
    }

    private val viewModel: AndroidYoutubePlayerViewModel by viewModels()

    private var _binding: FragmentAndroidYoutubePlayerBinding? = null
    private val binding: FragmentAndroidYoutubePlayerBinding get() = _binding!!

    private val args: AndroidYoutubePlayerFragmentArgs by navArgs()

    private var currentSec: Float = 0f
    private var durationSec: Float = -1f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentAndroidYoutubePlayerBinding.inflate(inflater, container, false).also {
            _binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.player.setupPlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun YouTubePlayerView.setupPlayer() {
        lifecycle.addObserver(this)

        inflateCustomPlayerUi(R.layout.view_player_control)

        enableAutomaticInitialization = false

        val options = IFramePlayerOptions.Builder()
            .controls(0)
            .ivLoadPolicy(3)
            .ccLoadPolicy(0)
            .rel(0)
            .build()
        initialize(listener, options)

//        this.enableAutomaticInitialization = true
//        this.addYouTubePlayerListener(listener)
    }

    private val listener = object : YouTubePlayerListener {
        override fun onReady(youTubePlayer: YouTubePlayer) {
            setEvent("onReady")
            onPlayerReady(youTubePlayer)
        }

        override fun onStateChange(
            youTubePlayer: YouTubePlayer,
            state: PlayerConstants.PlayerState
        ) {
            binding.state.text = "onStateChange: $state"
        }

        override fun onPlaybackQualityChange(
            youTubePlayer: YouTubePlayer,
            playbackQuality: PlayerConstants.PlaybackQuality
        ) {
            setEvent("onPlaybackQualityChange: $playbackQuality")
        }

        override fun onPlaybackRateChange(
            youTubePlayer: YouTubePlayer,
            playbackRate: PlayerConstants.PlaybackRate
        ) {
            setEvent("onPlaybackRateChange: $playbackRate")
        }

        override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
            setEvent("onError: $error")
        }

        override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
//                setEvent("onCurrentSecond: $second")
            binding.currentTime.text = TimeUtilities.formatTime(second)
            currentSec = second

            if (durationSec > 0 && currentSec >= durationSec - 1) {
                youTubePlayer.pause()
            }
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
            showPlaybackRateDialog {
                player.setPlaybackRate(it)
            }
        }
    }

    private fun showPlaybackRateDialog(onSelected: (PlayerConstants.PlaybackRate) -> Unit) {
        val values = PlayerConstants.PlaybackRate.values().drop(1)

        AlertDialog.Builder(requireActivity())
            .setItems(values.map { it.name }.toTypedArray()) { _, which ->
                onSelected.invoke(values[which])
            }
            .show()
    }
}
