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
import com.xdustatom.auryxbrowser.utils.LocaleHelper
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

        // Language selection
        binding.tvCurrentLanguage.text = LocaleHelper.getLanguageDisplayName(
            LocaleHelper.getLanguage(requireContext())
        )
        binding.layoutLanguage.setOnClickListener {
            showLanguageDialog()
        }

        // Clear cache
        binding.btnClearCache.setOnClickListener {
            AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle(R.string.settings_clear_cache)
                .setMessage(R.string.settings_clear_cache_confirm)
                .setPositiveButton(R.string.clear) { _, _ ->
                    WebStorage.getInstance().deleteAllData()
                    Toast.makeText(requireContext(), R.string.settings_cache_cleared, Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        // Clear history
        binding.btnClearHistory.setOnClickListener {
            AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle(R.string.settings_clear_history)
                .setMessage(R.string.settings_clear_history_confirm)
                .setPositiveButton(R.string.clear) { _, _ ->
                    prefs.clearHistory()
                    Toast.makeText(requireContext(), R.string.settings_history_cleared, Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.cancel, null)
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

    private fun showLanguageDialog() {
        val languageNames = LocaleHelper.supportedLanguages.map { it.nativeName }.toTypedArray()
        val currentIndex = LocaleHelper.getCurrentLanguageIndex(requireContext())

        AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
            .setTitle(R.string.settings_select_language)
            .setSingleChoiceItems(languageNames, currentIndex) { dialog, which ->
                val selectedLanguage = LocaleHelper.supportedLanguages[which]
                LocaleHelper.setLanguage(requireContext(), selectedLanguage.code)
                binding.tvCurrentLanguage.text = selectedLanguage.nativeName
                
                // Show restart message
                Toast.makeText(requireContext(), R.string.language_changed, Toast.LENGTH_LONG).show()
                
                dialog.dismiss()
                
                // Recreate activity to apply language change
                activity?.recreate()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun checkForUpdates() {
        binding.btnCheckUpdates.isEnabled = false
        Toast.makeText(requireContext(), R.string.settings_checking_updates, Toast.LENGTH_SHORT).show()

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
                        .setTitle(R.string.settings_up_to_date)
                        .setMessage(getString(R.string.settings_up_to_date_msg, UpdateChecker.getCurrentVersion()))
                        .setPositiveButton(R.string.ok, null)
                        .show()
                }
            }
        }
    }

    private fun showUpdateDialog(newVersion: String) {
        AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
            .setTitle(R.string.settings_update_available)
            .setMessage(getString(R.string.settings_update_available_msg, UpdateChecker.getCurrentVersion(), newVersion))
            .setPositiveButton(R.string.settings_download_update) { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(UpdateChecker.getDownloadUrl()))
                startActivity(intent)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showSearchEngineDialog() {
        val engines = arrayOf("DuckDuckGo", "Google", "Bing", "Yahoo")
        val currentIndex = engines.indexOf(prefs.searchEngine).coerceAtLeast(0)

        AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
            .setTitle(R.string.settings_select_search_engine)
            .setSingleChoiceItems(engines, currentIndex) { dialog, which ->
                prefs.searchEngine = engines[which]
                binding.tvCurrentSearchEngine.text = engines[which]
                onSettingsChanged()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
        _binding = null
    }
}
