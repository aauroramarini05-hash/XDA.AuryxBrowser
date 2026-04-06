package com.xdustatom.auryxbrowser.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.button.MaterialButton
import com.xdustatom.auryxbrowser.BuildConfig
import com.xdustatom.auryxbrowser.R
import com.xdustatom.auryxbrowser.utils.SecureGeminiKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocalPulseFragment : Fragment(R.layout.fragment_local_pulse) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val httpClient = OkHttpClient()
    private var lastLocation: Location? = null
    private var mapsWebView: WebView? = null
    private var mapExpanded = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.any { it }) {
            detectLocation()
        } else {
            Toast.makeText(requireContext(), "Permesso posizione negato", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mapsWebView = view.findViewById<WebView>(R.id.wvMaps).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            settings.allowFileAccess = false
            settings.allowContentAccess = true
            settings.mediaPlaybackRequiresUserGesture = true
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    return handleSpecialUrl(view, request.url.toString())
                }

                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    return handleSpecialUrl(view, url)
                }
            }
            webChromeClient = WebChromeClient()
            loadUrl("https://www.google.com/maps?hl=it")
        }

        view.findViewById<MaterialButton>(R.id.btnDetectLocation).setOnClickListener { detectLocation() }
        view.findViewById<MaterialButton>(R.id.btnToggleMapSize).setOnClickListener {
            mapExpanded = !mapExpanded
            val newHeight = if (mapExpanded) {
                (resources.displayMetrics.heightPixels * 0.72f).toInt()
            } else {
                (320 * resources.displayMetrics.density).toInt()
            }
            mapsWebView?.layoutParams = mapsWebView?.layoutParams?.apply { height = newHeight }
            (it as MaterialButton).text = if (mapExpanded) "Riduci mappa" else "Espandi mappa"
        }
        view.findViewById<MaterialButton>(R.id.btnSearchRestaurants).setOnClickListener {
            openMapsSearchInApp("ristoranti vicino a me")
        }
        view.findViewById<MaterialButton>(R.id.btnSearchFuel).setOnClickListener {
            openMapsSearchInApp("distributori benzina vicino")
        }
        view.findViewById<MaterialButton>(R.id.btnSearchHotels).setOnClickListener {
            openMapsSearchInApp("hotel vicino")
        }
        view.findViewById<MaterialButton>(R.id.btnSearchPharmacies).setOnClickListener {
            openMapsSearchInApp("farmacia vicino")
        }

        view.findViewById<MaterialButton>(R.id.btnAskAuryxAi).setOnClickListener {
            val prompt = view.findViewById<EditText>(R.id.etAuryxAiPrompt).text.toString().trim()
            if (prompt.isBlank()) {
                Toast.makeText(requireContext(), "Scrivi una domanda per AuryxAI", Toast.LENGTH_SHORT).show()
            } else {
                askAuryxAi(prompt)
            }
        }
    }

    private fun detectLocation() {
        if (!hasLocationPermission()) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        val token = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, token.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    lastLocation = location
                    bindLocationStats(location)
                } else {
                    view?.findViewById<TextView>(R.id.tvLocationStatus)?.text = "Posizione non disponibile"
                }
            }
            .addOnFailureListener {
                view?.findViewById<TextView>(R.id.tvLocationStatus)?.text = "Errore GPS"
            }
    }

    private fun bindLocationStats(location: Location) {
        val date = Date(location.time)
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        view?.findViewById<TextView>(R.id.tvLocationStatus)?.text =
            "Posizione rilevata: %.5f, %.5f".format(location.latitude, location.longitude)

        view?.findViewById<TextView>(R.id.tvLocationStats)?.text =
            "Accuratezza: %.1fm • Velocità: %.1f m/s • Altitudine: %.1f m\nDirezione: %.1f° • Aggiornata: %s".format(
                location.accuracy,
                location.speed,
                location.altitude,
                location.bearing,
                formatter.format(date)
            )
    }

    private fun openMapsSearchInApp(query: String) {
        val loc = lastLocation
        val anchoredQuery = if (loc != null) "$query ${loc.latitude},${loc.longitude}" else query
        val encoded = URLEncoder.encode(anchoredQuery, "UTF-8")
        mapsWebView?.loadUrl("https://www.google.com/maps/search/$encoded?hl=it")
    }

    private fun askAuryxAi(userPrompt: String) {
        val outputView = view?.findViewById<TextView>(R.id.tvAuryxAiOutput) ?: return
        outputView.text = "AuryxAI sta analizzando la richiesta..."

        val apiKey = BuildConfig.GEMINI_API_KEY.ifBlank {
            SecureGeminiKeyStore.loadApiKey(requireContext())
        }
        if (apiKey.isBlank()) {
            outputView.text = "Chiave API Gemini non configurata. Imposta GEMINI_API_KEY o configura l'asset cifrato gemini.key.enc."
            return
        }

        val locationContext = lastLocation?.let {
            "Posizione attuale: lat ${it.latitude}, lon ${it.longitude}."
        } ?: "Posizione attuale non disponibile."

        val systemPrompt = """
            Sei AuryxAI, assistente avanzato per Google Maps.
            Fornisci consigli concreti per spostamenti, ricerca luoghi, ristoranti, hotel, distributori e punti utili.
            Se presenti coordinate, usale per suggerimenti in zona con priorità pratica.
            $locationContext
            Richiesta utente: $userPrompt
        """.trimIndent()

        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                val gemini = runCatching { requestGeminiAnswer(systemPrompt, apiKey) }
                    .getOrElse { "Errore AuryxAI: ${it.message ?: "sconosciuto"}" }
                if (gemini.startsWith("AuryxAI non disponibile (429)")) {
                    requestOpenStreetMapAdvice(userPrompt)
                } else {
                    gemini
                }
            }
            outputView.text = response
        }
    }

    private fun requestGeminiAnswer(prompt: String, apiKey: String): String {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"
        val body = JSONObject()
            .put("contents", JSONArray().put(
                JSONObject().put("parts", JSONArray().put(
                    JSONObject().put("text", prompt)
                ))
            ))

        val request = Request.Builder()
            .url(endpoint)
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        httpClient.newCall(request).execute().use { response ->
            val payload = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val parsedMessage = runCatching {
                    JSONObject(payload).optJSONObject("error")?.optString("message")
                }.getOrNull().orEmpty().ifBlank { "servizio momentaneamente non disponibile" }
                return "AuryxAI non disponibile (${response.code}): $parsedMessage"
            }

            val root = JSONObject(payload)
            val parts = root
                .optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?: return "AuryxAI non ha restituito testo utile."

            val text = buildString {
                for (i in 0 until parts.length()) {
                    append(parts.optJSONObject(i)?.optString("text").orEmpty())
                }
            }.trim()

            return if (text.isBlank()) "AuryxAI non ha restituito testo utile." else text
        }
    }

    private fun requestOpenStreetMapAdvice(userPrompt: String): String {
        val loc = lastLocation
        val query = URLEncoder.encode(userPrompt, "UTF-8")
        val bias = if (loc != null) "&lat=${loc.latitude}&lon=${loc.longitude}" else ""
        val url = "https://nominatim.openstreetmap.org/search?format=jsonv2&limit=5&q=$query$bias"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "AuryxBrowser/1.0 (AuryxAI-FreeFallback)")
            .build()

        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@use "AuryxAI Free fallback non disponibile (${response.code})."
                }
                val payload = response.body?.string().orEmpty()
                val results = JSONArray(payload)
                if (results.length() == 0) {
                    return@use "Nessun risultato in zona trovato con il fallback gratuito."
                }
                buildString {
                    append("AuryxAI Free (OSM) - risultati utili:\n")
                    for (i in 0 until minOf(5, results.length())) {
                        val item = results.optJSONObject(i) ?: continue
                        val name = item.optString("name").ifBlank { "Luogo" }
                        val address = item.optString("display_name")
                        append("${i + 1}. $name — $address\n")
                    }
                    append("\nSuggerimento: usa i pulsanti rapidi per aprire la ricerca direttamente nella mappa integrata.")
                }.trim()
            }
        }.getOrElse {
            "Fallback gratuito non disponibile: ${it.message ?: "errore sconosciuto"}"
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    private fun handleSpecialUrl(webView: WebView, rawUrl: String): Boolean {
        if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
            return false
        }

        if (rawUrl.startsWith("intent://")) {
            return runCatching {
                val intent = Intent.parseUri(rawUrl, Intent.URI_INTENT_SCHEME)
                val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                when {
                    !fallbackUrl.isNullOrBlank() -> {
                        webView.loadUrl(fallbackUrl)
                        true
                    }
                    intent.dataString?.startsWith("http") == true -> {
                        webView.loadUrl(intent.dataString!!)
                        true
                    }
                    else -> {
                        val reconstructed = rawUrl
                            .substringAfter("intent://")
                            .substringBefore("#Intent")
                        webView.loadUrl("https://$reconstructed")
                        true
                    }
                }
            }.getOrDefault(true)
        }

        if (rawUrl.startsWith("geo:")) {
            val query = Uri.parse(rawUrl).getQueryParameter("q").orEmpty()
            val encoded = URLEncoder.encode(query, "UTF-8")
            webView.loadUrl("https://www.google.com/maps/search/$encoded?hl=it")
            return true
        }

        Toast.makeText(requireContext(), "Link non supportato in modalità integrata", Toast.LENGTH_SHORT).show()
        return true
    }

    override fun onDestroyView() {
        mapsWebView?.destroy()
        mapsWebView = null
        super.onDestroyView()
    }
}
