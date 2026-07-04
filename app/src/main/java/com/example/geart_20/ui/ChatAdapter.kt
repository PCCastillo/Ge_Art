package com.example.geart_20.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.geart_20.R
import com.example.geart_20.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(
    private val currentUserId: String,
    private val isClient: Boolean,
    private val messages: MutableList<ChatMessage>,
    private val onAcceptPriceClick: (Double) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun updateMessages(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val msg = messages[position]
        val isMine = msg.senderId == currentUserId

        holder.llMessageRoot.gravity = if (isMine) android.view.Gravity.END else android.view.Gravity.START

        val context = holder.itemView.context
        if (isMine) {
            holder.llBubble.background = ContextCompat.getDrawable(context, R.drawable.bg_chat_bubble_mine)
            holder.tvMessageText.setTextColor(ContextCompat.getColor(context, R.color.geart_ink))
        } else {
            holder.llBubble.background = ContextCompat.getDrawable(context, R.drawable.bg_chat_bubble_other)
            holder.tvMessageText.setTextColor(ContextCompat.getColor(context, R.color.geart_paper))
        }

        if (msg.type == "TEXT") {
            holder.tvMessageText.visibility = View.VISIBLE
            holder.tvMessageText.text = msg.message
        } else {
            holder.tvMessageText.visibility = View.GONE
        }

        if (msg.type == "PROGRESS_IMAGE" || msg.type == "FINAL_PRODUCT") {
            holder.flImageContainer.visibility = View.VISIBLE
            holder.ivMessageImage.visibility = View.VISIBLE
            holder.tvImageLabel.visibility = View.VISIBLE
            Glide.with(holder.itemView.context).load(msg.message).centerCrop().into(holder.ivMessageImage)
            holder.tvImageLabel.text = if (msg.type == "PROGRESS_IMAGE") "Avance de la obra" else "Producto Final Entregado"
        } else {
            holder.flImageContainer.visibility = View.GONE
            holder.ivMessageImage.visibility = View.GONE
            holder.tvImageLabel.visibility = View.GONE
        }

        if (msg.type == "PRICE_UPDATE") {
            holder.llPriceUpdate.visibility = View.VISIBLE
            holder.tvPriceText.text = "$${msg.message}"
            if (isClient && !isMine) {
                holder.btnAcceptPrice.visibility = View.VISIBLE
                holder.btnAcceptPrice.setOnClickListener {
                    val price = msg.message.toDoubleOrNull()
                    if (price != null) onAcceptPriceClick(price)
                }
            } else {
                holder.btnAcceptPrice.visibility = View.GONE
            }
        } else {
            holder.llPriceUpdate.visibility = View.GONE
        }

        holder.tvTimestamp.text = timeFormat.format(Date(msg.timestamp))
    }

    override fun getItemCount() = messages.size

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val llMessageRoot: LinearLayout = itemView.findViewById(R.id.llMessageRoot)
        val llBubble: LinearLayout = itemView.findViewById(R.id.llBubble)
        val tvMessageText: TextView = itemView.findViewById(R.id.tvMessageText)
        val flImageContainer: FrameLayout = itemView.findViewById(R.id.flImageContainer)
        val ivMessageImage: ImageView = itemView.findViewById(R.id.ivMessageImage)
        val tvImageLabel: TextView = itemView.findViewById(R.id.tvImageLabel)
        val llPriceUpdate: LinearLayout = itemView.findViewById(R.id.llPriceUpdate)
        val tvPriceText: TextView = itemView.findViewById(R.id.tvPriceText)
        val btnAcceptPrice: Button = itemView.findViewById(R.id.btnAcceptPrice)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
    }
}
