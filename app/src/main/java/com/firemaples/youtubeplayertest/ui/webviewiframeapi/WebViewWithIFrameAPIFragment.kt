package com.firemaples.youtubeplayertest.ui.webviewiframeapi

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.firemaples.youtubeplayertest.R
import com.firemaples.youtubeplayertest.databinding.FragmentWebViewWithIframeApiBinding

class WebViewWithIFrameAPIFragment : Fragment() {

    private val viewModel: WebViewWithIFrameAPIViewModel by viewModels()

    private var _binding: FragmentWebViewWithIframeApiBinding? = null
    private val binding: FragmentWebViewWithIframeApiBinding get() = _binding!!

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

        binding.webView.init()
    }
}
