package com.xdustatom.auryxbrowser.remote

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class RemoteConfigRepository(
    context: Context
) {
    companion object {
        private const val PREFS_REMOTE = "auryx_remote_config"
        private const val KEY_CACHED_JSON = "cached_json"
        private const val ENDPOINT =
            "https://aauroramarini05-hash.github.io/XDA.AuryxBrowser/features.json"
    }

    private val prefs = context.getSharedPreferences(PREFS_REMOTE, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val client = OkHttpClient()

    fun cached(): RemoteFeatureConfig {
        val raw = prefs.getString(KEY_CACHED_JSON, null) ?: return RemoteFeatureConfig()
        return runCatching {
            gson.fromJson(raw, RemoteFeatureConfig::class.java)
        }.getOrElse { RemoteFeatureConfig() }
    }

    suspend fun refresh(): RemoteFeatureConfig = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(ENDPOINT)
            .header("Cache-Control", "no-cache")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            return@withContext cached()
        }

        val body = response.body?.string().orEmpty()
        if (body.isBlank()) {
            return@withContext cached()
        }

        prefs.edit().putString(KEY_CACHED_JSON, body).apply()
        return@withContext runCatching {
            gson.fromJson(body, RemoteFeatureConfig::class.java)
        }.getOrElse { cached() }
    }
}
