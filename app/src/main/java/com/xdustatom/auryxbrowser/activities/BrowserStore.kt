package com.xdustatom.auryxbrowser.activities

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BrowserStore(ctx: Context) {

    private val prefs = ctx.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val KEY_BOOKMARKS = "bookmarks_json"
    private val KEY_HISTORY = "history_json"

    private fun readList(key: String): MutableList<String> {
        val raw = prefs.getString(key, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<String>>() {}.type
        return runCatching {
            gson.fromJson<MutableList<String>>(raw, type)
        }.getOrElse {
            mutableListOf()
        }
    }

    private fun writeList(key: String, values: List<String>) {
        prefs.edit().putString(key, gson.toJson(values)).apply()
    }

    fun getBookmarks(): List<String> = readList(KEY_BOOKMARKS).distinct()

    fun addBookmark(url: String) {
        val clean = url.trim()
        if (!isStorableUrl(clean)) return

        val current = readList(KEY_BOOKMARKS)
        current.removeAll { it == clean }
        current.add(0, clean)
        writeList(KEY_BOOKMARKS, current.take(200))
    }

    fun removeBookmark(url: String) {
        val current = readList(KEY_BOOKMARKS)
        current.removeAll { it == url }
        writeList(KEY_BOOKMARKS, current)
    }

    fun clearBookmarks() {
        prefs.edit().remove(KEY_BOOKMARKS).apply()
    }

    fun getHistory(): List<String> = readList(KEY_HISTORY)

    fun addHistory(url: String) {
        val clean = url.trim()
        if (!isStorableUrl(clean)) return

        val current = readList(KEY_HISTORY)
        current.removeAll { it == clean }
        current.add(0, clean)
        writeList(KEY_HISTORY, current.take(300))
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun isStorableUrl(url: String): Boolean {
        return url.startsWith("https://") || url.startsWith("http://")
    }
}
