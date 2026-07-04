package com.example.geart_20

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geart_20.model.NotificationItem
import com.example.geart_20.ui.NotificationAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationsActivity : AppCompatActivity() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val notifications = mutableListOf<NotificationItem>()
    private lateinit var adapter: NotificationAdapter
    private val dbNotifs = FirebaseDatabase.getInstance().getReference("notifications")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        val rvNotifications = findViewById<RecyclerView>(R.id.rvNotifications)
        adapter = NotificationAdapter(notifications) { notif ->
            marcarComoLeida(notif)
            when (notif.type) {
                "new_direct_commission", "commission_accepted", "commission_completed", "commission_canceled" -> {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                "new_comment" -> {
                    startActivity(Intent(this, ProfileActivity::class.java).apply {
                        putExtra("USER_ID", notif.relatedUserId)
                    })
                }
                "new_message" -> {
                    startActivity(Intent(this, ChatActivity::class.java).apply {
                        putExtra("CHAT_ID", notif.relatedId)
                        putExtra("CHAT_TITLE", notif.relatedUserName)
                        putExtra("OTHER_USER_ID", notif.relatedUserId)
                        putExtra("IS_PERSONAL", true)
                    })
                }
            }
        }
        rvNotifications.layoutManager = LinearLayoutManager(this)
        rvNotifications.adapter = adapter

        cargarNotificaciones()
    }

    private fun cargarNotificaciones() {
        dbNotifs.child(currentUserId)
            .orderByChild("timestamp")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    notifications.clear()
                    for (data in snapshot.children) {
                        val notif = data.getValue(NotificationItem::class.java)
                        if (notif != null) {
                            notifications.add(notif)
                        }
                    }
                    notifications.sortByDescending { it.timestamp }
                    adapter.updateList(notifications.toList())
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun marcarComoLeida(notif: NotificationItem) {
        dbNotifs.child(currentUserId).child(notif.id).child("read").setValue(true)
    }
}
