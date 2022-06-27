package com.firemaples.youtubeplayertest.ui.youtubevideoselector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.firemaples.youtubeplayertest.databinding.FragmentYoutubeVideoSelectorBinding
import com.firemaples.youtubeplayertest.ui.youtubevideoselector.selector.YoutubeVideoSelectorActivity

class YoutubeVideoSelectorFragment : Fragment() {

    private val viewModel: YoutubeVideoSelectorViewModel by viewModels()
    private var _binding: FragmentYoutubeVideoSelectorBinding? = null
    private val binding: FragmentYoutubeVideoSelectorBinding get() = _binding!!

    private val getVideoIdResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val videoUrl =
                YoutubeVideoSelectorActivity.getVideoURL(it.data)
                    ?: return@registerForActivityResult
            val action = YoutubeVideoSelectorFragmentDirections
                .actionYoutubeVideoSelectorFragmentToWebViewWithIFrameAPIFragment(videoUrl)
            findNavController().navigate(action)
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentYoutubeVideoSelectorBinding.inflate(inflater, container, false).apply {
            _binding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectYoutubeVideo.setOnClickListener {
            getVideoIdResult.launch(YoutubeVideoSelectorActivity.getIntent(requireActivity()))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
