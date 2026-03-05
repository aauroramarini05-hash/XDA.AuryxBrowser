package com.xdustatom.auryxbrowser

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    lateinit var webView: WebView

    val CURRENT_VERSION = "1.305.02"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        webView.webViewClient = WebViewClient()

        webView.loadUrl("https://www.google.com")
    }

    fun showFindDialog() {

        val input = EditText(this)

        AlertDialog.Builder(this)
            .setTitle("Find in Page")
            .setView(input)
            .setPositiveButton("Search") { _, _ ->

                val query = input.text.toString()

                webView.findAllAsync(query)

            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun checkUpdates() {

        Thread {

            try {

                val url = URL("https://TUOUSERNAME.github.io/TUAREPO/version.txt")

                val connection = url.openConnection() as HttpURLConnection

                val reader = BufferedReader(InputStreamReader(connection.inputStream))

                val latest = reader.readLine().trim()

                reader.close()

                runOnUiThread {

                    if (latest != CURRENT_VERSION) {

                        AlertDialog.Builder(this)
                            .setTitle("Update Available")
                            .setMessage("A new version is available: $latest")
                            .setPositiveButton("Update") { _, _ ->

                                val intent = Intent(Intent.ACTION_VIEW)

                                intent.data =
                                    Uri.parse("https://TUOUSERNAME.github.io/TUAREPO")

                                startActivity(intent)

                            }
                            .setNegativeButton("Later", null)
                            .show()

                    } else {

                        Toast.makeText(
                            this,
                            "You are using the latest version",
                            Toast.LENGTH_LONG
                        ).show()

                    }

                }

            } catch (e: Exception) {

                runOnUiThread {

                    Toast.makeText(
                        this,
                        "Update check failed",
                        Toast.LENGTH_LONG
                    ).show()

                }

            }

        }.start()

    }
}
