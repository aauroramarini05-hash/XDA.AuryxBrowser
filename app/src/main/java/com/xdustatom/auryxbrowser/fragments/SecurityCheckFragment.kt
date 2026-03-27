package com.xdustatom.auryxbrowser.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.xdustatom.auryxbrowser.R
import com.xdustatom.auryxbrowser.activities.MainActivity
import com.xdustatom.auryxbrowser.databinding.FragmentSecurityCheckBinding
import java.net.IDN
import java.net.URI

class SecurityCheckFragment : Fragment() {

    companion object {
        fun newInstance(): SecurityCheckFragment = SecurityCheckFragment()
    }

    private var _binding: FragmentSecurityCheckBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecurityCheckBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        renderSecurityAssessment()
    }

    private fun renderSecurityAssessment() {
        val act = activity as? MainActivity
        val url = act?.getCurrentPageUrl().orEmpty()
        val title = act?.getCurrentPageTitle().orEmpty()
        val report = assess(url)

        binding.tvPageTitle.text = title.ifBlank { "Untitled" }
        binding.tvPageUrl.text = url.ifBlank { "No active page" }
        binding.tvConnectionValue.text = report.connectionLabel
        binding.tvHostValue.text = report.hostLabel
        binding.tvRiskValue.text = report.riskLabel
        binding.tvAdviceValue.text = report.advice
        binding.tvWarningsValue.text = if (report.warnings.isEmpty()) {
            "No obvious red flags in the current URL."
        } else {
            report.warnings.joinToString(separator = "\n• ", prefix = "• ")
        }

        val riskColor = when (report.riskLevel) {
            RiskLevel.LOW -> ContextCompat.getColor(requireContext(), R.color.neon_green)
            RiskLevel.MEDIUM -> Color.parseColor("#FFC107")
            RiskLevel.HIGH -> ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)
        }
        binding.tvRiskValue.setTextColor(riskColor)
    }

    private fun assess(rawUrl: String): SecurityReport {
        if (rawUrl.isBlank()) {
            return SecurityReport(
                riskLevel = RiskLevel.MEDIUM,
                riskLabel = "No page loaded",
                connectionLabel = "Unavailable",
                hostLabel = "Unavailable",
                warnings = listOf("Open a page first to run a local security check."),
                advice = "Load a website, then reopen this tool to inspect the current address."
            )
        }

        val uri = runCatching { URI(rawUrl) }.getOrNull()
        val scheme = uri?.scheme?.lowercase().orEmpty()
        val host = uri?.host.orEmpty()
        val warnings = mutableListOf<String>()
        var score = 100

        if (scheme == "http") {
            score -= 35
            warnings += "This page is using cleartext HTTP instead of HTTPS."
        }

        if (scheme !in setOf("http", "https")) {
            score -= 45
            warnings += "The current page is using a non-standard scheme: ${scheme.ifBlank { "unknown" }}."
        }

        if (uri?.userInfo != null) {
            score -= 20
            warnings += "The URL contains embedded credentials before the host."
        }

        if (host.isBlank()) {
            score -= 20
            warnings += "The page host could not be parsed correctly."
        } else {
            if (host.startsWith("xn--", ignoreCase = true) || host.contains(".xn--", ignoreCase = true)) {
                score -= 20
                warnings += "The domain uses punycode. Double-check for lookalike domains."
            }

            if (isIpAddress(host)) {
                score -= 15
                warnings += "The address uses a raw IP instead of a domain name."
            }
        }

        val port = uri?.port ?: -1
        if (port != -1 && !((scheme == "https" && port == 443) || (scheme == "http" && port == 80))) {
            score -= 10
            warnings += "The site is using a non-standard port (${port})."
        }

        val unicodeHost = host.takeIf { it.isNotBlank() }?.let { runCatching { IDN.toUnicode(it) }.getOrDefault(it) }
        val connectionLabel = when (scheme) {
            "https" -> "Encrypted (HTTPS)"
            "http" -> "Insecure (HTTP)"
            else -> "Unsupported / custom"
        }

        val riskLevel = when {
            score >= 80 -> RiskLevel.LOW
            score >= 50 -> RiskLevel.MEDIUM
            else -> RiskLevel.HIGH
        }

        val advice = when (riskLevel) {
            RiskLevel.LOW -> "No clear URL-level red flags detected. Still verify the brand, permissions, and login prompts."
            RiskLevel.MEDIUM -> "Proceed carefully. Verify the domain and avoid entering passwords or payment data until you trust the site."
            RiskLevel.HIGH -> "Avoid entering sensitive data. Consider leaving the page unless you explicitly trust this destination."
        }

        return SecurityReport(
            riskLevel = riskLevel,
            riskLabel = when (riskLevel) {
                RiskLevel.LOW -> "Low risk"
                RiskLevel.MEDIUM -> "Needs attention"
                RiskLevel.HIGH -> "High risk"
            },
            connectionLabel = connectionLabel,
            hostLabel = unicodeHost ?: "Unavailable",
            warnings = warnings,
            advice = advice
        )
    }

    private fun isIpAddress(host: String): Boolean {
        val ipv4 = Regex("""^((25[0-5]|2[0-4]\d|1?\d?\d)\.){3}(25[0-5]|2[0-4]\d|1?\d?\d)$""")
        val ipv6 = Regex("""^[0-9a-fA-F:]+$""")
        val normalized = host.removePrefix("[").removeSuffix("]")
        return ipv4.matches(normalized) || (normalized.contains(':') && ipv6.matches(normalized))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private data class SecurityReport(
        val riskLevel: RiskLevel,
        val riskLabel: String,
        val connectionLabel: String,
        val hostLabel: String,
        val warnings: List<String>,
        val advice: String
    )

    private enum class RiskLevel { LOW, MEDIUM, HIGH }
}
