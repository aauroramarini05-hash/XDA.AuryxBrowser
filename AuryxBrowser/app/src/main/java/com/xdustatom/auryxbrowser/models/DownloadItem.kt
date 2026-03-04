package com.xdustatom.auryxbrowser.models

import java.util.UUID

data class DownloadItem(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val fileName: String,
    val filePath: String,
    var progress: Int = 0,
    var totalSize: Long = 0,
    var downloadedSize: Long = 0,
    var status: DownloadStatus = DownloadStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis()
)

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    FAILED,
    CANCELLED
}
