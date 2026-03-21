package com.xdustatom.auryxbrowser.remote

data class RemoteFeatureConfig(
    val schemaVersion: Int = 1,
    val flags: Map<String, Boolean> = emptyMap(),
    val commands: List<RemoteCommand> = emptyList(),
    val quickLinks: List<QuickLink> = emptyList(),
    val announcement: String? = null
)

data class RemoteCommand(
    val trigger: String = "",
    val match: String = "equals",
    val action: String = "",
    val data: String? = null,
    val reply: String = ""
)

data class QuickLink(
    val title: String = "",
    val url: String = ""
)
