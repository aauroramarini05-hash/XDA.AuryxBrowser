package com.xdustatom.auryxbrowser.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.xdustatom.auryxbrowser.databinding.FragmentPageInfoBinding

class PageInfoFragment(
    private val pageUrl: String,
    private val pageTitle: String,
    private val userAgent: String,
    private val loadTime: Long
) : Fragment() {

    private var _binding: FragmentPageInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPageInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayPageInfo()
    }

    private fun displayPageInfo() {
        binding.tvPageUrl.text = pageUrl.ifEmpty { "No page loaded" }
        binding.tvPageTitle.text = pageTitle.ifEmpty { "Untitled" }
        
        val isHttps = pageUrl.startsWith("https://")
        binding.tvHttpsStatus.text = if (isHttps) "Secure (HTTPS)" else "Not Secure (HTTP)"
        binding.tvHttpsStatus.setTextColor(
            if (isHttps) resources.getColor(com.xdustatom.auryxbrowser.R.color.neon_green, null)
            else resources.getColor(android.R.color.holo_red_light, null)
        )
        
        binding.tvLoadTime.text = if (loadTime > 0) "${loadTime}ms" else "N/A"
        binding.tvUserAgent.text = userAgent.ifEmpty { "Default" }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
