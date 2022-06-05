package com.firemaples.youtubeplayertest.ui.youtubesdk

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.firemaples.youtubeplayertest.R
import com.firemaples.youtubeplayertest.databinding.FragmentYoutubeSdkBinding
import com.firemaples.youtubeplayertest.utils.Utils
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * ## References
 *
 * [https://developers.google.com/youtube/android/player](https://developers.google.com/youtube/android/player)
 * [https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/package-summary](https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/package-summary)
 *
 * ## Need to add following code in the gradle.properties
 *
 * ```
 * android.enableJetifier=true
 * ```
 *
 * ## Need to add following code in the AndroidManifest.xml
 *
 * ```
 * <uses-permission android:name="android.permission.INTERNET" />
 * <queries>
 *  <intent>
 *      <action android:name="com.google.android.youtube.api.service.START" />
 *  </intent>
 * </queries>
 * ```
 */
class YoutubeSDKFragment : Fragment() {

    companion object {
        private val TAG = YoutubeSDKFragment::class.java.simpleName

        private const val DEV_KEY =
            "145075750979-a8eb2a06llt94c85vpa7tb8qm93li7jl.apps.googleusercontent.com"
    }

    private val viewModel: YoutubeSDKViewModel by viewModels()
    private var _binding: FragmentYoutubeSdkBinding? = null
    private val binding: FragmentYoutubeSdkBinding get() = _binding!!
    private val args: YoutubeSDKFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentYoutubeSdkBinding.inflate(layoutInflater, container, false).also {
            _binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragment =
            childFragmentManager.findFragmentById(R.id.fragment_youtube_api_sdk) as? YouTubePlayerSupportFragment
        Log.i(TAG, "onViewCreated: $fragment")
        fragment?.initialize(DEV_KEY, object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(
                provider: YouTubePlayer.Provider?,
                player: YouTubePlayer?,
                wasRestored: Boolean
            ) {
                Log.i(TAG, "onInitializationSuccess: $provider, $player, $wasRestored")
                player?.setupPlayer()
                if (!wasRestored) {
                    player?.let { onInitialized(player) }
                }
            }

            override fun onInitializationFailure(
                provider: YouTubePlayer.Provider?,
                error: YouTubeInitializationResult?
            ) {
                Log.i(TAG, "onInitializationFailure: $provider, $error")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onInitialized(player: YouTubePlayer) {
        val videoId = Utils.extractYoutubeVideoId(args.url)
        Log.i(TAG, "onInitialized, url: ${args.url}, videoId: $videoId")
        if (videoId != null) {
            player.loadVideo(videoId)
        }
    }

    private fun YouTubePlayer.setupPlayer() {
        setManageAudioFocus(true)

        binding.play.setOnClickListener {
            play()
        }

        binding.pause.setOnClickListener {
            pause()
        }

        binding.prev10.setOnClickListener {
            seekRelativeMillis(-10.seconds.inWholeMilliseconds.toInt())
        }

        binding.next10.setOnClickListener {
            seekRelativeMillis(10.seconds.inWholeMilliseconds.toInt())
        }

        binding.seekToMiddle.setOnClickListener {
            seekToMillis(durationMillis / 2)
        }

        setPlayerStateChangeListener(object : YouTubePlayer.PlayerStateChangeListener {
            private var job: Job? = null

            override fun onLoading() {
                setState("onLoading")
                binding.currentTime.text = ""
                binding.totalTime.text = ""
            }

            override fun onLoaded(videoId: String?) {
                setState("onLoaded: $videoId")
                binding.totalTime.text = Utils.toMmSs(durationMillis.toLong())
            }

            override fun onAdStarted() {
                setState("onAdStarted")
            }

            override fun onVideoStarted() {
                setState("onVideoStarted")
                job = lifecycleScope.launch {
                    while (true) {
                        binding.currentTime.text = Utils.toMmSs(currentTimeMillis.toLong())
                        delay(500L)
                    }
                }
            }

            override fun onVideoEnded() {
                setState("onVideoEnded")
                job?.cancel()
            }

            override fun onError(reason: YouTubePlayer.ErrorReason?) {
                setState("onError: $reason")
            }

            private fun setState(state: String) {
                binding.state.text = "State: $state"
            }
        })

        setPlaybackEventListener(object : YouTubePlayer.PlaybackEventListener {
            override fun onPlaying() {
                setEvent("onPlaying")
            }

            override fun onPaused() {
                setEvent("onPaused")
            }

            override fun onStopped() {
                setEvent("onStopped")
            }

            override fun onBuffering(isBuffering: Boolean) {
                setEvent("onBuffering: $isBuffering")
            }

            override fun onSeekTo(newPositionMillis: Int) {
                setEvent("onSeekTo: $newPositionMillis ms")
            }

            private fun setEvent(event: String) {
                binding.event.text = "Event: $event"
            }
        })
    }
}
