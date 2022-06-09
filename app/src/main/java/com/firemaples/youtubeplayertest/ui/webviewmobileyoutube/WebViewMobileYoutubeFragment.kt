package com.firemaples.youtubeplayertest.ui.webviewmobileyoutube

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.firemaples.youtubeplayertest.databinding.FragmentWebViewMobileYoutubeBinding
import com.firemaples.youtubeplayertest.utils.Utils
import java.util.*


class WebViewMobileYoutubeFragment : Fragment() {

    companion object {
        private val TAG = WebViewMobileYoutubeFragment::class.java.simpleName
    }

    private val viewModel: WebViewMobileYoutubeViewModel by viewModels()

    private var _binding: FragmentWebViewMobileYoutubeBinding? = null
    private val binding: FragmentWebViewMobileYoutubeBinding get() = _binding!!

    private val args: WebViewMobileYoutubeFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentWebViewMobileYoutubeBinding.inflate(layoutInflater, container, false).also {
            _binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val videoId = Utils.extractYoutubeVideoId(args.url) ?: return
        load(videoId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setWebView(){
        binding.webView.setBackgroundColor(0)
        val settings: WebSettings = binding.webView.getSettings()
        settings.javaScriptEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = false
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        settings.cacheMode = -1
        settings.domStorageEnabled = true
        binding.webView.setScrollBarStyle(33554432)
        binding.webView.setScrollbarFadingEnabled(true)
        binding.webView.setLayerType(2, null)
    }

    private fun load(videoId: String) {
        CookieManager.getInstance().setCookie(
            ".youtube.com",
            java.lang.String.format(
                Locale.ENGLISH,
                "CONSENT=YES+cb.20210328-17-p0.en+FX+%d; path=/",
                Integer.valueOf(Random().nextInt(899) + 100)
            )
        )

        val hashMap = HashMap<String, String>()
        hashMap["User-Agent"] =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3"

        binding.webView.loadUrl("https://m.youtube.com", hashMap)

        binding.webView.webViewClient = MyWebViewClient()
    }

    private val cssRules =
        ".menu-content,.scbrr-tabs,.search-bar,ytm-c4-tabbed-header-renderer,ytm-mobile-topbar-renderer,ytm-pivot-bar-renderer,ytm-select{background:rgba(0,0,0,.3)!important;-webkit-backdrop-filter:blur(16px)!important;backdrop-filter:blur(16px)!important}.compact-link-icon,.icon-button,.mobile-topbar-header[data-mode=searching],.mobile-topbar-title,.multi-page-menu-system-link,.pivot-bar-item-title,.searchbox-input.title,body,c3-icon#home-icon.mobile-topbar-logo.ringo-logo,html,ytm-app .secondary-text,ytm-app button,ytm-multi-page-menu-section-renderer,ytm-pivot-bar-renderer c3-icon,ytm-simple-menu-header-renderer{color:#fff!important;background:0 0!important}.rich-grid-renderer-header{display:none!important}.menu-content,.scbrr-tab,.scbrr-tabs,ytm-select{color:#fff!important}"

    private fun injectCSS(injectCss: Boolean) {
        try {
            if (activity == null) {
                return
            }
            val sb2 = StringBuilder()
            sb2.append("javascript:(function() {")
            sb2.append("var parent = document.getElementsByTagName('head').item(0);")
            if (injectCss) {
                sb2.append("var style = document.createElement('style');style.rel='stylesheet';")
                sb2.append("style.type = 'text/css';")
                sb2.append("style.innerHTML = window.atob('")
//                sb2.append(Base64.encodeToString(this.cssRules.getBytes(), 2))
                sb2.append(cssRules)
                sb2.append("');")
                sb2.append("parent.appendChild(style);")
            }
            sb2.append("})()")
//            val str = LOG_TAG
//            RaveLogging.v(str, "Injected CSS : " + this.cssRules)
            binding.webView.evaluateJavascript(sb2.toString(),
                ValueCallback<String> { obj ->
//                    // from class: com.wemesh.android.Fragments.VideoGridFragments.l
//                    // android.webkit.ValueCallback
//                    YoutubeVideoGridFragment.W(this@YoutubeVideoGridFragment, obj as String)
                })
        } catch (e: Exception) {
//            showNoVideosFoundImage()
            Log.i(TAG, "", e)
        }
    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

//            injectCSS(true)
        }
    }
}