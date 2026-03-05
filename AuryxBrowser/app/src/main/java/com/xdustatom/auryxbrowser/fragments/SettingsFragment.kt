package com.xdustatom.auryxbrowser.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.xdustatom.auryxbrowser.R
import com.xdustatom.auryxbrowser.activities.BrowserStore
import com.xdustatom.auryxbrowser.activities.MainActivity

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    companion object {
        fun newInstance() = SettingsFragment()
    }

    private lateinit var store: BrowserStore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        store = BrowserStore(requireContext())

        val prefs = requireContext().getSharedPreferences(MainActivity.PREFS, android.content.Context.MODE_PRIVATE)

        val swDesktop = view.findViewById<Switch>(R.id.swDesktopMode)
        val etHome = view.findViewById<EditText>(R.id.etHomeUrl)
        val btnSave = view.findViewById<Button>(R.id.btnSaveSettings)
        val btnClearHistory = view.findViewById<Button>(R.id.btnClearHistorySettings)
        val btnClearBookmarks = view.findViewById<Button>(R.id.btnClearBookmarksSettings)

        swDesktop.isChecked = prefs.getBoolean(MainActivity.KEY_DESKTOP_MODE, false)
        etHome.setText(prefs.getString(MainActivity.KEY_HOME, MainActivity.DEFAULT_HOME) ?: MainActivity.DEFAULT_HOME)

        btnSave.setOnClickListener {
            val home = etHome.text?.toString()?.trim().orEmpty()
            prefs.edit()
                .putBoolean(MainActivity.KEY_DESKTOP_MODE, swDesktop.isChecked)
                .putString(MainActivity.KEY_HOME, if (home.isBlank()) MainActivity.DEFAULT_HOME else home)
                .apply()

            Toast.makeText(requireContext(), "Saved. Restart app to apply UA mode.", Toast.LENGTH_SHORT).show()
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
}
