package com.xdustatom.auryxbrowser.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.xdustatom.auryxbrowser.activities.MainActivity
import com.xdustatom.auryxbrowser.R
import com.xdustatom.auryxbrowser.databinding.FragmentPageInfoBinding

class PageInfoFragment : Fragment() {

    companion object {
        private const val ARG_URL = "arg_url"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_UA = "arg_ua"
        private const val ARG_LOAD_TIME = "arg_load_time"

        fun newInstance(
            pageUrl: String = "",
            pageTitle: String = "",
            userAgent: String = "",
            loadTime: Long = 0L
        ): PageInfoFragment {
            return PageInfoFragment().apply {
                arguments = bundleOf(
                    ARG_URL to pageUrl,
                    ARG_TITLE to pageTitle,
                    ARG_UA to userAgent,
                    ARG_LOAD_TIME to loadTime
                )
            }
        }
    }

    private var _binding: FragmentPageInfoBinding? = null
    private val binding get() = _binding!!

    private var pageUrl: String = ""
    private var pageTitle: String = ""
    private var userAgent: String = ""
    private var loadTime: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments
        pageUrl = args?.getString(ARG_URL).orEmpty()
        pageTitle = args?.getString(ARG_TITLE).orEmpty()
        userAgent = args?.getString(ARG_UA).orEmpty()
        loadTime = args?.getLong(ARG_LOAD_TIME, 0L) ?: 0L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPageInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayPageInfo()
    }

    private fun displayPageInfo() {
        if (pageUrl.isBlank()) {
            val act = activity as? MainActivity
            if (act != null) {
                pageUrl = act.getCurrentPageUrl()
                pageTitle = act.getCurrentPageTitle()
                userAgent = act.getCurrentPageUserAgent()
                loadTime = act.getCurrentPageLoadTimeMs()
            }
        }

        binding.tvPageUrl.text = pageUrl.ifEmpty { "No page loaded" }
        binding.tvPageTitle.text = pageTitle.ifEmpty { "Untitled" }

        val isHttps = pageUrl.startsWith("https://")
        binding.tvHttpsStatus.text = if (isHttps) "Secure (HTTPS)" else "Not Secure (HTTP)"
        binding.tvHttpsStatus.setTextColor(
            if (isHttps) ContextCompat.getColor(requireContext(), R.color.neon_green)
            else ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)
        )

        binding.tvLoadTime.text = if (loadTime > 0) "${loadTime}ms" else "N/A"
        binding.tvUserAgent.text = userAgent.ifEmpty { "Default" }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
