package com.example.geart_20.model

data class Conversation(
    val id: String = "",
    val type: String = "commission",
    val title: String = "",
    val subtitle: String = "",
    val lastMessage: String = "",
    val lastTimestamp: Long = 0L,
    val otherUserId: String = "",
    val commissionId: String = "",
    val clientId: String = "",
    val unread: Boolean = false
)
