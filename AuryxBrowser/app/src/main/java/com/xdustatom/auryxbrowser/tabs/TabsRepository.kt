package com.xdustatom.auryxbrowser.tabs

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TabsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("auryx_tabs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val KEY_TABS = "tabs_json"
    private val KEY_SELECTED_TAB_ID = "selected_tab_id"

    fun getTabs(): MutableList<BrowserTab> {
        val raw = prefs.getString(KEY_TABS, null) ?: return mutableListOf(defaultTab())
        val type = object : TypeToken<MutableList<BrowserTab>>() {}.type
        return runCatching {
            gson.fromJson<MutableList<BrowserTab>>(raw, type)
        }.getOrElse {
            mutableListOf(defaultTab())
        }.ifEmpty {
            mutableListOf(defaultTab())
        }
    }

    fun saveTabs(tabs: List<BrowserTab>) {
        prefs.edit().putString(KEY_TABS, gson.toJson(tabs)).apply()
    }

    fun getSelectedTabId(): Long {
        return prefs.getLong(KEY_SELECTED_TAB_ID, -1L)
    }

    fun saveSelectedTabId(id: Long) {
        prefs.edit().putLong(KEY_SELECTED_TAB_ID, id).apply()
    }

    private fun defaultTab(): BrowserTab {
        return BrowserTab(
            id = System.currentTimeMillis(),
            title = "Home",
            url = ""
        )
    }
}
