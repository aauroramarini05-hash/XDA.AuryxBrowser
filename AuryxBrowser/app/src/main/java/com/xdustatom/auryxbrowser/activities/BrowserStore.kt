package com.xdustatom.auryxbrowser.activities

import android.content.Context

class BrowserStore(ctx: Context) {

    private val prefs = ctx.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)

    private val KEY_BOOKMARKS = "bookmarks"
    private val KEY_HISTORY = "history"

    fun getBookmarks(): List<String> {
        return prefs.getStringSet(KEY_BOOKMARKS, emptySet())!!.toList().sorted()
    }

    fun addBookmark(url: String) {
        val clean = url.trim()
        if (clean.isBlank()) return
        val set = prefs.getStringSet(KEY_BOOKMARKS, emptySet())!!.toMutableSet()
        set.add(clean)
        prefs.edit().putStringSet(KEY_BOOKMARKS, set).apply()
    }

    fun removeBookmark(url: String) {
        val set = prefs.getStringSet(KEY_BOOKMARKS, emptySet())!!.toMutableSet()
        set.remove(url)
        prefs.edit().putStringSet(KEY_BOOKMARKS, set).apply()
    }

    fun clearBookmarks() {
        prefs.edit().remove(KEY_BOOKMARKS).apply()
    }

    fun getHistory(): List<String> {
        // Stored as newline string to preserve order
        val raw = prefs.getString(KEY_HISTORY, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split("\n").filter { it.isNotBlank() }
    }

    fun addHistory(url: String) {
        val clean = url.trim()
        if (clean.isBlank()) return

        val current = getHistory().toMutableList()

        // remove duplicates, keep most recent on top
        current.removeAll { it == clean }
        current.add(0, clean)

        // limit size
        val limited = current.take(200)

        prefs.edit().putString(KEY_HISTORY, limited.joinToString("\n")).apply()
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}
