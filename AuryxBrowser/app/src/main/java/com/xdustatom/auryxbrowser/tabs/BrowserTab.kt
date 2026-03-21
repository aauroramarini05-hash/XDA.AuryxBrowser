package com.xdustatom.auryxbrowser.tabs

data class BrowserTab(
    val id: Long,
    var title: String = "New Tab",
    var url: String = "",
    var lastUpdated: Long = System.currentTimeMillis()
)
