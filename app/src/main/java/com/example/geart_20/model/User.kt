package com.example.geart_20.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "CLIENT",

    // Nuevos campos para personalización
    val profileImageUrl: String = "", // Link a Firebase Storage
    val bio: String = "", // Descripción del perfil
    val socialLinks: String = "", // Redes sociales (ej. linktree o instagram)

    // Específicos para el Artista (el cliente simplemente los tendrá en 0 o vacíos)
    val commissionTableUrl: String = "", // Imagen de su tabla de precios
    val rating: Double = 0.0, // Promedio de estrellas
    val ratingCount: Int = 0, // Cantidad de personas que lo han calificado
    val fcmToken: String = "" // Token de Firebase Cloud Messaging para push notifications
)