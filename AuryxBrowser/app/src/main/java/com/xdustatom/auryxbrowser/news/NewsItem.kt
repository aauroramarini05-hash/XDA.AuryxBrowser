package com.xdustatom.auryxbrowser.news

data class NewsItem(
    val id: String,
    val title: String,
    val summary: String,
    val url: String,
    val imageUrl: String? = null,
    val source: String,
    val country: String,
    val category: String,
    val publishedAt: String
)

data class NewsFeedResponse(
    val updatedAt: String,
    val items: List<NewsItem>
)
