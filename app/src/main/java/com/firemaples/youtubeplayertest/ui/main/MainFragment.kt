package com.firemaples.youtubeplayertest.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.firemaples.youtubeplayertest.databinding.FragmentMainBinding

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

//        binding.youtubeURL.editText?.setText("https://www.youtube.com/watch?v=U3DNz5asasA")
        binding.youtubeURL.editText?.setText("https://www.youtube.com/watch?v=O3cUQrylUoo")

        binding.useYoutubeSDK.setOnClickListener {
            getUrl {
                findNavController()
                    .navigate(MainFragmentDirections.actionMainFragmentToYoutubeSDKFragment(it))
            }
        }

        binding.useAndroidYoutubePlayer.setOnClickListener {
            getUrl {
                val action = MainFragmentDirections
                    .actionMainFragmentToAndroidYoutubePlayerFragment(it)
                findNavController().navigate(action)
            }
        }

        binding.useExoPlayerWithUrlExtractor.setOnClickListener {
            getUrl {
                findNavController()
                    .navigate(
                        MainFragmentDirections.actionMainFragmentToExoPlayerWithExtractorFragment(it)
                    )
            }
        }

        binding.useWebViewWithIframeAPI.setOnClickListener {
            getUrl {
                findNavController()
                    .navigate(
                        MainFragmentDirections.actionMainFragmentToWebViewWithIFrameAPIFragment(it)
                    )
            }
        }

        binding.useWebViewWithMobileYoutube.setOnClickListener {
            getUrl {
                findNavController()
                    .navigate(
                        MainFragmentDirections.actionMainFragmentToWebViewMobileYoutubeFragment(it)
                    )
            }
        }

        binding.youtubeVideoSelector.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToYoutubeVideoSelectorFragment())
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
