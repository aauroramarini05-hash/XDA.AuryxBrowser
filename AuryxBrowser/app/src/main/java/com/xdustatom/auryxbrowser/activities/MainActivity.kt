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
import com.xdustatom.auryxbrowser.utils.LocaleHelper
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    lateinit var webView: WebView

    private val tabs = mutableListOf<Tab>()
    private var currentTabIndex = 0
    private val webViews = mutableMapOf<String, WebView>()

    private var isHomeVisible = true
    private var contextMenuUrl: String? = null
    
    // Page info tracking
    private var pageLoadStartTime: Long = 0
    private var lastPageLoadTime: Long = 0
    private var findInPageDialog: AlertDialog? = null

    companion object {
        private const val STORAGE_PERMISSION_CODE = 100
        private const val DESKTOP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }
    val CURRENT_VERSION = "1.305.02"

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

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
                pageLoadStartTime = System.currentTimeMillis()
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
                lastPageLoadTime = System.currentTimeMillis() - pageLoadStartTime
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
            "Page Info",
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
                    4 -> showFindInPage()
                    5 -> showPageInfo()
                    6 -> shareCurrentPage()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun checkUpdates() {

    @SuppressLint("InflateParams")
    private fun showFindInPage() {
        val webView = getCurrentWebView() ?: run {
            Toast.makeText(this, "No page loaded", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_find_in_page, null)
        val etQuery = dialogView.findViewById<android.widget.EditText>(R.id.etFindQuery)
        val tvMatchCount = dialogView.findViewById<android.widget.TextView>(R.id.tvMatchCount)
        val btnPrevious = dialogView.findViewById<android.widget.ImageButton>(R.id.btnPrevious)
        val btnNext = dialogView.findViewById<android.widget.ImageButton>(R.id.btnNext)
        val btnClose = dialogView.findViewById<android.widget.ImageButton>(R.id.btnCloseFindBar)

        findInPageDialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        webView.setFindListener { activeMatchOrdinal, numberOfMatches, isDoneCounting ->
            if (isDoneCounting) {
                tvMatchCount.text = if (numberOfMatches > 0) {
                    "${activeMatchOrdinal + 1} of $numberOfMatches"
                } else {
                    "No matches"
                }
            }
        }

        etQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = etQuery.text.toString()
                if (query.isNotEmpty()) {
                    webView.findAllAsync(query)
                }
                true
            } else false
        }

        etQuery.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    webView.findAllAsync(query)
                } else {
                    webView.clearMatches()
                    tvMatchCount.text = "0 matches"
                }
            }
        })

        btnPrevious.setOnClickListener { webView.findNext(false) }
        btnNext.setOnClickListener { webView.findNext(true) }
        btnClose.setOnClickListener {
            webView.clearMatches()
            findInPageDialog?.dismiss()
        }

        findInPageDialog?.setOnDismissListener {
            webView.clearMatches()
        }

        findInPageDialog?.show()
        etQuery.requestFocus()
    }

    private fun showPageInfo() {
        val currentTab = getCurrentTab()
        val webView = getCurrentWebView()

        if (currentTab == null || currentTab.url.isEmpty()) {
            Toast.makeText(this, "No page loaded", Toast.LENGTH_SHORT).show()
            return
        }

        showFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, PageInfoFragment(
                pageUrl = currentTab.url,
                pageTitle = currentTab.title,
                userAgent = webView?.settings?.userAgentString ?: "",
                loadTime = lastPageLoadTime
            ))
            .addToBackStack(null)
            .commit()
    }

    // Functions for Assistant actions
    fun performAssistantAction(action: String, data: String?) {
        when (action) {
            "open_url" -> data?.let { loadUrl(it) }
            "search" -> data?.let { loadUrl(prefs.getSearchUrl(it)) }
            "open_settings" -> {
                binding.bottomNav.selectedItemId = R.id.nav_settings
            }
            "open_bookmarks" -> {
                binding.bottomNav.selectedItemId = R.id.nav_bookmarks
            }
            "open_history" -> {
                binding.bottomNav.selectedItemId = R.id.nav_history
            }
            "new_tab" -> {
                createNewTab()
                showHome()
            }
        }
    }

    // Tab Management
    private fun createNewTab(): Tab {
        val tab = Tab()
        tabs.add(tab)
        currentTabIndex = tabs.size - 1
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
