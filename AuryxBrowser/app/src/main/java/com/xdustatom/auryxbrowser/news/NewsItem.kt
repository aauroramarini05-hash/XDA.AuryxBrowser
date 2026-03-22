package com.xdustatom.auryxbrowser.news

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class NewsItem(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("title")
    val title: String = "",

    @SerializedName("summary")
    val summary: String = "",

    @SerializedName("url")
    val url: String = "",

    @SerializedName("imageUrl")
    val imageUrl: String? = null,

    @SerializedName("source")
    val source: String = "",

    @SerializedName("country")
    val country: String = "",

    @SerializedName("category")
    val category: String = "",

    @SerializedName("publishedAt")
    val publishedAt: String = ""
)

@Keep
data class NewsFeedResponse(
    @SerializedName("updatedAt")
    val updatedAt: String = "",

    @SerializedName("items")
    val items: List<NewsItem> = emptyList()
)
