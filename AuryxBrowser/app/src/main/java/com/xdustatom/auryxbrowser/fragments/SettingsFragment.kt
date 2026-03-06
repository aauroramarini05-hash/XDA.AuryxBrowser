package com.xdustatom.auryxbrowser.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.xdustatom.auryxbrowser.R
import com.xdustatom.auryxbrowser.activities.BrowserStore
import com.xdustatom.auryxbrowser.activities.MainActivity

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }

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
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveSettings)
        val btnClearHistory = view.findViewById<MaterialButton>(R.id.btnClearHistorySettings)
        val btnClearBookmarks = view.findViewById<MaterialButton>(R.id.btnClearBookmarksSettings)

        val searchEngines = listOf("DuckDuckGo", "Google", "Bing")
        val languages = listOf("System Default", "English", "Italiano")

        dropSearchEngine.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, searchEngines)
        )
        dropLanguage.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, languages)
        )

        dropSearchEngine.keyListener = null
        dropLanguage.keyListener = null

        dropSearchEngine.setOnClickListener { dropSearchEngine.showDropDown() }
        dropLanguage.setOnClickListener { dropLanguage.showDropDown() }

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

        dropLanguage.setText(
            prefs.getString(MainActivity.KEY_APP_LANGUAGE, "System Default") ?: "System Default",
            false
        )

        btnSave.setOnClickListener {
            val home = etHome.text?.toString()?.trim().orEmpty()
            val selectedSearchEngine = dropSearchEngine.text?.toString()?.trim().orEmpty()
            val selectedLanguage = dropLanguage.text?.toString()?.trim().orEmpty()

            prefs.edit()
                .putBoolean(MainActivity.KEY_DESKTOP_MODE, swDesktop.isChecked)
                .putBoolean(MainActivity.KEY_JAVASCRIPT_ENABLED, swJavascript.isChecked)
                .putBoolean(MainActivity.KEY_LOAD_IMAGES, swLoadImages.isChecked)
                .putString(
                    MainActivity.KEY_HOME,
                    if (home.isBlank()) MainActivity.DEFAULT_HOME else home
                )
                .putString(
                    MainActivity.KEY_SEARCH_ENGINE,
                    if (selectedSearchEngine in searchEngines) selectedSearchEngine else "DuckDuckGo"
                )
                .putString(
                    MainActivity.KEY_APP_LANGUAGE,
                    if (selectedLanguage in languages) selectedLanguage else "System Default"
                )
                .apply()

            applyLanguage(
                if (selectedLanguage in languages) selectedLanguage else "System Default"
            )

            Toast.makeText(requireContext(), "Settings saved", Toast.LENGTH_SHORT).show()
            activity?.recreate()
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

    private fun applyLanguage(language: String) {
        val locales = when (language) {
            "English" -> LocaleListCompat.forLanguageTags("en")
            "Italiano" -> LocaleListCompat.forLanguageTags("it")
            else -> LocaleListCompat.getEmptyLocaleList()
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
