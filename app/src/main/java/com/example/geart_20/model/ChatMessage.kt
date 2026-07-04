package com.example.geart_20.model

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val message: String = "",
    val type: String = "TEXT",
    val timestamp: Long = System.currentTimeMillis(),
    val accepted: Boolean = false
)