package com.firemaples.youtubeplayertest.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.firemaples.youtubeplayertest.R
import com.firemaples.youtubeplayertest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
