package com.xdustatom.auryxbrowser.models

import java.util.UUID

data class HistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val title: String,
    val favicon: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
