package com.example.geart_20.model

data class Commission(
    val id: String = "",
    val clientId: String = "",
    val artistId: String = "",
    val title: String = "",
    val description: String = "",
    val status: String = "PENDING",
    val price: Double = 0.0,
    val referenceImageUrl: String = "",
    val isRated: Boolean = false,
    val finalProductUrl: String = ""
)