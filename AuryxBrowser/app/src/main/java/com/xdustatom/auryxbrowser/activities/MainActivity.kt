package com.xdustatom.auryxbrowser.activities

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xdustatom.auryxbrowser.R

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var currentSearch: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)

        setupWebView()

        webView.loadUrl("https://www.google.com")
    }

    private fun setupWebView() {

        val settings: WebSettings = webView.settings

        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadsImagesAutomatically = true
        settings.allowFileAccess = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true

        webView.webViewClient = WebViewClient()
    }

    fun openUrl(url: String) {

        var fixedUrl = url

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            fixedUrl = "https://www.google.com/search?q=$url"
        }

        webView.loadUrl(fixedUrl)
    }

    fun findInPage() {

        val input = EditText(this)

        AlertDialog.Builder(this)
            .setTitle("Find in Page")
            .setView(input)
            .setPositiveButton("Search") { _, _ ->

                currentSearch = input.text.toString()

                if (currentSearch.isNotEmpty()) {
                    webView.findAllAsync(currentSearch)
                    Toast.makeText(this, "Searching: $currentSearch", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun findNext() {
        webView.findNext(true)
    }

    fun findPrevious() {
        webView.findNext(false)
    }

    fun sharePage() {

        val intent = Intent(Intent.ACTION_SEND)

        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, webView.url)

        startActivity(Intent.createChooser(intent, "Share page"))
    }

    fun openExternal(url: String) {

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

        startActivity(intent)
    }

    override fun onBackPressed() {

        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
