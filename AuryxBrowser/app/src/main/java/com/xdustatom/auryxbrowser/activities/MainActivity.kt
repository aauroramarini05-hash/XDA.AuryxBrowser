package com.xdustatom.auryxbrowser.activities

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.xdustatom.auryxbrowser.BuildConfig
import com.xdustatom.auryxbrowser.R
import com.xdustatom.auryxbrowser.fragments.AuryxToolsFragment
import com.xdustatom.auryxbrowser.fragments.BookmarksFragment
import com.xdustatom.auryxbrowser.fragments.HistoryFragment
import com.xdustatom.auryxbrowser.fragments.SettingsFragment
import com.xdustatom.auryxbrowser.remote.RemoteConfigRepository
import com.xdustatom.auryxbrowser.ui.animateEntrance
import com.xdustatom.auryxbrowser.ui.applyPressAnimation
import com.xdustatom.auryxbrowser.ui.crossfadeVisible
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    companion object {
        const val UPDATE_SITE = "https://auryxbrowser.it.uptodown.com/android"
        const val DEFAULT_HOME = "https://duckduckgo.com/"

        const val PREFS = "auryx_prefs"
        const val KEY_HOME = "home_url"
        const val KEY_DESKTOP_MODE = "desktop_mode"
        const val KEY_SEARCH_ENGINE = "search_engine"
        const val KEY_APP_LANGUAGE = "app_language"
        const val KEY_JAVASCRIPT_ENABLED = "javascript_enabled"
        const val KEY_LOAD_IMAGES = "load_images"

        private const val KEY_WEBVIEW_STATE = "webview_state"
    }

    private lateinit var urlBar: EditText
    private var homeSearchBar: EditText? = null
    private lateinit var btnRefresh: ImageButton
    private lateinit var btnMenu: ImageButton
    private var btnTabs: View? = null
    private var tabsCount: TextView? = null

    private lateinit var webView: WebView
    private lateinit var bottomNav: BottomNavigationView
    private var progressBar: ProgressBar? = null

    private lateinit var homeContainer: View
    private lateinit var webViewContainer: View
    private lateinit var fragmentContainer: View

    private var desktopModeEnabled = false
    private var isTvDevice = false

    private var findQuery: String = ""
    private var findActive: Boolean = false

    private lateinit var store: BrowserStore
    private lateinit var remoteConfigRepository: RemoteConfigRepository
    private var restoredWebState: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        store = BrowserStore(this)
        remoteConfigRepository = RemoteConfigRepository(this)
        restoredWebState = savedInstanceState?.getBundle(KEY_WEBVIEW_STATE)

        bindViews()

        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        desktopModeEnabled = prefs.getBoolean(KEY_DESKTOP_MODE, false)

        isTvDevice = isRunningOnTv()
        if (isTvDevice) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        setupTopButtons()
        setupBottomNav()
        setupWebView()
        setupUrlInputs()
        setupHomeSearch()
        optimizeForTv()
        attachMicroAnimations()

        lifecycleScope.launch {
            runCatching { remoteConfigRepository.refresh() }
        }

        if (savedInstanceState == null) {
            showBrowser()
            loadUrl(getHomeUrl())
        } else if (restoredWebState != null) {
            showBrowser()
            webView.restoreState(restoredWebState!!)
        }
    }

    private fun bindViews() {
        urlBar = findViewById(R.id.urlBar)
        btnRefresh = findViewById(R.id.btnRefresh)
        btnMenu = findViewById(R.id.btnMenu)
        webView = findViewById(R.id.webView)
        bottomNav = findViewById(R.id.bottomNav)

        progressBar = runCatching { findViewById<ProgressBar>(R.id.progressBar) }.getOrNull()
        homeSearchBar = runCatching { findViewById<EditText>(R.id.homeSearchBar) }.getOrNull()
        btnTabs = runCatching { findViewById<View>(R.id.btnTabs) }.getOrNull()
        tabsCount = runCatching { findViewById<TextView>(R.id.tabsCount) }.getOrNull()

        homeContainer = findViewById(R.id.homeContainer)
        webViewContainer = findViewById(R.id.webViewContainer)
        fragmentContainer = findViewById(R.id.fragmentContainer)

        tabsCount?.text = "1"
    }

    private fun isRunningOnTv(): Boolean {
        val uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        val isTelevisionMode = uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
        val hasLeanback = packageManager.hasSystemFeature("android.software.leanback")
        return isTelevisionMode || hasLeanback
    }

    private fun setupTopButtons() {
        btnRefresh.setOnClickListener {
            if (webViewContainer.isVisible) {
                webView.reload()
            } else {
                showBrowser()
            }
        }

        btnMenu.setOnClickListener {
            showBrowserMenuDialog()
        }

        btnTabs?.setOnClickListener {
            Toast.makeText(this, "Tabs coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showBrowserMenuDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_browser_menu, null)

        val dialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setView(view)
            .create()

        view.findViewById<MaterialButton>(R.id.menuRefresh).setOnClickListener {
            dialog.dismiss()
            showBrowser()
            webView.reload()
        }

        view.findViewById<MaterialButton>(R.id.menuStop).setOnClickListener {
            dialog.dismiss()
            webView.stopLoading()
        }

        view.findViewById<MaterialButton>(R.id.menuBookmark).setOnClickListener {
            dialog.dismiss()
            addBookmarkCurrent()
        }

        view.findViewById<MaterialButton>(R.id.menuShare).setOnClickListener {
            dialog.dismiss()
            shareCurrentPage()
        }

        view.findViewById<MaterialButton>(R.id.menuCopy).setOnClickListener {
            dialog.dismiss()
            copyCurrentUrl()
        }

        view.findViewById<MaterialButton>(R.id.menuDesktop).setOnClickListener {
            dialog.dismiss()
            toggleDesktopMode()
        }

        view.findViewById<MaterialButton>(R.id.menuFind).setOnClickListener {
            dialog.dismiss()
            showBrowser()
            showFindInPageDialog()
        }

        view.findViewById<MaterialButton>(R.id.menuTranslate).setOnClickListener {
            dialog.dismiss()
            translateCurrentPageWithChoice()
        }

        view.findViewById<MaterialButton>(R.id.menuUpdates).setOnClickListener {
            dialog.dismiss()
            checkForUpdates()
        }

        dialog.show()
    }

    private fun setupBottomNav() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    showBrowser()
                    loadUrl(getHomeUrl())
                    true
                }

                R.id.nav_bookmarks -> {
                    showFragment(BookmarksFragment.newInstance())
                    true
                }

                R.id.nav_history -> {
                    showFragment(HistoryFragment.newInstance())
                    true
                }

                R.id.nav_tools -> {
                    showFragment(AuryxToolsFragment.newInstance())
                    true
                }

                R.id.nav_settings -> {
                    showFragment(SettingsFragment.newInstance())
                    true
                }

                else -> false
            }
        }
    }

    private fun setupHomeSearch() {
        homeSearchBar?.setOnEditorActionListener { _, actionId, event ->
            val isSearch =
                actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_GO ||
                    (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)

            if (isSearch) {
                val text = homeSearchBar?.text?.toString()?.trim().orEmpty()
                if (text.isNotEmpty()) {
                    showBrowser()
                    loadFromInput(text)
                }
                true
            } else {
                false
            }
        }
    }

    private fun attachMicroAnimations() {
        listOf(btnRefresh, btnMenu, urlBar).forEach { it.applyPressAnimation() }
        btnTabs?.applyPressAnimation()
        bottomNav.applyPressAnimation(1f, 0.995f)
        homeSearchBar?.animateEntrance(40L)
    }

    private fun showFragment(fragment: androidx.fragment.app.Fragment) {
        homeContainer.crossfadeVisible(false)
        webViewContainer.crossfadeVisible(false)
        fragmentContainer.crossfadeVisible(true)

        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun showBrowser() {
        fragmentContainer.crossfadeVisible(false)
        homeContainer.crossfadeVisible(false)
        webViewContainer.crossfadeVisible(true)

        if (isTvDevice) {
            webView.requestFocus()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        CookieManager.getInstance().setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        }

        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val jsEnabled = prefs.getBoolean(KEY_JAVASCRIPT_ENABLED, true)
        val loadImages = prefs.getBoolean(KEY_LOAD_IMAGES, true)

        val s: WebSettings = webView.settings
        s.javaScriptEnabled = jsEnabled
        s.domStorageEnabled = true
        s.databaseEnabled = true
        s.loadsImagesAutomatically = loadImages
        s.useWideViewPort = true
        s.loadWithOverviewMode = true
        s.builtInZoomControls = true
        s.displayZoomControls = false
        s.setSupportZoom(true)
        s.mediaPlaybackRequiresUserGesture = false
        s.javaScriptCanOpenWindowsAutomatically = false
        s.cacheMode = WebSettings.LOAD_DEFAULT
        s.userAgentString = defaultUserAgent()
        s.setSupportMultipleWindows(false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WebView.setWebContentsDebuggingEnabled(false)
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar?.isVisible = newProgress in 1..99
                progressBar?.progress = newProgress
            }

            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                return false
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                if (
                    url.startsWith("tel:") ||
                    url.startsWith("mailto:") ||
                    url.startsWith("sms:")
                ) {
                    openExternal(url)
                    return true
                }
                return false
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                progressBar?.isVisible = true
                progressBar?.progress = 8
                urlBar.setText(url)
                findActive = false
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView, url: String) {
                progressBar?.progress = 100
                progressBar?.isVisible = false
                urlBar.setText(url)
                store.addHistory(url)
                super.onPageFinished(view, url)
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (request.isForMainFrame) {
                    progressBar?.isVisible = false
                    Toast.makeText(this@MainActivity, "Page load failed", Toast.LENGTH_SHORT).show()
                }
                super.onReceivedError(view, request, error)
            }

            override fun onRenderProcessGone(
                view: WebView,
                detail: RenderProcessGoneDetail
            ): Boolean {
                runCatching { view.destroy() }
                Toast.makeText(this@MainActivity, "Browser engine restarted", Toast.LENGTH_SHORT).show()
                recreate()
                return true
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
            } else {
                false
            }
        }
    }

    private fun optimizeForTv() {
        if (!isTvDevice) return

        urlBar.isFocusable = true
        urlBar.isFocusableInTouchMode = true

        btnRefresh.isFocusable = true
        btnMenu.isFocusable = true
        bottomNav.isFocusable = true
        webView.isFocusable = true
        webView.isFocusableInTouchMode = true

        btnTabs?.isFocusable = true
        homeSearchBar?.apply {
            isFocusable = true
            isFocusableInTouchMode = true
        }

        bottomNav.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED

        applyTvFocusEffect(urlBar, 1.02f)
        applyTvFocusEffect(btnRefresh, 1.10f)
        applyTvFocusEffect(btnMenu, 1.10f)
        applyTvFocusEffect(webView, 1.0f)

        btnTabs?.let { applyTvFocusEffect(it, 1.10f) }
        homeSearchBar?.let { applyTvFocusEffect(it, 1.03f) }

        webView.post {
            if (webViewContainer.isVisible) {
                webView.requestFocus()
            } else {
                urlBar.requestFocus()
            }
        }
    }

    private fun applyTvFocusEffect(view: View, focusedScale: Float) {
        view.setOnFocusChangeListener { v, hasFocus ->
            v.animate()
                .scaleX(if (hasFocus) focusedScale else 1f)
                .scaleY(if (hasFocus) focusedScale else 1f)
                .alpha(if (hasFocus) 1f else 0.95f)
                .setDuration(120)
                .start()
        }
    }

    private fun defaultUserAgent(): String {
        val base = WebSettings.getDefaultUserAgent(this)
        return if (desktopModeEnabled || isTvDevice) {
            base.replace("Mobile", "X11").replace("Android", "Linux")
        } else {
            base
        }
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

        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val engine = prefs.getString(KEY_SEARCH_ENGINE, "DuckDuckGo") ?: "DuckDuckGo"
        val q = URLEncoder.encode(trimmed, "UTF-8")

        return when (engine) {
            "Google" -> "https://www.google.com/search?q=$q"
            "Bing" -> "https://www.bing.com/search?q=$q"
            else -> "https://duckduckgo.com/?q=$q"
        }
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean = super.onCreateOptionsMenu(menu)

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

            R.id.menu_add_bookmark -> {
                addBookmarkCurrent()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (isTvDevice && event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_MENU -> {
                    btnMenu.performClick()
                    return true
                }

                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    if (webViewContainer.isVisible) {
                        webView.reload()
                        return true
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event)
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
        getSharedPreferences(PREFS, MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DESKTOP_MODE, desktopModeEnabled)
            .apply()

        webView.settings.userAgentString = defaultUserAgent()
        webView.reload()

        Toast.makeText(
            this,
            if (desktopModeEnabled) "Desktop mode ON" else "Desktop mode OFF",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun addBookmarkCurrent() {
        val url = webView.url ?: urlBar.text?.toString()?.trim().orEmpty()
        if (url.isBlank()) {
            Toast.makeText(this, "No URL to bookmark", Toast.LENGTH_SHORT).show()
            return
        }
        store.addBookmark(url)
        Toast.makeText(this, "Added to bookmarks", Toast.LENGTH_SHORT).show()
    }

    private fun showFindInPageDialog() {
        val input = EditText(this).apply {
            hint = "Find in page"
            setText(findQuery)
            setSelection(text?.length ?: 0)
        }

        AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle("Find in page")
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
                Toast.makeText(this, "Searching for \"$q\"", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Next") { _, _ ->
                if (findActive) {
                    webView.findNext(true)
                } else {
                    Toast.makeText(this, "Search something first", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Previous") { _, _ ->
                if (findActive) {
                    webView.findNext(false)
                } else {
                    Toast.makeText(this, "Search something first", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun translateCurrentPageWithChoice() {
        val currentUrl = webView.url?.trim().orEmpty()
        if (currentUrl.isBlank()) {
            Toast.makeText(this, "No page to translate", Toast.LENGTH_SHORT).show()
            return
        }

        val labels = arrayOf("Italiano", "English", "Español", "Français")
        val codes = arrayOf("it", "en", "es", "fr")

        AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle("Translate page")
            .setItems(labels) { _, which ->
                val encodedUrl = URLEncoder.encode(currentUrl, "UTF-8")
                val translateUrl =
                    "https://translate.google.com/translate?sl=auto&tl=${codes[which]}&u=$encodedUrl"
                showBrowser()
                webView.loadUrl(translateUrl)
            }
            .show()
    }

    private fun getHomeUrl(): String {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val value = prefs.getString(KEY_HOME, DEFAULT_HOME) ?: DEFAULT_HOME
        return if (value.isBlank()) DEFAULT_HOME else value
    }

    private fun checkForUpdates() {
        val currentVersion = BuildConfig.VERSION_NAME

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Checking for updates…", Toast.LENGTH_SHORT).show()

        thread {
            try {
                val html = httpGetText(UPDATE_SITE)
                val latest = extractLatestVersionFromUptodownHtml(html)

                runOnUiThread {
                    if (latest.isBlank()) {
                        Toast.makeText(this, "Can't detect latest version", Toast.LENGTH_LONG).show()
                        return@runOnUiThread
                    }

                    if (compareVersions(latest, currentVersion) > 0) {
                        AlertDialog.Builder(this)
                            .setTitle("Update available")
                            .setMessage("New version: $latest\nCurrent: $currentVersion")
                            .setPositiveButton("Open Uptodown") { _, _ ->
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

    private fun extractLatestVersionFromUptodownHtml(html: String): String {
        val patterns = listOf(
            Regex("""\b(\d+\.\d+\.\d+)\b"""),
            Regex("""version["'\s:>]+(\d+\.\d+\.\d+)""", RegexOption.IGNORE_CASE),
            Regex("""AuryxBrowser[^\d]*(\d+\.\d+\.\d+)""", RegexOption.IGNORE_CASE)
        )

        val matches = mutableListOf<String>()

        for (pattern in patterns) {
            pattern.findAll(html).forEach { match ->
                val value = match.groupValues.getOrNull(1).orEmpty()
                if (value.matches(Regex("""\d+\.\d+\.\d+"""))) {
                    matches.add(value)
                }
            }
        }

        if (matches.isEmpty()) return ""

        return matches.distinct().maxWithOrNull { a, b -> compareVersions(a, b) } ?: ""
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
            connectTimeout = 10000
            readTimeout = 10000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "Mozilla/5.0")
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
                loadUrl(getHomeUrl())
            }

            else -> Toast.makeText(this, "Unknown action: $action", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val webBundle = Bundle()
        webView.saveState(webBundle)
        outState.putBundle(KEY_WEBVIEW_STATE, webBundle)
    }

    override fun onPause() {
        webView.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onBackPressed() {
        if (findActive) {
            findActive = false
            webView.clearMatches()
            Toast.makeText(this, "Find cleared", Toast.LENGTH_SHORT).show()
            return
        }

        if (fragmentContainer.isVisible) {
            showBrowser()
            return
        }

        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        runCatching {
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.onPause()
            webView.removeAllViews()
            webView.destroy()
        }
        super.onDestroy()
    }
}
