package com.xdustatom.auryxbrowser.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object NetworkUtils {

    data class ConnectivityReport(
        val state: String,
        val latencyMs: Long,
        val jitterMs: Long,
        val packetLossPercent: Int,
        val downloadMbps: Double
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()

    fun getConnectionType(context: Context): String {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "Not Connected"
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "Unknown"

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi‑Fi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> getCellularGeneration(context)
            else -> "Unknown"
        }
    }

    private fun getCellularGeneration(context: Context): String {
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            ?: return "Cellular"
        val networkType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            telephony.dataNetworkType
        } else {
            @Suppress("DEPRECATION")
            telephony.networkType
        }

        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            TelephonyManager.NETWORK_TYPE_LTE,
            TelephonyManager.NETWORK_TYPE_LTE_CA,
            TelephonyManager.NETWORK_TYPE_IWLAN -> "4G/4G+"
            TelephonyManager.NETWORK_TYPE_HSPAP,
            TelephonyManager.NETWORK_TYPE_UMTS,
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_EVDO_0,
            TelephonyManager.NETWORK_TYPE_EVDO_A,
            TelephonyManager.NETWORK_TYPE_EVDO_B -> "3G"
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_GPRS,
            TelephonyManager.NETWORK_TYPE_CDMA,
            TelephonyManager.NETWORK_TYPE_1xRTT,
            TelephonyManager.NETWORK_TYPE_IDEN,
            TelephonyManager.NETWORK_TYPE_GSM -> "2G"
            else -> "Cellular"
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun getPublicIp(): String = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.ipify.org")
                .build()
            client.newCall(request).execute().use { response ->
                response.body?.string()?.trim().orEmpty().ifBlank { "Unable to fetch" }
            }
        } catch (_: Exception) {
            "Unable to fetch"
        }
    }

    suspend fun measureLatency(): Long = withContext(Dispatchers.IO) {
        val values = mutableListOf<Long>()
        repeat(3) {
            val start = System.nanoTime()
            val ok = runCatching {
                val request = Request.Builder().url("https://www.google.com/generate_204").build()
                client.newCall(request).execute().use { it.isSuccessful }
            }.getOrDefault(false)
            if (ok) {
                values.add((System.nanoTime() - start) / 1_000_000)
            }
        }
        values.minOrNull() ?: -1L
    }

    suspend fun runConnectivityTest(): ConnectivityReport = withContext(Dispatchers.IO) {
        val pings = mutableListOf<Long>()
        repeat(5) {
            val start = System.nanoTime()
            val ok = runCatching {
                val request = Request.Builder().url("https://www.google.com/generate_204").build()
                client.newCall(request).execute().use { it.isSuccessful }
            }.getOrDefault(false)
            if (ok) {
                pings.add((System.nanoTime() - start) / 1_000_000)
            }
        }

        val packetLossPercent = ((5 - pings.size) * 100) / 5
        val latency = pings.minOrNull() ?: -1L
        val jitter = if (pings.size >= 2) {
            val sorted = pings.sorted()
            (sorted.last() - sorted.first())
        } else {
            -1L
        }

        val downloadMbps = runCatching {
            val request = Request.Builder().url("https://speed.cloudflare.com/__down?bytes=500000").build()
            val start = System.nanoTime()
            val bytes = client.newCall(request).execute().use { response ->
                response.body?.bytes()?.size ?: 0
            }
            val elapsedSec = (System.nanoTime() - start) / 1_000_000_000.0
            if (bytes > 0 && elapsedSec > 0) {
                ((bytes * 8) / elapsedSec) / 1_000_000.0
            } else {
                -1.0
            }
        }.getOrDefault(-1.0)

        val state = when {
            pings.isEmpty() -> "Offline"
            packetLossPercent >= 40 -> "Unstable"
            latency > 200 -> "Slow"
            else -> "Connected"
        }

        ConnectivityReport(
            state = state,
            latencyMs = latency,
            jitterMs = jitter,
            packetLossPercent = packetLossPercent,
            downloadMbps = downloadMbps
        )
    }

    fun getNetworkState(context: Context): String {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        return when {
            network == null -> "Disconnected"
            capabilities == null -> "Unknown"
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) -> "Connected"
            else -> "Connecting..."
        }
    }
}
