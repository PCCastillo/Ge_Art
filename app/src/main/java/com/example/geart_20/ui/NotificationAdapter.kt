package com.example.geart_20.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.geart_20.R
import com.example.geart_20.model.NotificationItem

class NotificationAdapter(
    private var notifications: List<NotificationItem>,
    private val onItemClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tvNotifMessage)
        val tvTimestamp: TextView = view.findViewById(R.id.tvNotifTimestamp)
        val tvType: TextView = view.findViewById(R.id.tvNotifType)
        val viewUnread: View = view.findViewById(R.id.viewUnreadDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notif = notifications[position]
        holder.tvMessage.text = notif.message
        holder.tvTimestamp.text = formatTimestamp(notif.timestamp)
        holder.tvType.text = typeLabel(notif.type)
        holder.viewUnread.visibility = if (!notif.read) View.VISIBLE else View.GONE
        holder.itemView.setOnClickListener { onItemClick(notif) }
    }

    override fun getItemCount() = notifications.size

    fun updateList(newList: List<NotificationItem>) {
        notifications = newList
        notifyDataSetChanged()
    }

    private fun formatTimestamp(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val sdf = java.text.SimpleDateFormat("dd/MM/yy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    private fun typeLabel(type: String): String = when (type) {
        "new_comment" -> "Nuevo comentario"
        "new_message" -> "Nuevo mensaje"
        "new_direct_commission" -> "Nueva solicitud directa"
        "commission_accepted" -> "Comisión aceptada"
        "commission_completed" -> "Comisión finalizada"
        else -> type
    }
}
