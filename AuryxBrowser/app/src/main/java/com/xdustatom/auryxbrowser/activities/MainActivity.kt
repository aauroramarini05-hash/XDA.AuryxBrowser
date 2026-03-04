package com.xdustatom.auryxbrowser.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.xdustatom.auryxbrowser.AuryxApplication
import com.xdustatom.auryxbrowser.R
import com.xdustatom.auryxbrowser.databinding.ActivityMainBinding
import com.xdustatom.auryxbrowser.fragments.*
import com.xdustatom.auryxbrowser.models.Bookmark
import com.xdustatom.auryxbrowser.models.DownloadItem
import com.xdustatom.auryxbrowser.models.DownloadStatus
import com.xdustatom.auryxbrowser.models.HistoryItem
import com.xdustatom.auryxbrowser.models.Tab
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val prefs by lazy { AuryxApplication.instance.preferencesManager }
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val tabs = mutableListOf<Tab>()
    private var currentTabIndex = 0
    private val webViews = mutableMapOf<String, WebView>()

    private var isHomeVisible = true
    private var contextMenuUrl: String? = null

    companion object {
        private const val STORAGE_PERMISSION_CODE = 100
        private const val DESKTOP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupWebView()
        setupBottomNavigation()
        createNewTab()
        showHome()

        // Handle intent URLs
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        val url = intent.dataString
        if (!url.isNullOrEmpty()) {
            loadUrl(url)
        }
    }

    private fun setupUI() {
        // URL bar actions
        binding.urlBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.urlBar.text.toString().trim()
                if (query.isNotEmpty()) {
                    val url = if (isValidUrl(query)) {
                        if (!query.startsWith("http")) "https://$query" else query
                    } else {
                        prefs.getSearchUrl(query)
                    }
                    loadUrl(url)
                    hideKeyboard()
                }
                true
            } else false
        }

        binding.urlBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.urlBar.selectAll()
            }
        }

        // Refresh button
        binding.btnRefresh.setOnClickListener {
            getCurrentWebView()?.reload()
        }

        // Tabs button
        binding.btnTabs.setOnClickListener {
            showTabsOverview()
        }

        // Menu button
        binding.btnMenu.setOnClickListener {
            showMainMenu()
        }

        // Home search bar
        binding.homeSearchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.homeSearchBar.text.toString().trim()
                if (query.isNotEmpty()) {
                    val url = if (isValidUrl(query)) {
                        if (!query.startsWith("http")) "https://$query" else query
                    } else {
                        prefs.getSearchUrl(query)
                    }
                    loadUrl(url)
                    hideKeyboard()
                }
                true
            } else false
        }

        // Update tabs count
        updateTabsCount()
    }

    private fun isValidUrl(text: String): Boolean {
        val urlPattern = "^(https?://)?([\\w-]+\\.)+[\\w-]+(/.*)?$".toRegex(RegexOption.IGNORE_CASE)
        return urlPattern.matches(text) || text.contains(".")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        registerForContextMenu(binding.webView)
    }

    private fun createWebView(): WebView {
        return WebView(this).apply {
            setupWebViewSettings(this)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebViewSettings(webView: WebView) {
        webView.settings.apply {
            javaScriptEnabled = prefs.isJavaScriptEnabled
            domStorageEnabled = true
            databaseEnabled = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportMultipleWindows(true)
            javaScriptCanOpenWindowsAutomatically = prefs.isPopupsEnabled
            allowFileAccess = true
            allowContentAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            cacheMode = WebSettings.LOAD_DEFAULT

            if (prefs.isDesktopMode) {
                userAgentString = DESKTOP_USER_AGENT
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let {
                    binding.urlBar.setText(it)
                    updateCurrentTab { tab ->
                        tab.url = it
                        tab.progress = 0
                    }
                }
                binding.progressBar.isVisible = true
                binding.btnRefresh.setImageResource(R.drawable.ic_close)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBar.isVisible = false
                binding.btnRefresh.setImageResource(R.drawable.ic_refresh)

                url?.let { pageUrl ->
                    val title = view?.title ?: pageUrl
                    updateCurrentTab { tab ->
                        tab.url = pageUrl
                        tab.title = title
                        tab.canGoBack = view?.canGoBack() ?: false
                        tab.canGoForward = view?.canGoForward() ?: false
                    }

                    // Add to history
                    if (pageUrl.isNotEmpty() && !pageUrl.startsWith("about:")) {
                        prefs.addHistoryItem(HistoryItem(url = pageUrl, title = title))
                    }
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false

                // Handle special URLs
                if (url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("intent:")) {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Cannot open link", Toast.LENGTH_SHORT).show()
                    }
                    return true
                }

                return false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                binding.progressBar.progress = newProgress
                updateCurrentTab { it.progress = newProgress }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                updateCurrentTab { tab ->
                    tab.title = title ?: "New Tab"
                }
            }

            override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                updateCurrentTab { tab ->
                    tab.favicon = icon
                }
            }

            override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: android.os.Message?): Boolean {
                val newTab = createNewTab()
                val newWebView = webViews[newTab.id]
                val transport = resultMsg?.obj as? WebView.WebViewTransport
                transport?.webView = newWebView
                resultMsg?.sendToTarget()
                return true
            }
        }

        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            downloadFile(url, userAgent, contentDisposition, mimeType)
        }

        registerForContextMenu(webView)
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    showHome()
                    true
                }
                R.id.nav_bookmarks -> {
                    showBookmarks()
                    true
                }
                R.id.nav_history -> {
                    showHistory()
                    true
                }
                R.id.nav_tools -> {
                    showAuryxTools()
                    true
                }
                R.id.nav_settings -> {
                    showSettings()
                    true
                }
                else -> false
            }
        }
    }

    private fun showHome() {
        isHomeVisible = true
        binding.homeContainer.isVisible = true
        binding.webViewContainer.isVisible = false
        binding.fragmentContainer.isVisible = false
        binding.homeSearchBar.setText("")
        setupQuickAccess()
    }

    private fun setupQuickAccess() {
        val quickAccess = prefs.getQuickAccessSites()
        binding.quickAccessGrid.removeAllViews()

        quickAccess.take(6).forEach { site ->
            val itemView = layoutInflater.inflate(R.layout.item_quick_access, binding.quickAccessGrid, false)
            itemView.findViewById<android.widget.TextView>(R.id.quickAccessTitle).text = site.title
            itemView.findViewById<android.widget.TextView>(R.id.quickAccessIcon).text = site.title.first().uppercase()
            itemView.setOnClickListener {
                loadUrl(site.url)
            }
            binding.quickAccessGrid.addView(itemView)
        }
    }

    private fun showWebView() {
        isHomeVisible = false
        binding.homeContainer.isVisible = false
        binding.webViewContainer.isVisible = true
        binding.fragmentContainer.isVisible = false
    }

    private fun showFragment() {
        binding.homeContainer.isVisible = false
        binding.webViewContainer.isVisible = false
        binding.fragmentContainer.isVisible = true
    }

    private fun showBookmarks() {
        showFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, BookmarksFragment { url ->
                loadUrl(url)
                binding.bottomNav.selectedItemId = R.id.nav_home
            })
            .commit()
    }

    private fun showHistory() {
        showFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, HistoryFragment { url ->
                loadUrl(url)
                binding.bottomNav.selectedItemId = R.id.nav_home
            })
            .commit()
    }

    private fun showAuryxTools() {
        showFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, AuryxToolsFragment())
            .commit()
    }

    private fun showSettings() {
        showFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, SettingsFragment {
                // Refresh WebView settings
                webViews.values.forEach { setupWebViewSettings(it) }
            })
            .commit()
    }

    private fun showTabsOverview() {
        showFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, TabsFragment(
                tabs = tabs.toList(),
                onTabSelected = { index ->
                    switchToTab(index)
                    if (isHomeVisible) showHome() else showWebView()
                },
                onTabClosed = { index ->
                    closeTab(index)
                },
                onNewTab = {
                    createNewTab()
                    showHome()
                }
            ))
            .commit()
    }

    private fun showMainMenu() {
        val items = arrayOf(
            "New Tab",
            "Add to Bookmarks",
            "Downloads",
            "Desktop Mode",
            "Find in Page",
            "Share"
        )

        AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> {
                        createNewTab()
                        showHome()
                    }
                    1 -> addCurrentPageToBookmarks()
                    2 -> showDownloads()
                    3 -> toggleDesktopMode()
                    4 -> Toast.makeText(this, "Find in page - Coming soon", Toast.LENGTH_SHORT).show()
                    5 -> shareCurrentPage()
                }
            }
            .show()
    }

    private fun showDownloads() {
        showFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, DownloadsFragment())
            .commit()
    }

    private fun addCurrentPageToBookmarks() {
        val currentTab = getCurrentTab() ?: return
        if (currentTab.url.isEmpty() || currentTab.url.startsWith("about:")) {
            Toast.makeText(this, "Cannot bookmark this page", Toast.LENGTH_SHORT).show()
            return
        }

        if (prefs.isBookmarked(currentTab.url)) {
            Toast.makeText(this, "Already bookmarked", Toast.LENGTH_SHORT).show()
        } else {
            prefs.addBookmark(Bookmark(
                url = currentTab.url,
                title = currentTab.title
            ))
            Toast.makeText(this, "Bookmark added", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleDesktopMode() {
        prefs.isDesktopMode = !prefs.isDesktopMode
        webViews.values.forEach { setupWebViewSettings(it) }
        getCurrentWebView()?.reload()
        val message = if (prefs.isDesktopMode) "Desktop mode enabled" else "Desktop mode disabled"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun shareCurrentPage() {
        val currentTab = getCurrentTab() ?: return
        if (currentTab.url.isEmpty()) return

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, currentTab.title)
            putExtra(Intent.EXTRA_TEXT, currentTab.url)
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    }

    // Tab Management
    private fun createNewTab(): Tab {
        val tab = Tab()
        tabs.add(tab)
        currentTabIndex = tabs.size - 1

        val webView = createWebView()
        webViews[tab.id] = webView

        updateTabsCount()
        return tab
    }

    private fun switchToTab(index: Int) {
        if (index < 0 || index >= tabs.size) return

        currentTabIndex = index
        val tab = tabs[index]
        val webView = webViews[tab.id]

        binding.webViewContainer.removeAllViews()
        webView?.let {
            binding.webViewContainer.addView(it)
            binding.urlBar.setText(tab.url)
        }

        if (tab.url.isEmpty()) {
            showHome()
        } else {
            showWebView()
        }
    }

    private fun closeTab(index: Int) {
        if (tabs.size <= 1) {
            // Keep at least one tab
            tabs[0] = Tab()
            webViews[tabs[0].id]?.loadUrl("about:blank")
            showHome()
            return
        }

        val tab = tabs[index]
        webViews[tab.id]?.destroy()
        webViews.remove(tab.id)
        tabs.removeAt(index)

        if (currentTabIndex >= tabs.size) {
            currentTabIndex = tabs.size - 1
        }

        updateTabsCount()
        switchToTab(currentTabIndex)
    }

    private fun getCurrentTab(): Tab? = tabs.getOrNull(currentTabIndex)

    private fun getCurrentWebView(): WebView? {
        val tab = getCurrentTab() ?: return null
        return webViews[tab.id]
    }

    private fun updateCurrentTab(update: (Tab) -> Unit) {
        getCurrentTab()?.let { update(it) }
    }

    private fun updateTabsCount() {
        binding.tabsCount.text = tabs.size.toString()
    }

    // URL Loading
    fun loadUrl(url: String) {
        showWebView()
        val currentTab = getCurrentTab() ?: createNewTab()
        var webView = webViews[currentTab.id]

        if (webView == null) {
            webView = createWebView()
            webViews[currentTab.id] = webView
        }

        binding.webViewContainer.removeAllViews()
        binding.webViewContainer.addView(webView)

        val finalUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else url

        webView.loadUrl(finalUrl)
        binding.urlBar.setText(finalUrl)
    }

    // Downloads
    private fun downloadFile(url: String, userAgent: String, contentDisposition: String, mimeType: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
                return
            }
        }

        val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)

        try {
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setMimeType(mimeType)
                addRequestHeader("User-Agent", userAgent)
                setDescription("Downloading file...")
                setTitle(fileName)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            }

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            // Save to our download list
            prefs.addDownload(DownloadItem(
                url = url,
                fileName = fileName,
                filePath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/$fileName",
                status = DownloadStatus.DOWNLOADING
            ))

            Toast.makeText(this, "Download started: $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Context Menu for links
    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val webView = getCurrentWebView() ?: return
        val result = webView.hitTestResult

        when (result.type) {
            WebView.HitTestResult.SRC_ANCHOR_TYPE,
            WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                contextMenuUrl = result.extra
                menu?.setHeaderTitle("Link Options")
                menu?.add(0, 1, 0, "Open in New Tab")
                menu?.add(0, 2, 0, "Copy Link")
                menu?.add(0, 3, 0, "Share Link")
            }
            WebView.HitTestResult.IMAGE_TYPE -> {
                contextMenuUrl = result.extra
                menu?.setHeaderTitle("Image Options")
                menu?.add(0, 4, 0, "Save Image")
                menu?.add(0, 5, 0, "Open Image in New Tab")
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val url = contextMenuUrl ?: return super.onContextItemSelected(item)

        when (item.itemId) {
            1 -> { // Open in New Tab
                val newTab = createNewTab()
                webViews[newTab.id]?.loadUrl(url)
                Toast.makeText(this, "Opened in new tab", Toast.LENGTH_SHORT).show()
            }
            2 -> { // Copy Link
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("URL", url))
                Toast.makeText(this, "Link copied", Toast.LENGTH_SHORT).show()
            }
            3 -> { // Share Link
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, url)
                }
                startActivity(Intent.createChooser(intent, "Share via"))
            }
            4 -> { // Save Image
                downloadFile(url, "", "", "image/*")
            }
            5 -> { // Open Image in New Tab
                val newTab = createNewTab()
                webViews[newTab.id]?.loadUrl(url)
            }
        }

        return true
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.urlBar.windowToken, 0)
        binding.urlBar.clearFocus()
    }

    override fun onBackPressed() {
        val webView = getCurrentWebView()
        when {
            binding.fragmentContainer.isVisible -> {
                if (isHomeVisible) showHome() else showWebView()
            }
            webView?.canGoBack() == true -> webView.goBack()
            !isHomeVisible -> showHome()
            else -> super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        webViews.values.forEach { it.destroy() }
    }
}
