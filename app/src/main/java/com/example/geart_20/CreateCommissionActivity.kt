package com.example.geart_20

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.geart_20.model.Commission
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CreateCommissionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_commission)

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val etPrice = findViewById<EditText>(R.id.etPrice)
        val etReferenceUrl = findViewById<EditText>(R.id.etReferenceUrl) // El nuevo campo para el link
        val btnPublish = findViewById<Button>(R.id.btnPublishCommission)

        btnPublish.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val desc = etDescription.text.toString().trim()
            val priceStr = etPrice.text.toString().trim()
            val referenceUrl = etReferenceUrl.text.toString().trim() // Capturamos el texto del enlace
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            if (title.isEmpty() || desc.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Debes ingresar título, descripción y precio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentUserId == null) return@setOnClickListener

            btnPublish.isEnabled = false

            val dbRef = FirebaseDatabase.getInstance().getReference("commissions")
            val commissionId = dbRef.push().key ?: return@setOnClickListener

            // --- LÓGICA DE COMISIÓN DIRECTA ---
            // Recuperamos el ID si venimos del perfil de un artista
            val targetArtistId = intent.getStringExtra("TARGET_ARTIST_ID") ?: ""

            // Si hay un artista objetivo, el estado es directo. Si no, es público.
            val initialStatus = if (targetArtistId.isNotEmpty()) "DIRECT_REQUEST" else "PENDING"

            // Creamos la comisión
            val commission = Commission(
                id = commissionId,
                clientId = currentUserId,
                artistId = targetArtistId, // Si es público estará vacío, si es directo tendrá el ID
                title = title,
                description = desc,
                status = initialStatus, // Será "DIRECT_REQUEST" o "PENDING"
                price = priceStr.toDoubleOrNull() ?: 0.0,
                referenceImageUrl = referenceUrl
            )

            dbRef.child(commissionId).setValue(commission)
                .addOnSuccessListener {
                    Toast.makeText(this, "¡Comisión publicada con éxito!", Toast.LENGTH_SHORT).show()

                    if (targetArtistId.isNotEmpty()) {
                        val notifId = FirebaseDatabase.getInstance().getReference("notifications").child(targetArtistId).push().key ?: return@addOnSuccessListener
                        FirebaseDatabase.getInstance().getReference("users").child(currentUserId).get()
                            .addOnSuccessListener { snap ->
                                val clientName = snap.child("name").value?.toString() ?: "Un cliente"
                                val notif = com.example.geart_20.model.NotificationItem(
                                    id = notifId,
                                    type = "new_direct_commission",
                                    message = "$clientName te envió una solicitud directa: $title",
                                    relatedUserId = currentUserId,
                                    relatedUserName = clientName,
                                    relatedId = commissionId,
                                    timestamp = System.currentTimeMillis()
                                )
                                FirebaseDatabase.getInstance().getReference("notifications").child(targetArtistId).child(notifId).setValue(notif)
                            }
                    }

                    finish()
                }
                .addOnFailureListener {
                    btnPublish.isEnabled = true
                    Toast.makeText(this, "Error al publicar.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}