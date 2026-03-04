package com.xdustatom.auryxbrowser.models

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

data class Tab(
    val id: String = UUID.randomUUID().toString(),
    var url: String = "",
    var title: String = "New Tab",
    var favicon: Bitmap? = null,
    var isActive: Boolean = false,
    var progress: Int = 0,
    var canGoBack: Boolean = false,
    var canGoForward: Boolean = false
)
