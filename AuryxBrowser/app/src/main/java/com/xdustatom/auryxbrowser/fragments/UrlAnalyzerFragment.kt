package com.xdustatom.auryxbrowser.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.xdustatom.auryxbrowser.R
import com.xdustatom.auryxbrowser.activities.MainActivity

class UrlAnalyzerFragment : Fragment(R.layout.fragment_url_analyzer) {

    companion object {
        private const val ARG_URL = "arg_url"

        fun newInstance(url: String): UrlAnalyzerFragment {
            return UrlAnalyzerFragment().apply {
                arguments = Bundle().apply { putString(ARG_URL, url) }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val original = arguments?.getString(ARG_URL).orEmpty()
        val (cleanUrl, removed) = sanitizeUrl(original)

        view.findViewById<TextView>(R.id.tvOriginalUrl).text = original.ifBlank { "Nessun URL attivo" }
        view.findViewById<TextView>(R.id.tvCleanUrl).text = cleanUrl.ifBlank { "N/A" }
        view.findViewById<TextView>(R.id.tvRemovedTrackers).text = "Tracker rimossi: $removed"

        view.findViewById<MaterialButton>(R.id.btnOpenCleanUrl).setOnClickListener {
            if (cleanUrl.isBlank()) {
                Toast.makeText(requireContext(), "URL non disponibile", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            (activity as? MainActivity)?.performAssistantAction("open_url", cleanUrl)
            parentFragmentManager.popBackStack()
        }

        view.findViewById<MaterialButton>(R.id.btnCopyCleanUrl).setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("clean_url", cleanUrl))
            Toast.makeText(requireContext(), "URL pulito copiato", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sanitizeUrl(url: String): Pair<String, Int> {
        if (url.isBlank()) return "" to 0
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return url to 0

        val trackingParams = setOf(
            "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content",
            "gclid", "fbclid", "mc_cid", "mc_eid", "igshid", "ref", "spm"
        )

        val builder = uri.buildUpon().clearQuery()
        var removed = 0

        for (name in uri.queryParameterNames) {
            if (trackingParams.contains(name.lowercase())) {
                removed++
                continue
            }
            uri.getQueryParameters(name).forEach { value ->
                builder.appendQueryParameter(name, value)
            }
        }

        return builder.build().toString() to removed
    }
}
