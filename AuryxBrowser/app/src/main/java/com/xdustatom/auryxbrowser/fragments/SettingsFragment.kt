package com.xdustatom.auryxbrowser.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebStorage
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.xdustatom.auryxbrowser.AuryxApplication
import com.xdustatom.auryxbrowser.R
import com.xdustatom.auryxbrowser.databinding.FragmentSettingsBinding

class SettingsFragment(private val onSettingsChanged: () -> Unit) : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val prefs by lazy { AuryxApplication.instance.preferencesManager }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSettings()
    }

    private fun setupSettings() {
        // JavaScript toggle
        binding.switchJavascript.isChecked = prefs.isJavaScriptEnabled
        binding.switchJavascript.setOnCheckedChangeListener { _, isChecked ->
            prefs.isJavaScriptEnabled = isChecked
            onSettingsChanged()
        }

        // Popups toggle
        binding.switchPopups.isChecked = prefs.isPopupsEnabled
        binding.switchPopups.setOnCheckedChangeListener { _, isChecked ->
            prefs.isPopupsEnabled = isChecked
            onSettingsChanged()
        }

        // Desktop mode toggle
        binding.switchDesktopMode.isChecked = prefs.isDesktopMode
        binding.switchDesktopMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.isDesktopMode = isChecked
            onSettingsChanged()
        }

        // Search engine selection
        binding.tvCurrentSearchEngine.text = prefs.searchEngine
        binding.layoutSearchEngine.setOnClickListener {
            showSearchEngineDialog()
        }

        // Clear cache
        binding.btnClearCache.setOnClickListener {
            AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle("Clear Cache")
                .setMessage("Clear all cached data?")
                .setPositiveButton("Clear") { _, _ ->
                    WebStorage.getInstance().deleteAllData()
                    Toast.makeText(requireContext(), "Cache cleared", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Clear history
        binding.btnClearHistory.setOnClickListener {
            AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle("Clear History")
                .setMessage("Clear all browsing history?")
                .setPositiveButton("Clear") { _, _ ->
                    prefs.clearHistory()
                    Toast.makeText(requireContext(), "History cleared", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // App version
        binding.tvVersion.text = "v1.305.01"
        binding.tvAuthor.text = "xDustAtom"
    }

    private fun showSearchEngineDialog() {
        val engines = arrayOf("DuckDuckGo", "Google", "Bing", "Yahoo")
        val currentIndex = engines.indexOf(prefs.searchEngine).coerceAtLeast(0)

        AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Select Search Engine")
            .setSingleChoiceItems(engines, currentIndex) { dialog, which ->
                prefs.searchEngine = engines[which]
                binding.tvCurrentSearchEngine.text = engines[which]
                onSettingsChanged()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
