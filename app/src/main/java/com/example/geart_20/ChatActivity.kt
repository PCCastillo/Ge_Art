package com.example.geart_20

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geart_20.model.ChatMessage
import com.example.geart_20.ui.ChatAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatActivity : AppCompatActivity() {

    private var commissionId: String = ""
    private var chatId: String = ""
    private var isClient: Boolean = false
    private var isPersonal: Boolean = false
    private var otherUserId: String = ""
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val dbChats = FirebaseDatabase.getInstance().getReference("chats")
    private val dbPersonalChats = FirebaseDatabase.getInstance().getReference("personalChats")
    private val dbCommissions = FirebaseDatabase.getInstance().getReference("commissions")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        isPersonal = intent.getBooleanExtra("IS_PERSONAL", false)

        if (isPersonal) {
            chatId = intent.getStringExtra("CHAT_ID") ?: return
            otherUserId = intent.getStringExtra("OTHER_USER_ID") ?: ""
            val chatTitle = intent.getStringExtra("CHAT_TITLE") ?: "Chat personal"
            findViewById<TextView>(R.id.tvChatTitle).text = chatTitle
            findViewById<LinearLayout>(R.id.llActions).visibility = View.GONE
        } else {
            commissionId = intent.getStringExtra("COMMISSION_ID") ?: return
            isClient = intent.getBooleanExtra("IS_CLIENT", false)
            val commissionTitle = intent.getStringExtra("COMMISSION_TITLE") ?: "Chat de Comisión"
            findViewById<TextView>(R.id.tvChatTitle).text = commissionTitle
            val llActions = findViewById<LinearLayout>(R.id.llActions)
            if (isClient) {
                llActions.visibility = View.GONE
            }
            setupButtons()
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val rvChatMessages = findViewById<RecyclerView>(R.id.rvChatMessages)
        val messagesList = mutableListOf<ChatMessage>()

        val adapter = ChatAdapter(currentUserId, isClient, messagesList) { nuevoPrecio ->
            aceptarNuevoPrecio(nuevoPrecio)
        }

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        rvChatMessages.layoutManager = layoutManager
        rvChatMessages.adapter = adapter

        val ref = if (isPersonal) {
            dbPersonalChats.child(chatId).child("messages")
        } else {
            dbChats.child(commissionId)
        }

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messagesList.clear()
                for (data in snapshot.children) {
                    val msg = data.getValue(ChatMessage::class.java)
                    if (msg != null) messagesList.add(msg)
                }
                adapter.updateMessages(messagesList)
                if (messagesList.isNotEmpty()) {
                    rvChatMessages.scrollToPosition(messagesList.size - 1)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        findViewById<Button>(R.id.btnSendText).setOnClickListener {
            val etMessage = findViewById<EditText>(R.id.etMessage)
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                enviarMensaje(text, "TEXT")
                etMessage.text.clear()
            }
        }
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnRenegotiate).setOnClickListener {
            mostrarDialogoInput("Renegociar Precio", "Ingresa el nuevo monto ($):", "PRICE_UPDATE")
        }

        findViewById<Button>(R.id.btnSendProgress).setOnClickListener {
            mostrarDialogoInput("Enviar Avance", "Pega el link de la imagen de progreso:", "PROGRESS_IMAGE")
        }

        findViewById<Button>(R.id.btnSendFinal).setOnClickListener {
            mostrarDialogoInput("Entregar Final", "Pega el link de la obra final (Esto completará la comisión):", "FINAL_PRODUCT")
        }
    }

    private fun mostrarDialogoInput(titulo: String, hint: String, tipoMensaje: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(titulo)

        val input = EditText(this).apply {
            this.hint = hint
            if (tipoMensaje == "PRICE_UPDATE") {
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            }
        }

        val container = LinearLayout(this).apply {
            setPadding(48, 16, 48, 16)
            addView(input)
        }
        builder.setView(container)

        builder.setPositiveButton("Enviar") { _, _ ->
            val contenido = input.text.toString().trim()
            if (contenido.isNotEmpty()) {
                enviarMensaje(contenido, tipoMensaje)
                if (tipoMensaje == "FINAL_PRODUCT") {
                    entregarProductoFinal(contenido)
                }
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun enviarMensaje(contenido: String, tipo: String) {
        if (isPersonal) {
            val msgId = dbPersonalChats.child(chatId).child("messages").push().key ?: return
            val chatMsg = ChatMessage(msgId, currentUserId, contenido, tipo)
            dbPersonalChats.child(chatId).child("messages").child(msgId).setValue(chatMsg)
            dbPersonalChats.child(chatId).child("lastMessage").setValue(contenido)
            dbPersonalChats.child(chatId).child("lastTimestamp").setValue(System.currentTimeMillis())
            dbPersonalChats.child(chatId).child("lastSender").setValue(currentUserId)

            if (otherUserId.isNotEmpty()) {
                val notifId = FirebaseDatabase.getInstance().getReference("notifications").child(otherUserId).push().key ?: return
                val notif = com.example.geart_20.model.NotificationItem(
                    id = notifId,
                    type = "new_message",
                    message = "Nuevo mensaje: $contenido",
                    relatedUserId = currentUserId,
                    relatedId = chatId,
                    timestamp = System.currentTimeMillis()
                )
                FirebaseDatabase.getInstance().getReference("notifications").child(otherUserId).child(notifId).setValue(notif)
            }
        } else {
            val msgId = dbChats.child(commissionId).push().key ?: return
            val chatMsg = ChatMessage(msgId, currentUserId, contenido, tipo)
            dbChats.child(commissionId).child(msgId).setValue(chatMsg)
        }
    }

    private fun aceptarNuevoPrecio(nuevoPrecio: Double) {
        dbCommissions.child(commissionId).child("price").setValue(nuevoPrecio).addOnSuccessListener {
            Toast.makeText(this, "Precio actualizado a $$nuevoPrecio", Toast.LENGTH_SHORT).show()
            enviarMensaje("El cliente ha aceptado el nuevo precio de $$nuevoPrecio.", "TEXT")
        }
    }

    private fun entregarProductoFinal(urlFinal: String) {
        val updates = mapOf(
            "finalProductUrl" to urlFinal,
            "status" to "COMPLETED"
        )
        dbCommissions.child(commissionId).updateChildren(updates).addOnSuccessListener {
            Toast.makeText(this, "¡Obra entregada exitosamente!", Toast.LENGTH_LONG).show()
            dbCommissions.child(commissionId).get().addOnSuccessListener { snap ->
                val clientId = snap.child("clientId").value?.toString() ?: return@addOnSuccessListener
                val title = snap.child("title").value?.toString() ?: "Comisión"
                val notifId = FirebaseDatabase.getInstance().getReference("notifications").child(clientId).push().key ?: return@addOnSuccessListener
                val notif = com.example.geart_20.model.NotificationItem(
                    id = notifId,
                    type = "commission_completed",
                    message = "Tu comisión \"$title\" ha sido completada",
                    relatedId = commissionId,
                    timestamp = System.currentTimeMillis()
                )
                FirebaseDatabase.getInstance().getReference("notifications").child(clientId).child(notifId).setValue(notif)
            }
        }
    }
}
