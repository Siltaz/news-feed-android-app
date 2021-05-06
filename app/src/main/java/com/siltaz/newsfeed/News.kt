package com.siltaz.newsfeed

data class News(
    val source: String,
    val title: String,
    val description: String,
    val url: String,
    val imageUrl: String,
    val publishedAt: String
)
