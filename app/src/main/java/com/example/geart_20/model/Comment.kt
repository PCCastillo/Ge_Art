package com.example.geart_20.model

data class Comment(
    val id: String = "",
    val fromUserId: String = "",
    val fromUserName: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)