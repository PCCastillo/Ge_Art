package com.example.geart_20.model

data class NotificationItem(
    val id: String = "",
    val type: String = "",  // "new_comment", "new_message", "new_direct_commission", "commission_accepted", "commission_completed"
    val message: String = "",
    val relatedUserId: String = "",
    val relatedUserName: String = "",
    val relatedId: String = "",  // commissionId or chatId
    val read: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
