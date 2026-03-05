package com.xdustatom.auryxbrowser.fragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebStorage
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.xdustatom.auryxbrowser.AuryxApplication
import com.xdustatom.auryxbrowser.R
import com.xdustatom.auryxbrowser.databinding.FragmentSettingsBinding
import com.xdustatom.auryxbrowser.utils.UpdateChecker
import kotlinx.coroutines.*

class SettingsFragment(private val onSettingsChanged: () -> Unit) : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val prefs by lazy { AuryxApplication.instance.preferencesManager }
    private val scope = CoroutineScope(Dispatchers.Main + Job())

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

        // Check for updates
        binding.btnCheckUpdates.setOnClickListener {
            checkForUpdates()
        }

        // App version
        binding.tvVersion.text = "v${UpdateChecker.getCurrentVersion()}"
        binding.tvAuthor.text = "xDustAtom"
    }

    private fun checkForUpdates() {
        binding.btnCheckUpdates.isEnabled = false
        Toast.makeText(requireContext(), "Checking for updates...", Toast.LENGTH_SHORT).show()

        scope.launch {
            val result = UpdateChecker.checkForUpdates()

            if (!isAdded) return@launch

            binding.btnCheckUpdates.isEnabled = true

            when {
                result.error != null -> {
                    Toast.makeText(requireContext(), result.error, Toast.LENGTH_SHORT).show()
                }
                result.hasUpdate -> {
                    showUpdateDialog(result.latestVersion ?: "")
                }
                else -> {
                    AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                        .setTitle("Up to Date")
                        .setMessage("You have the latest version (${UpdateChecker.getCurrentVersion()})")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }
    }

    private fun showUpdateDialog(newVersion: String) {
        AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Update Available")
            .setMessage("A new version of AuryxBrowser is available.\n\nCurrent: ${UpdateChecker.getCurrentVersion()}\nLatest: $newVersion")
            .setPositiveButton("Download Update") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(UpdateChecker.getDownloadUrl()))
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
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
        scope.cancel()
        _binding = null
    }
}
