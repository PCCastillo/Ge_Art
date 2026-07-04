package com.example.geart_20.ui

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
    private var messages: List<ChatMessage>,
    private val onAcceptPriceClick: (messageId: String, price: Double) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llMessageRoot: LinearLayout = view.findViewById(R.id.llMessageRoot)
        val llBubble: LinearLayout = view.findViewById(R.id.llBubble)
        val tvMessageText: TextView = view.findViewById(R.id.tvMessageText)
        val ivMessageImage: ImageView = view.findViewById(R.id.ivMessageImage)
        val tvImageLabel: TextView = view.findViewById(R.id.tvImageLabel)
        val llPriceUpdate: LinearLayout = view.findViewById(R.id.llPriceUpdate)
        val tvPriceText: TextView = view.findViewById(R.id.tvPriceText)
        val btnAcceptPrice: Button = view.findViewById(R.id.btnAcceptPrice)
        val tvTimestamp: TextView = view.findViewById(R.id.tvTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = messages[position]

        // 1. Alineación visual: Mis mensajes a la derecha (azules), los de la otra persona a la izquierda (grises)
        if (chat.senderId == currentUserId) {
            holder.llMessageRoot.gravity = Gravity.END
            holder.llBubble.setBackgroundResource(R.drawable.bg_chat_sent)
            holder.tvMessageText.setTextColor(android.graphics.Color.WHITE)
            holder.tvImageLabel.setTextColor(android.graphics.Color.WHITE)
            holder.tvPriceText.setTextColor(android.graphics.Color.WHITE)
            holder.tvTimestamp.gravity = Gravity.END
        } else {
            holder.llMessageRoot.gravity = Gravity.START
            holder.llBubble.setBackgroundResource(R.drawable.bg_chat_received)
            holder.tvMessageText.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.context, R.color.text_secondary))
            holder.tvImageLabel.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.context, R.color.text_secondary))
            holder.tvPriceText.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.context, R.color.text_secondary))
            holder.tvTimestamp.gravity = Gravity.START
        }

        // Formateamos la hora (Ej: 14:30)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        holder.tvTimestamp.text = sdf.format(Date(chat.timestamp))

        // 2. IMPORTANTE: Ocultar todo antes de dibujar (para evitar que se reciclen vistas incorrectas al hacer scroll)
        holder.tvMessageText.visibility = View.GONE
        holder.ivMessageImage.visibility = View.GONE
        holder.tvImageLabel.visibility = View.GONE
        holder.llPriceUpdate.visibility = View.GONE
        holder.btnAcceptPrice.visibility = View.GONE

        // 3. Activar los elementos según el TIPO de mensaje
        when (chat.type) {
            "TEXT" -> {
                holder.tvMessageText.visibility = View.VISIBLE
                holder.tvMessageText.text = chat.message
            }
            "PROGRESS_IMAGE" -> {
                holder.ivMessageImage.visibility = View.VISIBLE
                holder.tvImageLabel.visibility = View.VISIBLE
                holder.tvImageLabel.text = "Avance de la obra"
                Glide.with(holder.itemView.context).load(chat.message).into(holder.ivMessageImage)
            }
            "FINAL_PRODUCT" -> {
                holder.ivMessageImage.visibility = View.VISIBLE
                holder.tvImageLabel.visibility = View.VISIBLE
                holder.tvImageLabel.text = "Producto Final Entregado"
                Glide.with(holder.itemView.context).load(chat.message).into(holder.ivMessageImage)
            }
            "PRICE_UPDATE" -> {
                holder.llPriceUpdate.visibility = View.VISIBLE
                val price = chat.message.toDoubleOrNull() ?: 0.0
                holder.tvPriceText.text = if (chat.accepted) {
                    "✓ Precio de $$price aceptado"
                } else {
                    "Propuesta de nuevo precio: $$price"
                }

                if (chat.senderId != currentUserId && !chat.accepted) {
                    holder.btnAcceptPrice.visibility = View.VISIBLE
                    holder.btnAcceptPrice.setOnClickListener {
                        holder.btnAcceptPrice.visibility = View.GONE
                        onAcceptPriceClick(chat.id, price)
                    }
                }
            }
        }
    }

    override fun getItemCount() = messages.size

    fun updateMessages(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }
}