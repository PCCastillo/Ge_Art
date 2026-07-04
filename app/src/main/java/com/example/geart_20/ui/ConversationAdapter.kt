package com.example.geart_20.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.geart_20.R
import com.example.geart_20.model.Conversation

class ConversationAdapter(
    private var conversations: List<Conversation>,
    private val onItemClick: (Conversation) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

    class ConversationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvConvTitle)
        val tvSubtitle: TextView = view.findViewById(R.id.tvConvSubtitle)
        val tvLastMessage: TextView = view.findViewById(R.id.tvConvLastMessage)
        val tvTimestamp: TextView = view.findViewById(R.id.tvConvTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_conversation, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val conv = conversations[position]
        holder.tvTitle.text = conv.title
        holder.tvSubtitle.text = conv.subtitle
        holder.tvLastMessage.text = conv.lastMessage.ifEmpty { "Sin mensajes aún" }
        holder.tvTimestamp.text = formatTimestamp(conv.lastTimestamp)
        holder.itemView.setOnClickListener { onItemClick(conv) }
    }

    override fun getItemCount() = conversations.size

    fun updateList(newList: List<Conversation>) {
        conversations = newList
        notifyDataSetChanged()
    }

    private fun formatTimestamp(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val sdf = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}
