package com.firemaples.youtubeplayertest.ui.webviewiframeapi.controller

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.firemaples.iframeyoutubeplayer.player.*
import com.firemaples.youtubeplayertest.R
import com.firemaples.youtubeplayertest.databinding.ItemYoutubePlayerRateBinding
import com.firemaples.youtubeplayertest.databinding.ViewYoutubePlayerControlBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class YoutubeVideoController(private val activity: Activity, private val scope: CoroutineScope) {
    companion object {
        private val TAG = YoutubeVideoController::class.java.simpleName

        private const val controlTimeout = 4_000L
    }

    private val binding: ViewYoutubePlayerControlBinding =
        ViewYoutubePlayerControlBinding.inflate(LayoutInflater.from(activity))

    private val hideableUIs = listOf(
        binding.btnPlay,
        binding.fullscreen,
        binding.rate,
        binding.seekBar,
        binding.controllerToolsBackground,
    )

    private var player: YouTubePlayer? = null
    private var isPlaying: Boolean = false
    private var totalSec: Float = -1f
    private var availablePlaybackRate: FloatArray = floatArrayOf()
    private var hideJob: Job? = null

    private val rateAdapter: RateAdapter by lazy {
        RateAdapter(
            onRateClicked = { rate ->
                player?.setPlaybackRate(rate)
                binding.rateList.isVisible = false
                rescheduleUIHide()
            })
    }

    private val youtubePlayerListener = object : YouTubePlayerListener {
        override fun onReady(youTubePlayer: YouTubePlayer) {
            availablePlaybackRate = youTubePlayer.availablePlaybackRates
            rateAdapter.notifyDataSetChanged()
        }

        override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerState) {
            onPlayerStateChanged(state)
        }

        override fun onPlaybackQualityChange(
            youTubePlayer: YouTubePlayer,
            playbackQuality: PlaybackQuality
        ) {

        }

        override fun onPlaybackRateChange(youTubePlayer: YouTubePlayer, playbackRate: Float) {
            onPlaybackRateChanged(playbackRate)
        }

        override fun onError(youTubePlayer: YouTubePlayer, error: PlayerError) {

        }

        override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
            onCurrentSecUpdated(second)
        }

        override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
            onTotalSecUpdated(duration)
        }

        override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {

        }

        override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {

        }

        override fun onApiChange(youTubePlayer: YouTubePlayer) {

        }

    }

    init {
        initUI()
    }

    fun getController(): View = binding.root

    fun bindPlayer(player: YouTubePlayer) {
        this.player = player
        player.addListener(youtubePlayerListener)
    }

    private fun initUI() {
        rescheduleUIHide()

        binding.root.setOnClickListener {
            val hide = hideableUIs[0].isVisible
            hideableUIs.forEach {
                it.isVisible = !hide
            }

            binding.rateList.isVisible = false

            if (!hide) {
                rescheduleUIHide()
            }
        }

        binding.btnPlay.setOnClickListener {
            if (isPlaying) {
                player?.pause()
            } else {
                player?.play()
            }
            rescheduleUIHide()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.i(TAG, "onProgressChanged(), $progress")
                if (fromUser) {
                    player?.seekTo(progress.toFloat())
                    rescheduleUIHide()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        binding.rate.setOnClickListener {
            binding.rateList.isVisible = !binding.rateList.isVisible
            rescheduleUIHide()
        }

        with(binding.rateList) {
            adapter = rateAdapter
        }

        binding.fullscreen.setOnClickListener {
            val landscape =
                activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            activity.requestedOrientation = if (landscape) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            rescheduleUIHide()
        }
    }

    private fun rescheduleUIHide() {
        hideJob?.cancel()
        hideJob = scope.launch {
            delay(controlTimeout)
            hideableUIs.forEach { it.isVisible = false }
        }
    }

    private fun onPlayerStateChanged(state: PlayerState) {
        when (state) {
            PlayerState.UNSTARTED,
            PlayerState.PAUSED,
            PlayerState.ENDED -> {
                isPlaying = false
            }
            PlayerState.PLAYING,
            PlayerState.BUFFERING -> {
                isPlaying = true
            }
            else -> {}
        }

        if (isPlaying) {
            binding.btnPlay.setImageResource(R.drawable.in_channel_pause)
        } else {
            binding.btnPlay.setImageResource(R.drawable.in_channel_play)
        }
    }

    private fun onCurrentSecUpdated(currentSecond: Float) {
        binding.seekBar.progress = currentSecond.toInt()
    }

    private fun onTotalSecUpdated(totalSec: Float) {
        this.totalSec = totalSec
        binding.seekBar.max = totalSec.toInt()
    }

    private fun onPlaybackRateChanged(rate: Float) {
        binding.rate.text = "Rate $rate"
    }

    private inner class RateAdapter(private val onRateClicked: (Float) -> Unit) :
        RecyclerView.Adapter<RateViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RateViewHolder =
            RateViewHolder(
                ItemYoutubePlayerRateBinding.inflate(
                    LayoutInflater.from(activity),
                    parent,
                    false
                )
            )

        override fun onBindViewHolder(holder: RateViewHolder, position: Int) {
            val rate = availablePlaybackRate[position]
            with(holder.binding.root) {
                text = "Rate $rate"
                setOnClickListener {
                    onRateClicked.invoke(rate)
                }
            }
        }

        override fun getItemCount(): Int = availablePlaybackRate.size
    }

    class RateViewHolder(val binding: ItemYoutubePlayerRateBinding) :
        RecyclerView.ViewHolder(binding.root)
}
