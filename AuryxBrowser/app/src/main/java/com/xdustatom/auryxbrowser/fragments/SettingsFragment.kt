package com.xdustatom.auryxbrowser.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.xdustatom.auryxbrowser.BuildConfig
import com.xdustatom.auryxbrowser.R
import com.xdustatom.auryxbrowser.activities.BrowserStore
import com.xdustatom.auryxbrowser.activities.MainActivity
import com.xdustatom.auryxbrowser.utils.LocaleHelper

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }

    private data class LanguageOption(
        val label: String,
        val languageTag: String
    )

    private lateinit var store: BrowserStore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        store = BrowserStore(requireContext())

        val prefs = requireContext().getSharedPreferences(
            MainActivity.PREFS,
            Context.MODE_PRIVATE
        )

        val swDesktop = view.findViewById<SwitchMaterial>(R.id.swDesktopMode)
        val swJavascript = view.findViewById<SwitchMaterial>(R.id.swJavascript)
        val swLoadImages = view.findViewById<SwitchMaterial>(R.id.swLoadImages)
        val etHome = view.findViewById<TextInputEditText>(R.id.etHomeUrl)
        val dropSearchEngine = view.findViewById<AutoCompleteTextView>(R.id.dropSearchEngine)
        val dropLanguage = view.findViewById<AutoCompleteTextView>(R.id.dropLanguage)
        val dropAccentTheme = view.findViewById<AutoCompleteTextView>(R.id.dropAccentTheme)
        val dropPerformanceMode = view.findViewById<AutoCompleteTextView>(R.id.dropPerformanceMode)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveSettings)
        val btnClearHistory = view.findViewById<MaterialButton>(R.id.btnClearHistorySettings)
        val btnClearBookmarks = view.findViewById<MaterialButton>(R.id.btnClearBookmarksSettings)
        val tvVersion = view.findViewById<TextView>(R.id.tvVersion)

        val searchEngines = listOf("DuckDuckGo", "Google", "Bing")
        val languageOptions = listOf(
            LanguageOption("System Default", ""),
            LanguageOption("English", "en"),
            LanguageOption("Italiano", "it"),
            LanguageOption("Español", "es"),
            LanguageOption("Français", "fr"),
            LanguageOption("Deutsch", "de")
        )
        val languageLabels = languageOptions.map { it.label }
        val accentOptions = listOf("Neon Green", "Ocean Blue", "Sunset Orange", "Purple", "Gradient Aurora", "Gradient Magma")
        val performanceModes = listOf("Balanced", "Boost", "Eco RAM")

        dropSearchEngine.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, searchEngines)
        )
        dropLanguage.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, languageLabels)
        )
        dropAccentTheme.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, accentOptions)
        )
        dropPerformanceMode.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, performanceModes)
        )

        dropSearchEngine.keyListener = null
        dropLanguage.keyListener = null
        dropAccentTheme.keyListener = null
        dropPerformanceMode.keyListener = null

        dropSearchEngine.setOnClickListener { dropSearchEngine.showDropDown() }
        dropLanguage.setOnClickListener { dropLanguage.showDropDown() }
        dropAccentTheme.setOnClickListener { dropAccentTheme.showDropDown() }
        dropPerformanceMode.setOnClickListener { dropPerformanceMode.showDropDown() }

        swDesktop.isChecked = prefs.getBoolean(MainActivity.KEY_DESKTOP_MODE, false)
        swJavascript.isChecked = prefs.getBoolean(MainActivity.KEY_JAVASCRIPT_ENABLED, true)
        swLoadImages.isChecked = prefs.getBoolean(MainActivity.KEY_LOAD_IMAGES, true)

        etHome.setText(
            prefs.getString(MainActivity.KEY_HOME, MainActivity.DEFAULT_HOME)
                ?: MainActivity.DEFAULT_HOME
        )

        dropSearchEngine.setText(
            prefs.getString(MainActivity.KEY_SEARCH_ENGINE, "DuckDuckGo") ?: "DuckDuckGo",
            false
        )

        val savedLanguage = prefs.getString(MainActivity.KEY_APP_LANGUAGE, "") ?: ""
        val savedAccent = prefs.getString(MainActivity.KEY_THEME_ACCENT, "Neon Green") ?: "Neon Green"
        val savedPerformance = prefs.getString(MainActivity.KEY_PERFORMANCE_MODE, "Balanced") ?: "Balanced"
        val currentLanguageOption = languageOptions.firstOrNull {
            it.languageTag == savedLanguage || it.label == savedLanguage
        } ?: languageOptions.first()
        dropLanguage.setText(currentLanguageOption.label, false)
        dropAccentTheme.setText(savedAccent, false)
        dropPerformanceMode.setText(savedPerformance, false)

        tvVersion.text = "Version ${BuildConfig.VERSION_NAME}"

        btnSave.setOnClickListener {
            val home = etHome.text?.toString()?.trim().orEmpty()
            val selectedSearchEngine = dropSearchEngine.text?.toString()?.trim().orEmpty()
            val selectedLanguageLabel = dropLanguage.text?.toString()?.trim().orEmpty()
            val selectedAccent = dropAccentTheme.text?.toString()?.trim().orEmpty()
            val selectedPerformanceMode = dropPerformanceMode.text?.toString()?.trim().orEmpty()

            val safeSearchEngine =
                if (selectedSearchEngine in searchEngines) selectedSearchEngine else "DuckDuckGo"

            val safeLanguage = languageOptions.firstOrNull { it.label == selectedLanguageLabel }
                ?: languageOptions.first()
            val safeAccent = if (selectedAccent in accentOptions) selectedAccent else "Neon Green"
            val safePerformance = if (selectedPerformanceMode in performanceModes) selectedPerformanceMode else "Balanced"
            val normalizedHome = normalizeHomeUrl(home)

            prefs.edit()
                .putBoolean(MainActivity.KEY_DESKTOP_MODE, swDesktop.isChecked)
                .putBoolean(MainActivity.KEY_JAVASCRIPT_ENABLED, swJavascript.isChecked)
                .putBoolean(MainActivity.KEY_LOAD_IMAGES, swLoadImages.isChecked)
                .putString(
                    MainActivity.KEY_HOME,
                    normalizedHome
                )
                .putString(MainActivity.KEY_SEARCH_ENGINE, safeSearchEngine)
                .putString(MainActivity.KEY_APP_LANGUAGE, safeLanguage.languageTag)
                                .putString(MainActivity.KEY_THEME_ACCENT, safeAccent)
                .putString(MainActivity.KEY_PERFORMANCE_MODE, safePerformance)
                .apply()

            applyLanguage(safeLanguage.languageTag)
            LocaleHelper.setLanguage(requireContext(), safeLanguage.languageTag)

            Toast.makeText(requireContext(), "Settings saved", Toast.LENGTH_SHORT).show()
            requireActivity().recreate()
        }

        btnClearHistory.setOnClickListener {
            store.clearHistory()
            Toast.makeText(requireContext(), "History cleared", Toast.LENGTH_SHORT).show()
        }

        btnClearBookmarks.setOnClickListener {
            store.clearBookmarks()
            Toast.makeText(requireContext(), "Bookmarks cleared", Toast.LENGTH_SHORT).show()
        }
    }

    private fun normalizeHomeUrl(input: String): String {
        if (input.isBlank()) {
            return MainActivity.DEFAULT_HOME
        }
        val hasScheme = input.startsWith("http://") || input.startsWith("https://")
        return if (hasScheme) input else "https://$input"
    }

    private fun applyLanguage(languageTag: String) {
        val locales = if (languageTag.isBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageTag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
