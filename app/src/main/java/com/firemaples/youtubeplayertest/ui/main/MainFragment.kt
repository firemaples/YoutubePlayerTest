package com.firemaples.youtubeplayertest.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.firemaples.youtubeplayertest.databinding.FragmentMainBinding
import com.firemaples.youtubeplayertest.ui.youtubevideoselector.selector.YoutubeVideoSelectorActivity

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding get() = _binding!!

    private val getVideoIdResultForExoPlayerWithInternalAPI =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val videoInfo =
                YoutubeVideoSelectorActivity.getVideoInfo(it.data)
                    ?: return@registerForActivityResult
            val action = MainFragmentDirections
                .actionMainFragmentToExoPlayerWithInternalAPIFragment(videoInfo.url)
            findNavController().navigate(action)
        }

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
//        binding.youtubeURL.editText?.setText("https://www.youtube.com/watch?v=O3cUQrylUoo")
//        binding.youtubeURL.editText?.setText("https://www.youtube.com/watch?v=4MKtq_9n7RI")
        binding.youtubeURL.editText?.setText("https://www.youtube.com/watch?v=c3nPloFgHkM")

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

        binding.youtubeVideoSelectorForIframeAPI.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToYoutubeVideoSelectorFragment())
        }

        binding.useExoPlayerWithInternalAPI.setOnClickListener {
            getUrl {
                val action = MainFragmentDirections
                    .actionMainFragmentToExoPlayerWithInternalAPIFragment(it)
                findNavController().navigate(action)
            }
        }

        binding.youtubeVideoSelectorForInternalAPI.setOnClickListener {
            getVideoIdResultForExoPlayerWithInternalAPI
                .launch(YoutubeVideoSelectorActivity.getIntent(requireActivity()))
//            getUrl {
//                findNavController().navigate(
//                    MainFragmentDirections.actionMainFragmentToExoPlayerWithInternalAPIFragment(it)
//                )
//            }
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
