package com.firemaples.youtubeplayertest.ui.main

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.firemaples.youtubeplayertest.databinding.FragmentMainBinding
import java.net.URL

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentMainBinding.inflate(layoutInflater, container, false)
            .also {
                _binding = it
            }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.youtubeURL.editText?.setText("https://www.youtube.com/watch?v=U3DNz5asasA")

        binding.useYoutubeSDK.setOnClickListener {
            getUrl {
                findNavController()
                    .navigate(MainFragmentDirections.actionMainFragmentToYoutubeSDKFragment(it))
            }
        }

        binding.useAndroidYoutubePlayer.setOnClickListener {
            getUrl {
                findNavController()
                    .navigate(
                        MainFragmentDirections.actionMainFragmentToAndroidYoutubePlayerFragment(
                            it
                        )
                    )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getUrl(action: (String) -> Unit) {
        val url = binding.youtubeURL.editText?.text?.toString()
        if (!url.isNullOrBlank() && URLUtil.isNetworkUrl(url)) {
            action.invoke(url)
        }
    }
}
