package com.xdustatom.auryxbrowser.activities

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.xdustatom.auryxbrowser.R
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    companion object {
        const val CURRENT_VERSION = "1.305.02"
        const val UPDATE_SITE = "https://aauroramarini05-hash.github.io/XDA.AuryxBrowser/"
        const val DEFAULT_HOME = "https://duckduckgo.com/"
    }

    private lateinit var urlBar: EditText
    private lateinit var btnRefresh: ImageButton
    private lateinit var btnMenu: ImageButton
    private lateinit var webView: WebView
    private lateinit var bottomNav: BottomNavigationView

    private var progressBar: android.widget.ProgressBar? = null

    // Containers in your activity_main.xml
    private lateinit var homeContainer: android.view.View
    private lateinit var webViewContainer: android.view.View
    private lateinit var fragmentContainer: android.view.View

    private var desktopModeEnabled = false

    // Find in page
    private var findQuery: String = ""
    private var findActive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setupTopButtons()
        setupBottomNav()
        setupWebView()
        setupUrlInputs()

        // Start in browser mode (home page) on first launch
        if (savedInstanceState == null) {
            showBrowser()
            loadUrl(DEFAULT_HOME)
        }
    }

    private fun bindViews() {
        urlBar = findViewById(R.id.urlBar)
        btnRefresh = findViewById(R.id.btnRefresh)
        btnMenu = findViewById(R.id.btnMenu)
        webView = findViewById(R.id.webView)
        bottomNav = findViewById(R.id.bottomNav)

        progressBar = runCatching { findViewById<android.widget.ProgressBar>(R.id.progressBar) }.getOrNull()

        homeContainer = findViewById(R.id.homeContainer)
        webViewContainer = findViewById(R.id.webViewContainer)
        fragmentContainer = findViewById(R.id.fragmentContainer)
    }

    private fun setupTopButtons() {
        btnRefresh.setOnClickListener {
            if (webViewContainer.isVisible) {
                webView.reload()
            } else {
                // If in another section, go back to browser
                showBrowser()
            }
        }

        btnMenu.setOnClickListener { anchor ->
            val popup = PopupMenu(this, anchor)
            popup.menuInflater.inflate(R.menu.browser_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                onOptionsItemSelected(item)
                true
            }
            popup.show()
        }
    }

    private fun setupBottomNav() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    showBrowser()
                    loadUrl(DEFAULT_HOME)
                    true
                }

                R.id.nav_bookmarks -> {
                    // If you don't have these fragments yet, we show the HomeContainer instead of lying.
                    // You can replace these with real fragments later.
                    showPlaceholder("Bookmarks not implemented yet")
                    true
                }

                R.id.nav_history -> {
                    showPlaceholder("History not implemented yet")
                    true
                }

                R.id.nav_tools -> {
                    // If AuryxToolsFragment exists, show it.
                    // Otherwise fallback to placeholder without toast spam.
                    if (classExists("com.xdustatom.auryxbrowser.fragments.AuryxToolsFragment")) {
                        showFragment("com.xdustatom.auryxbrowser.fragments.AuryxToolsFragment")
                    } else {
                        showPlaceholder("Tools not implemented yet")
                    }
                    true
                }

                R.id.nav_settings -> {
                    if (classExists("com.xdustatom.auryxbrowser.fragments.SettingsFragment")) {
                        showFragment("com.xdustatom.auryxbrowser.fragments.SettingsFragment")
                    } else {
                        showPlaceholder("Settings not implemented yet")
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun classExists(name: String): Boolean {
        return try {
            Class.forName(name)
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun showFragment(className: String) {
        try {
            val clazz = Class.forName(className)
            val fragment = clazz.newInstance() as androidx.fragment.app.Fragment

            homeContainer.isVisible = false
            webViewContainer.isVisible = false
            fragmentContainer.isVisible = true

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        } catch (_: Throwable) {
            showPlaceholder("Section error")
        }
    }

    private fun showBrowser() {
        fragmentContainer.isVisible = false
        homeContainer.isVisible = false
        webViewContainer.isVisible = true
    }

    private fun showPlaceholder(text: String) {
        // We use homeContainer as placeholder screen (no fake toast "Section not available")
        fragmentContainer.isVisible = false
        webViewContainer.isVisible = false
        homeContainer.isVisible = true
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        CookieManager.getInstance().setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        }

        val s: WebSettings = webView.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.databaseEnabled = true
        s.loadsImagesAutomatically = true
        s.useWideViewPort = true
        s.loadWithOverviewMode = true
        s.builtInZoomControls = true
        s.displayZoomControls = false
        s.setSupportZoom(true)
        s.mediaPlaybackRequiresUserGesture = false
        s.javaScriptCanOpenWindowsAutomatically = false
        s.cacheMode = WebSettings.LOAD_DEFAULT
        s.userAgentString = defaultUserAgent()

        webView.webChromeClient = object : WebChromeClient() {}

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                if (url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("sms:")) {
                    openExternal(url)
                    return true
                }
                return false
            }

            override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
                progressBar?.isVisible = true
                urlBar.setText(url)
                findActive = false
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView, url: String) {
                progressBar?.isVisible = false
                urlBar.setText(url)
                super.onPageFinished(view, url)
            }
        }
    }

    private fun setupUrlInputs() {
        urlBar.setOnEditorActionListener { _, actionId, event ->
            val isEnter =
                actionId == EditorInfo.IME_ACTION_GO ||
                    actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)

            if (isEnter) {
                val text = urlBar.text?.toString()?.trim().orEmpty()
                if (text.isNotEmpty()) {
                    showBrowser()
                    loadFromInput(text)
                }
                true
            } else false
        }
    }

    private fun defaultUserAgent(): String {
        val base = WebSettings.getDefaultUserAgent(this)
        return if (desktopModeEnabled) {
            base.replace("Mobile", "X11").replace("Android", "Linux")
        } else base
    }

    private fun loadFromInput(input: String) {
        val url = normalizeInputToUrl(input)
        loadUrl(url)
    }

    private fun normalizeInputToUrl(input: String): String {
        val trimmed = input.trim()

        val hasScheme = trimmed.startsWith("http://") || trimmed.startsWith("https://")
        if (hasScheme) return trimmed

        val looksLikeDomain = trimmed.contains(".") && !trimmed.contains(" ")
        if (looksLikeDomain) return "https://$trimmed"

        val q = URLEncoder.encode(trimmed, "UTF-8")
        return "https://duckduckgo.com/?q=$q"
    }

    private fun loadUrl(url: String) {
        webView.loadUrl(url)
    }

    private fun openExternal(url: String) {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }.onFailure {
            Toast.makeText(this, "Can't open external link", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.menu_refresh -> {
                showBrowser()
                webView.reload()
                true
            }

            R.id.menu_stop -> {
                webView.stopLoading()
                true
            }

            R.id.menu_share -> {
                shareCurrentPage()
                true
            }

            R.id.menu_copy_link -> {
                copyCurrentUrl()
                true
            }

            R.id.menu_desktop_mode -> {
                toggleDesktopMode()
                true
            }

            R.id.menu_find_in_page -> {
                showBrowser()
                showFindInPageDialog()
                true
            }

            R.id.menu_find_next -> {
                if (findActive) webView.findNext(true)
                true
            }

            R.id.menu_find_prev -> {
                if (findActive) webView.findNext(false)
                true
            }

            R.id.menu_check_updates -> {
                checkForUpdates()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun shareCurrentPage() {
        val url = webView.url ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        startActivity(Intent.createChooser(intent, "Share"))
    }

    private fun copyCurrentUrl() {
        val url = webView.url ?: return
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("url", url))
        Toast.makeText(this, "Copied link", Toast.LENGTH_SHORT).show()
    }

    private fun toggleDesktopMode() {
        desktopModeEnabled = !desktopModeEnabled
        webView.settings.userAgentString = defaultUserAgent()
        webView.reload()
        Toast.makeText(
            this,
            if (desktopModeEnabled) "Desktop mode ON" else "Desktop mode OFF",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showFindInPageDialog() {
        val input = EditText(this).apply {
            hint = "Search in page"
            setText(findQuery)
        }

        AlertDialog.Builder(this)
            .setTitle("Find in Page")
            .setView(input)
            .setPositiveButton("Find") { _, _ ->
                val q = input.text?.toString()?.trim().orEmpty()
                if (q.isEmpty()) {
                    Toast.makeText(this, "Empty query", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                findQuery = q
                findActive = true
                webView.findAllAsync(q)
                Toast.makeText(this, "Searching…", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Next") { _, _ ->
                if (findActive) webView.findNext(true)
            }
            .setNegativeButton("Prev") { _, _ ->
                if (findActive) webView.findNext(false)
            }
            .show()
    }

    private fun checkForUpdates() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Checking for updates…", Toast.LENGTH_SHORT).show()

        thread {
            try {
                val html = httpGetText(UPDATE_SITE)
                val latest = extractLatestVersionFromHtml(html) ?: ""

                runOnUiThread {
                    if (latest.isBlank()) {
                        Toast.makeText(this, "Can't detect latest version", Toast.LENGTH_LONG).show()
                        return@runOnUiThread
                    }

                    if (latest != CURRENT_VERSION) {
                        AlertDialog.Builder(this)
                            .setTitle("Update available")
                            .setMessage("New version: $latest\nCurrent: $CURRENT_VERSION")
                            .setPositiveButton("Open download page") { _, _ ->
                                openExternal(UPDATE_SITE)
                            }
                            .setNegativeButton("Later", null)
                            .show()
                    } else {
                        Toast.makeText(this, "You already have the latest version", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (_: Throwable) {
                runOnUiThread {
                    Toast.makeText(this, "Update check failed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun extractLatestVersionFromHtml(html: String): String? {
        val regex = Regex("""AuryxBrowser-v(\d+\.\d+\.\d+)\.apk""")
        val all = regex.findAll(html).map { it.groupValues[1] }.toList()
        if (all.isEmpty()) return null
        return all.maxWithOrNull { a, b -> compareVersions(a, b) }
    }

    private fun compareVersions(a: String, b: String): Int {
        val pa = a.split(".").mapNotNull { it.toIntOrNull() }
        val pb = b.split(".").mapNotNull { it.toIntOrNull() }
        val max = maxOf(pa.size, pb.size)
        for (i in 0 until max) {
            val ai = pa.getOrElse(i) { 0 }
            val bi = pb.getOrElse(i) { 0 }
            if (ai != bi) return ai.compareTo(bi)
        }
        return 0
    }

    private fun httpGetText(url: String): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8000
            readTimeout = 8000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "AuryxBrowser/$CURRENT_VERSION")
        }
        conn.inputStream.use { input ->
            BufferedReader(InputStreamReader(input)).use { br ->
                val sb = StringBuilder()
                var line: String?
                while (true) {
                    line = br.readLine() ?: break
                    sb.append(line).append('\n')
                }
                return sb.toString()
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun performAssistantAction(action: String, data: String?) {
        when (action) {
            "open_url" -> {
                val url = data?.trim().orEmpty()
                if (url.isNotEmpty()) {
                    showBrowser()
                    loadFromInput(url)
                }
            }
            "search" -> {
                val q = data?.trim().orEmpty()
                if (q.isNotEmpty()) {
                    showBrowser()
                    loadFromInput(q)
                }
            }
            "open_settings" -> bottomNav.selectedItemId = R.id.nav_settings
            "open_bookmarks" -> bottomNav.selectedItemId = R.id.nav_bookmarks
            "open_history" -> bottomNav.selectedItemId = R.id.nav_history
            "new_tab" -> {
                showBrowser()
                loadUrl(DEFAULT_HOME)
            }
            else -> Toast.makeText(this, "Unknown action: $action", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        if (findActive) {
            findActive = false
            webView.clearMatches()
            Toast.makeText(this, "Find cleared", Toast.LENGTH_SHORT).show()
            return
        }

        if (fragmentContainer.isVisible) {
            // If a fragment is open, go back to browser
            showBrowser()
            return
        }

        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
