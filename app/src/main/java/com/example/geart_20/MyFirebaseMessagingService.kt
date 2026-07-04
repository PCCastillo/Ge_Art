package com.example.geart_20

import android.content.Intent
import android.util.Log
import com.example.geart_20.model.NotificationItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Servicio FCM que maneja tokens y mensajes entrantes.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "GeArt_FCM"
        const val KEY_FCM_TOKEN = "fcmToken"
    }

    /**
     * Se llama cuando se genera/refresca el token FCM.
     * Guardamos el token en Firebase para que el backend pueda enviar pushes.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM: $token")

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Guardar token en el perfil del usuario
            FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.uid)
                .child(KEY_FCM_TOKEN)
                .setValue(token)
                .addOnSuccessListener {
                    Log.d(TAG, "Token FCM guardado para usuario ${currentUser.uid}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al guardar token FCM", e)
                }
        }
    }

    /**
     * Se llama cuando llega un mensaje FCM.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Mensaje FCM recibido: ${message.data}")

        // Datos del mensaje
        val data = message.data
        val title = data["title"] ?: message.notification?.title ?: "GeArt"
        val body = data["body"] ?: message.notification?.body ?: ""
        val type = data["type"] ?: ""
        val relatedUserId = data["relatedUserId"] ?: ""
        val relatedId = data["relatedId"] ?: ""
        val relatedUserName = data["relatedUserName"] ?: ""
        val channelId = data["channelId"] ?: NotificationHelper.CHANNEL_GENERAL

        // Guardar en Realtime Database (para que aparezca en NotificationsActivity)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && type.isNotEmpty()) {
            guardarNotificacionEnRTDB(type, body, relatedUserId, relatedUserName, relatedId)
        }

        // Crear intent de navegación según el tipo
        val intent = if (type.isNotEmpty()) {
            NotificationHelper.createNavigationIntent(this, type, relatedUserId, relatedId, relatedUserName)
        } else {
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }

        // Mostrar notificación
        NotificationHelper.showNotification(
            context = this,
            title = title,
            message = body,
            channelId = channelId,
            dataIntent = intent
        )
    }

    /**
     * Guardar la notificación en Realtime Database para que aparezca en el listado
     * de notificaciones dentro de la app.
     */
    private fun guardarNotificacionEnRTDB(
        type: String,
        message: String,
        relatedUserId: String,
        relatedUserName: String,
        relatedId: String
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val userId = when (type) {
            // Para tipos relacionados al cliente actual, enviar a ellos
            "commission_accepted", "commission_completed" -> {
                // El relatedUserId ya debería ser el cliente
                relatedUserId.ifEmpty { return }
            }
            // Para mensajes y comentarios, enviar al otro usuario
            "new_message", "new_comment", "new_direct_commission" -> {
                relatedUserId.ifEmpty { return }
            }
            else -> return
        }

        // Si la notificación es para el usuario actual, no guardar duplicado
        if (userId == currentUser.uid) return

        val notifRef = FirebaseDatabase.getInstance()
            .getReference("notifications")
            .child(userId)
            .push()

        val notifKey = notifRef.key ?: return
        val notif = NotificationItem(
            id = notifKey,
            type = type,
            message = message,
            relatedUserId = currentUser.uid,
            relatedUserName = currentUser.displayName ?: "Usuario",
            relatedId = relatedId,
            timestamp = System.currentTimeMillis()
        )

        notifRef.setValue(notif)
    }
}
