package com.xdustatom.auryxbrowser.utils

import android.content.Context
import android.util.Base64
import com.xdustatom.auryxbrowser.BuildConfig
import java.security.MessageDigest

object SecureGeminiKeyStore {

    private const val ENCRYPTED_ASSET_FILE = "gemini.key.enc"

    fun loadApiKey(context: Context): String {
        val fromBuildConfig = BuildConfig.GEMINI_API_KEY.trim()
        if (fromBuildConfig.isNotEmpty()) {
            return fromBuildConfig
        }

        return runCatching {
            val payload = context.assets.open(ENCRYPTED_ASSET_FILE)
                .bufferedReader()
                .use { it.readText().trim() }
            val encrypted = Base64.decode(payload, Base64.DEFAULT)
            val secret = MessageDigest.getInstance("SHA-256")
                .digest("${BuildConfig.APPLICATION_ID}|${BuildConfig.VERSION_NAME}|AuryxMapsSecureV1".toByteArray())
            val plain = ByteArray(encrypted.size)
            encrypted.indices.forEach { index ->
                plain[index] = (encrypted[index].toInt() xor secret[index % secret.size].toInt()).toByte()
            }
            String(plain).trim()
        }.getOrDefault("")
    }
}
