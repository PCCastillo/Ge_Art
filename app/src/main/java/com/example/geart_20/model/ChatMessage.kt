package com.example.geart_20.model

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val message: String = "", // Aquí irá el texto, o el Link de la imagen, o el nuevo precio
    val type: String = "TEXT", // Tipos: "TEXT", "PROGRESS_IMAGE", "FINAL_PRODUCT", "PRICE_UPDATE"
    val timestamp: Long = System.currentTimeMillis()
)