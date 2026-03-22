package com.xdustatom.auryxbrowser.news

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class NewsRepository(context: Context) {

    companion object {
        private const val PREFS = "auryx_news_prefs"
        private const val KEY_CACHE = "news_cache"
        private const val NEWS_URL =
            "https://raw.githubusercontent.com/aauroramarini05-hash/XDA.AuryxBrowser/main/docs/news/index.json"
    }

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    fun getCachedFeed(): NewsFeedResponse? {
        val raw = prefs.getString(KEY_CACHE, null) ?: return null
        return runCatching {
            gson.fromJson(raw, NewsFeedResponse::class.java)
        }.getOrNull()
    }

    fun fetchRemoteFeed(): NewsFeedResponse? {
        val request = Request.Builder()
            .url(NEWS_URL)
            .header("User-Agent", "Mozilla/5.0")
            .header("Cache-Control", "no-cache")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                Log.d("AuryxNews", "HTTP code = ${response.code}")

                if (!response.isSuccessful) {
                    Log.e("AuryxNews", "Request failed with code ${response.code}")
                    return null
                }

                val body = response.body?.string().orEmpty()
                Log.d("AuryxNews", "Body length = ${body.length}")

                if (body.isBlank()) {
                    Log.e("AuryxNews", "Empty body")
                    return null
                }

                val parsed = gson.fromJson(body, NewsFeedResponse::class.java)
                prefs.edit().putString(KEY_CACHE, body).apply()
                parsed
            }
        } catch (e: Throwable) {
            Log.e("AuryxNews", "fetchRemoteFeed failed", e)
            null
        }
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
