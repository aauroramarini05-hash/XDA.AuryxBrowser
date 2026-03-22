package com.xdustatom.auryxbrowser.news

import android.content.Context
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request

class NewsRepository(context: Context) {

    companion object {
        private const val PREFS = "auryx_news_prefs"
        private const val KEY_CACHE = "news_cache"
        private const val NEWS_URL =
            "https://aauroramarini05-hash.github.io/XDA.AuryxBrowser/docs/news/index.json"
    }

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val client = OkHttpClient()

    fun getCachedFeed(): NewsFeedResponse? {
        val raw = prefs.getString(KEY_CACHE, null) ?: return null
        return runCatching {
            gson.fromJson(raw, NewsFeedResponse::class.java)
        }.getOrNull()
    }

    fun fetchRemoteFeed(): NewsFeedResponse? {
        val request = Request.Builder()
            .url(NEWS_URL)
            .header("Cache-Control", "no-cache")
            .build()

        return runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) return null

                prefs.edit().putString(KEY_CACHE, body).apply()
                gson.fromJson(body, NewsFeedResponse::class.java)
            }
        }.getOrNull()
    }

    fun filter(
        items: List<NewsItem>,
        country: String,
        category: String
    ): List<NewsItem> {
        return items.filter { item ->
            val countryOk = country == "all" || item.country.equals(country, ignoreCase = true)
            val categoryOk = category == "all" || item.category.equals(category, ignoreCase = true)
            countryOk && categoryOk
        }
    }
}
