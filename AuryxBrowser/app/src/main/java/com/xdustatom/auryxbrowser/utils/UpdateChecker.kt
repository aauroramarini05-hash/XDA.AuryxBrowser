package com.xdustatom.auryxbrowser.utils

import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object UpdateChecker {

    private const val UPDATE_URL = "https://aauroramarini05-hash.github.io/XDA.AuryxBrowser/"
    private const val CURRENT_VERSION = "1.305.02"
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    data class UpdateResult(
        val hasUpdate: Boolean,
        val latestVersion: String?,
        val error: String? = null
    )

    suspend fun checkForUpdates(): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(UPDATE_URL)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext UpdateResult(false, null, "Failed to check for updates")
                }

                val html = response.body?.string() ?: ""
                val latestVersion = parseLatestVersion(html)

                if (latestVersion != null && isNewerVersion(latestVersion, CURRENT_VERSION)) {
                    return@withContext UpdateResult(true, latestVersion)
                }

                return@withContext UpdateResult(false, latestVersion ?: CURRENT_VERSION)
            }
        } catch (e: Exception) {
            return@withContext UpdateResult(false, null, "Error: ${e.message}")
        }
    }

    private fun parseLatestVersion(html: String): String? {
        // Look for APK filename pattern: AuryxBrowser-vX.X.X.apk
        val pattern = "AuryxBrowser-v([0-9]+\\.[0-9]+\\.[0-9]+)\\.apk".toRegex()
        val matches = pattern.findAll(html)
        
        var latestVersion: String? = null
        for (match in matches) {
            val version = match.groupValues[1]
            if (latestVersion == null || isNewerVersion(version, latestVersion)) {
                latestVersion = version
            }
        }
        
        return latestVersion
    }

    private fun isNewerVersion(newVersion: String, currentVersion: String): Boolean {
        val newParts = newVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val currentParts = currentVersion.split(".").map { it.toIntOrNull() ?: 0 }

        for (i in 0 until maxOf(newParts.size, currentParts.size)) {
            val newPart = newParts.getOrElse(i) { 0 }
            val currentPart = currentParts.getOrElse(i) { 0 }
            
            if (newPart > currentPart) return true
            if (newPart < currentPart) return false
        }
        
        return false
    }

    fun getDownloadUrl(): String = UPDATE_URL
    
    fun getCurrentVersion(): String = CURRENT_VERSION
}
