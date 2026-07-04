package com.example.geart_20

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.geart_20.model.NotificationItem
import com.google.firebase.database.FirebaseDatabase

/**
 * Helper para manejar canales de notificación y mostrar notificaciones push.
 */
object NotificationHelper {

    // Canales
    const val CHANNEL_COMMISSIONS = "geart_commissions"
    const val CHANNEL_CHAT = "geart_chat"
    const val CHANNEL_COMMENTS = "geart_comments"
    const val CHANNEL_GENERAL = "geart_general"

    private const val NOTIFICATION_PREFIX = "geart_"

    /**
     * Crear todos los canales de notificación (Android 8+).
     * Llamar una vez desde Application.onCreate o desde la primera Activity.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channels = listOf(
            NotificationChannel(
                CHANNEL_COMMISSIONS,
                "Comisiones",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Nuevas comisiones, aceptaciones y entregas"
            },
            NotificationChannel(
                CHANNEL_CHAT,
                "Mensajes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Mensajes del chat"
            },
            NotificationChannel(
                CHANNEL_COMMENTS,
                "Comentarios",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Comentarios en tu perfil"
            },
            NotificationChannel(
                CHANNEL_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Otras notificaciones"
            }
        )

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        channels.forEach { manager.createNotificationChannel(it) }
    }

    /**
     * Mostrar una notificación local.
     */
    fun showNotification(
        context: Context,
        title: String,
        message: String,
        channelId: String = CHANNEL_GENERAL,
        notificationId: Int = System.currentTimeMillis().toInt(),
        dataIntent: Intent? = null
    ) {
        val pendingIntent = if (dataIntent != null) {
            PendingIntent.getActivity(
                context,
                notificationId,
                dataIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else null

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_PREFIX, notificationId, builder.build())
        } catch (e: SecurityException) {
            // Permiso de notificaciones no concedido en Android 13+
        }
    }

    /**
     * Navegar según el tipo de notificación.
     */
    fun createNavigationIntent(context: Context, type: String, relatedUserId: String, relatedId: String, relatedUserName: String): Intent {
        return when (type) {
            "new_direct_commission", "commission_accepted", "commission_completed" -> {
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            }
            "new_comment" -> {
                Intent(context, ProfileActivity::class.java).apply {
                    putExtra("USER_ID", relatedUserId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            "new_message" -> {
                Intent(context, ChatActivity::class.java).apply {
                    putExtra("CHAT_ID", relatedId)
                    putExtra("CHAT_TITLE", relatedUserName)
                    putExtra("OTHER_USER_ID", relatedUserId)
                    putExtra("IS_PERSONAL", true)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            else -> {
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            }
        }
    }
}
