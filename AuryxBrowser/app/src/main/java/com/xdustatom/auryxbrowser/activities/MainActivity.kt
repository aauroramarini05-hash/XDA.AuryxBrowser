package com.xdustatom.auryxbrowser.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import android.os.SystemClock
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.xdustatom.auryxbrowser.R
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.concurrent.thread

/**
 * AuryxBrowser MainActivity (v1.305.02)
 *
 * WHAT THIS FILE EXPECTS IN YOUR PROJECT (you can rename in code if different):
 * - layout: res/layout/activity_main.xml
 * - views:
 *   - WebView with id: webView
 *   - EditText/AutoCompleteTextView URL bar with id: urlBar
 *   - Toolbar with id: toolbar
 *   - BottomNavigationView with id: bottomNav
 *   - Optional progress view with id: progressBar (can be removed if not present)
 *
 * - bottom nav menu ids (from your bottom_nav_menu.xml):
 *   nav_home, nav_bookmarks, nav_history, nav_tools, nav_settings
 *
 * - Fragments (rename to match yours if different):
 *   HomeFragment, BookmarksFragment, HistoryFragment, ToolsFragment, SettingsFragment
 *
 * If your project uses different names, search/replace them.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        const val CURRENT_VERSION = "1.305.02"
        const val UPDATE_SITE = "https://aauroramarini05-hash.github.io/XDA.AuryxBrowser/"
        const val DEFAULT_HOME = "https://duckduckgo.com/"
    }

    // UI
    private lateinit var toolbar: Toolbar
    private lateinit var webView: WebView
    private lateinit var urlBar: EditText
    private lateinit var bottomNav: BottomNavigationView

    // Optional (safe)
    private var progressBar: View? = null

    // State
    private var pageLoadStartMs: Long = 0L
    private var desktopModeEnabled: Boolean = false

    // Find in page state
    private var findQuery: String = ""
    private var findActive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Must exist in your project. If your layout name differs, change it here.
        setContentView(R.layout.activity_main)

        bindViews()
        setupToolbar()
        setupBottomNav()
        setupWebView()
        setupUrlBar()

        // Load default page only on fresh start
        if (savedInstanceState == null) {
            loadUrl(DEFAULT_HOME)
            // If you have a HomeFragment UI, you can also show it
            // switchToFragment(HomeFragment())
        }
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        webView = findViewById(R.id.webView)
        urlBar = findViewById(R.id.urlBar)
        bottomNav = findViewById(R.id.bottomNav)

        // Optional: only if it exists
        progressBar = runCatching { findViewById<View>(R.id.progressBar) }.getOrNull()
        progressBar?.isVisible = false
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
    }

    private fun setupBottomNav() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // If you have a home fragment, show it. Otherwise just load home page.
                    loadUrl(DEFAULT_HOME)
                    true
                }
                R.id.nav_bookmarks -> {
                    // Replace with your actual fragment class if different
                    switchToFragmentSafe("com.xdustatom.auryxbrowser.fragments.BookmarksFragment")
                    true
                }
                R.id.nav_history -> {
                    switchToFragmentSafe("com.xdustatom.auryxbrowser.fragments.HistoryFragment")
                    true
                }
                R.id.nav_tools -> {
                    switchToFragmentSafe("com.xdustatom.auryxbrowser.fragments.ToolsFragment")
                    true
                }
                R.id.nav_settings -> {
                    switchToFragmentSafe("com.xdustatom.auryxbrowser.fragments.SettingsFragment")
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Safe fragment switching without hard compile-time dependency on exact class names.
     * This avoids breaking compilation if your fragments have different names.
     * If you know the exact fragment classes, you can replace this with direct references.
     */
    private fun switchToFragmentSafe(className: String) {
        try {
            val clazz = Class.forName(className)
            val fragment = clazz.newInstance() as Fragment
            switchToFragment(fragment)
        } catch (_: Throwable) {
            Toast.makeText(this, "Section not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun switchToFragment(fragment: Fragment) {
        // Your app must have a container in activity_main.xml with id fragmentContainer
        val containerId = resources.getIdentifier("fragmentContainer", "id", packageName)
        if (containerId == 0) {
            // If you don't have fragments container, do nothing.
            return
        }
        supportFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .addToBackStack(null)
            .commit()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        // Cookies often needed for modern sites
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

        webView.webChromeClient = object : WebChromeClient() {
            // You can add progress updates here if you have a progress bar
        }

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()

                // Keep inside WebView by default
                // You can add special cases (tel:, mailto:, intent:) here
                if (url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("sms:")) {
                    openExternal(url)
                    return true
                }
                return false
            }

            override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
                pageLoadStartMs = SystemClock.elapsedRealtime()
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

    private fun setupUrlBar() {
        urlBar.setOnEditorActionListener { _: TextView, actionId: Int, event: KeyEvent? ->
            val isEnter =
                actionId == EditorInfo.IME_ACTION_GO ||
                actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)

            if (isEnter) {
                val text = urlBar.text?.toString()?.trim().orEmpty()
                if (text.isNotEmpty()) {
                    loadFromInput(text)
                }
                true
            } else {
                false
            }
        }
    }

    private fun defaultUserAgent(): String {
        val base = WebSettings.getDefaultUserAgent(this)
        return if (desktopModeEnabled) {
            // Simple desktop UA tweak
            base.replace("Mobile", "X11").replace("Android", "Linux")
        } else base
    }

    private fun loadFromInput(input: String) {
        val url = normalizeInputToUrl(input)
        loadUrl(url)
    }

    private fun normalizeInputToUrl(input: String): String {
        val trimmed = input.trim()

        // If looks like URL already
        val hasScheme = trimmed.startsWith("http://") || trimmed.startsWith("https://")
        if (hasScheme) return trimmed

        // If is domain-like, add https
        val looksLikeDomain = trimmed.contains(".") && !trimmed.contains(" ")
        if (looksLikeDomain) return "https://$trimmed"

        // Otherwise: search query
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

    // -------------------------
    // Options menu (⋮)
    // -------------------------

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // If you already have a menu resource, inflate it here.
        // Example: menuInflater.inflate(R.menu.browser_menu, menu)
        // If not, we still can work without it.
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            // If you have these ids in your menu XML, they will work.
            R.id.menu_refresh -> {
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
        Toast.makeText(this, if (desktopModeEnabled) "Desktop mode ON" else "Desktop mode OFF", Toast.LENGTH_SHORT).show()
    }

    // -------------------------
    // Find in Page (REAL)
    // -------------------------

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

    // -------------------------
    // Update checker (NO backend)
    // Strategy:
    // - Fetch the GitHub Pages HTML
    // - Find newest "AuryxBrowser-vX.Y.ZZ.apk" (simple regex)
    // - Compare to CURRENT_VERSION
    // - If different -> open site
    // -------------------------

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
        // matches: AuryxBrowser-v1.305.01.apk OR AuryxBrowser-v1.305.02.apk etc.
        val regex = Regex("""AuryxBrowser-v(\d+\.\d+\.\d+)\.apk""")
        val all = regex.findAll(html).map { it.groupValues[1] }.toList()
        if (all.isEmpty()) return null
        // simple numeric compare: split by dots
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

    // -------------------------
    // Back navigation
    // -------------------------

    override fun onBackPressed() {
        // Close find mode first (optional)
        if (findActive) {
            findActive = false
            webView.clearMatches()
            Toast.makeText(this, "Find cleared", Toast.LENGTH_SHORT).show()
            return
        }

        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
