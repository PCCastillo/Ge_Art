package com.example.geart_20

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geart_20.model.Conversation
import com.example.geart_20.ui.ConversationAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatListActivity : AppCompatActivity() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val allConversations = mutableListOf<Conversation>()
    private val displayedConversations = mutableListOf<Conversation>()
    private lateinit var adapter: ConversationAdapter
    private var currentFilter = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        val rvConversations = findViewById<RecyclerView>(R.id.rvConversations)
        adapter = ConversationAdapter(displayedConversations) { conv ->
            when (conv.type) {
                "commission" -> {
                    startActivity(Intent(this, ChatActivity::class.java).apply {
                        putExtra("COMMISSION_ID", conv.commissionId)
                        putExtra("COMMISSION_TITLE", conv.title)
                        putExtra("IS_CLIENT", conv.clientId == currentUserId)
                    })
                }
                "personal" -> {
                    startActivity(Intent(this, ChatActivity::class.java).apply {
                        putExtra("CHAT_ID", conv.id)
                        putExtra("CHAT_TITLE", conv.title)
                        putExtra("OTHER_USER_ID", conv.otherUserId)
                        putExtra("IS_PERSONAL", true)
                    })
                }
            }
        }
        rvConversations.layoutManager = LinearLayoutManager(this)
        rvConversations.adapter = adapter

        val btnAll = findViewById<Button>(R.id.btnFilterAll)
        val btnCommissions = findViewById<Button>(R.id.btnFilterCommissions)
        val btnPersonal = findViewById<Button>(R.id.btnFilterPersonal)

        fun updateChips(selected: Button) {
            val selectedBg = R.drawable.bg_chip_selected
            val unselectedBg = R.drawable.bg_chip_unselected
            btnAll.setBackgroundResource(if (selected == btnAll) selectedBg else unselectedBg)
            btnCommissions.setBackgroundResource(if (selected == btnCommissions) selectedBg else unselectedBg)
            btnPersonal.setBackgroundResource(if (selected == btnPersonal) selectedBg else unselectedBg)
            val secondaryColor = androidx.core.content.ContextCompat.getColor(this@ChatListActivity, R.color.text_secondary)
            btnAll.setTextColor(if (selected == btnAll) android.graphics.Color.WHITE else secondaryColor)
            btnCommissions.setTextColor(if (selected == btnCommissions) android.graphics.Color.WHITE else secondaryColor)
            btnPersonal.setTextColor(if (selected == btnPersonal) android.graphics.Color.WHITE else secondaryColor)
        }

        btnAll.setOnClickListener { currentFilter = "all"; aplicarFiltro(); updateChips(btnAll) }
        btnCommissions.setOnClickListener { currentFilter = "commission"; aplicarFiltro(); updateChips(btnCommissions) }
        btnPersonal.setOnClickListener { currentFilter = "personal"; aplicarFiltro(); updateChips(btnPersonal) }
        updateChips(btnAll)

        cargarConversaciones()
    }

    private fun aplicarFiltro() {
        displayedConversations.clear()
        for (conv in allConversations) {
            if (currentFilter == "all" || conv.type == currentFilter) {
                displayedConversations.add(conv)
            }
        }
        adapter.updateList(displayedConversations.toList())
    }

    private fun cargarConversaciones() {
        allConversations.clear()

        FirebaseDatabase.getInstance().getReference("commissions")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        val clientId = data.child("clientId").value?.toString() ?: ""
                        val artistId = data.child("artistId").value?.toString() ?: ""
                        if (clientId == currentUserId || artistId == currentUserId) {
                            val title = data.child("title").value?.toString() ?: "Comisión"
                            val status = data.child("status").value?.toString() ?: ""
                            val statusText = when (status) {
                                "PENDING" -> "Pendiente"
                                "DIRECT_REQUEST" -> "Solicitud directa"
                                "ACCEPTED" -> "En progreso"
                                "COMPLETED" -> "Finalizada"
                                "CANCELED" -> "Cancelada"
                                else -> status
                            }
                            allConversations.add(Conversation(
                                id = data.key ?: "",
                                type = "commission",
                                title = title,
                                subtitle = "Comisión - $statusText",
                                commissionId = data.key ?: "",
                                lastTimestamp = 0L,
                                clientId = clientId
                            ))
                        }
                    }
                    cargarChatsPersonales()
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatListActivity, "Error al cargar chats", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun cargarChatsPersonales() {
        FirebaseDatabase.getInstance().getReference("userPersonalChats")
            .child(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        val chatId = data.key ?: continue
                        val otherUserId = data.child("otherUserId").value?.toString() ?: continue
                        val otherUserName = data.child("otherUserName").value?.toString() ?: "Usuario"
                        val lastMessage = data.child("lastMessage").value?.toString() ?: ""
                        val lastTimestamp = data.child("lastTimestamp").value as? Long ?: 0L

                        allConversations.add(Conversation(
                            id = chatId,
                            type = "personal",
                            title = otherUserName,
                            subtitle = "Chat personal",
                            lastMessage = lastMessage,
                            lastTimestamp = lastTimestamp,
                            otherUserId = otherUserId
                        ))
                    }
                    allConversations.sortByDescending { it.lastTimestamp }
                    aplicarFiltro()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
