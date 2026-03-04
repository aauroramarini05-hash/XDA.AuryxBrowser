package com.xdustatom.auryxbrowser.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xdustatom.auryxbrowser.models.Bookmark
import com.xdustatom.auryxbrowser.models.DownloadItem
import com.xdustatom.auryxbrowser.models.HistoryItem

class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "auryx_prefs"
        private const val KEY_JAVASCRIPT_ENABLED = "javascript_enabled"
        private const val KEY_POPUPS_ENABLED = "popups_enabled"
        private const val KEY_DESKTOP_MODE = "desktop_mode"
        private const val KEY_SEARCH_ENGINE = "search_engine"
        private const val KEY_BOOKMARKS = "bookmarks"
        private const val KEY_HISTORY = "history"
        private const val KEY_DOWNLOADS = "downloads"
        private const val KEY_QUICK_ACCESS = "quick_access"
    }
    
    var isJavaScriptEnabled: Boolean
        get() = prefs.getBoolean(KEY_JAVASCRIPT_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_JAVASCRIPT_ENABLED, value).apply()
    
    var isPopupsEnabled: Boolean
        get() = prefs.getBoolean(KEY_POPUPS_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_POPUPS_ENABLED, value).apply()
    
    var isDesktopMode: Boolean
        get() = prefs.getBoolean(KEY_DESKTOP_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DESKTOP_MODE, value).apply()
    
    var searchEngine: String
        get() = prefs.getString(KEY_SEARCH_ENGINE, "DuckDuckGo") ?: "DuckDuckGo"
        set(value) = prefs.edit().putString(KEY_SEARCH_ENGINE, value).apply()
    
    fun getSearchUrl(query: String): String {
        return when (searchEngine) {
            "Google" -> "https://www.google.com/search?q=$query"
            "Bing" -> "https://www.bing.com/search?q=$query"
            "DuckDuckGo" -> "https://duckduckgo.com/?q=$query"
            "Yahoo" -> "https://search.yahoo.com/search?p=$query"
            else -> "https://duckduckgo.com/?q=$query"
        }
    }
    
    // Bookmarks
    fun getBookmarks(): MutableList<Bookmark> {
        val json = prefs.getString(KEY_BOOKMARKS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Bookmark>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }
    
    fun saveBookmarks(bookmarks: List<Bookmark>) {
        prefs.edit().putString(KEY_BOOKMARKS, gson.toJson(bookmarks)).apply()
    }
    
    fun addBookmark(bookmark: Bookmark) {
        val bookmarks = getBookmarks()
        if (bookmarks.none { it.url == bookmark.url }) {
            bookmarks.add(0, bookmark)
            saveBookmarks(bookmarks)
        }
    }
    
    fun removeBookmark(bookmarkId: String) {
        val bookmarks = getBookmarks()
        bookmarks.removeAll { it.id == bookmarkId }
        saveBookmarks(bookmarks)
    }
    
    fun isBookmarked(url: String): Boolean {
        return getBookmarks().any { it.url == url }
    }
    
    // History
    fun getHistory(): MutableList<HistoryItem> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<HistoryItem>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }
    
    fun saveHistory(history: List<HistoryItem>) {
        prefs.edit().putString(KEY_HISTORY, gson.toJson(history)).apply()
    }
    
    fun addHistoryItem(item: HistoryItem) {
        val history = getHistory()
        history.removeAll { it.url == item.url }
        history.add(0, item)
        if (history.size > 500) {
            history.subList(500, history.size).clear()
        }
        saveHistory(history)
    }
    
    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }
    
    // Downloads
    fun getDownloads(): MutableList<DownloadItem> {
        val json = prefs.getString(KEY_DOWNLOADS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<DownloadItem>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }
    
    fun saveDownloads(downloads: List<DownloadItem>) {
        prefs.edit().putString(KEY_DOWNLOADS, gson.toJson(downloads)).apply()
    }
    
    fun addDownload(download: DownloadItem) {
        val downloads = getDownloads()
        downloads.add(0, download)
        saveDownloads(downloads)
    }
    
    fun updateDownload(download: DownloadItem) {
        val downloads = getDownloads()
        val index = downloads.indexOfFirst { it.id == download.id }
        if (index >= 0) {
            downloads[index] = download
            saveDownloads(downloads)
        }
    }
    
    // Quick Access Sites
    fun getQuickAccessSites(): List<Bookmark> {
        val json = prefs.getString(KEY_QUICK_ACCESS, null)
        if (json == null) {
            return getDefaultQuickAccess()
        }
        val type = object : TypeToken<List<Bookmark>>() {}.type
        return gson.fromJson(json, type) ?: getDefaultQuickAccess()
    }
    
    private fun getDefaultQuickAccess(): List<Bookmark> {
        return listOf(
            Bookmark(url = "https://www.google.com", title = "Google"),
            Bookmark(url = "https://www.youtube.com", title = "YouTube"),
            Bookmark(url = "https://www.github.com", title = "GitHub"),
            Bookmark(url = "https://www.reddit.com", title = "Reddit"),
            Bookmark(url = "https://www.wikipedia.org", title = "Wikipedia"),
            Bookmark(url = "https://www.twitter.com", title = "Twitter")
        )
    }
    
    fun clearCache() {
        // Clear app-specific cache through WebView in activity
    }
}
