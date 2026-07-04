package com.example.geart_20

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geart_20.model.Commission
import com.example.geart_20.model.User
import com.example.geart_20.ui.CommissionAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    private var currentUserData: User? = null
    private val dbRef = FirebaseDatabase.getInstance().getReference("commissions")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            irAlLogin()
            return
        }

        lifecycleScope.launch {
            try {
                val snapshot = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.uid).get().await()

                currentUserData = snapshot.getValue(User::class.java)

                if (currentUserData == null) {
                    Toast.makeText(this@MainActivity, "Perfil incompleto.", Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                    irAlLogin()
                } else {
                    if (currentUserData?.role == "ARTIST") {
                        setupArtistPanel()
                    } else {
                        setupClientPanel()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- LÓGICA DEL ARTISTA ---
    // Variable global para recordar qué filtro está activo (Ponla fuera de la función, al nivel de la clase si lo deseas, o dentro funciona igual)
    private var currentArtistFilter = "MARKET"

    private fun setupArtistPanel() {
        setContentView(R.layout.activity_main_artist)
        setupLogout()

        findViewById<TextView>(R.id.tvArtistTitle)?.text = "Mercado de Comisiones - ${currentUserData?.name}"

        // Botones de filtro
        val btnFilterMarket = findViewById<Button>(R.id.btnFilterMarket)
        val btnFilterDirect = findViewById<Button>(R.id.btnFilterDirect)
        val btnFilterMine = findViewById<Button>(R.id.btnFilterMine)

        val recyclerView = findViewById<RecyclerView>(R.id.rvCommissionsArtist)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        val allCommissions = mutableListOf<com.example.geart_20.model.Commission>()
        val displayedCommissions = mutableListOf<com.example.geart_20.model.Commission>()

        val adapter = com.example.geart_20.ui.CommissionAdapter(displayedCommissions) { commission ->
            showCommissionDetailsDialog(commission)
        }
        recyclerView.adapter = adapter

        // Función que filtra la lista maestra basándose en el botón seleccionado
        fun updateDisplayedList() {
            displayedCommissions.clear()
            val artistId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            for (comm in allCommissions) {
                when (currentArtistFilter) {
                    "MARKET" -> if (comm.status == "PENDING" && comm.artistId.isEmpty()) displayedCommissions.add(comm)
                    "DIRECT" -> if (comm.status == "DIRECT_REQUEST" && comm.artistId == artistId) displayedCommissions.add(comm)
                    "MINE" -> if ((comm.status == "ACCEPTED" || comm.status == "COMPLETED") && comm.artistId == artistId) displayedCommissions.add(comm)
                }
            }
            adapter.notifyDataSetChanged()
        }

        // Listeners de los botones
        btnFilterMarket?.setOnClickListener { currentArtistFilter = "MARKET"; updateDisplayedList() }
        btnFilterDirect?.setOnClickListener { currentArtistFilter = "DIRECT"; updateDisplayedList() }
        btnFilterMine?.setOnClickListener { currentArtistFilter = "MINE"; updateDisplayedList() }

        // Descargar TODAS las comisiones y dejar que la función de arriba las filtre
        dbRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                allCommissions.clear()
                for (data in snapshot.children) {
                    val commission = data.getValue(com.example.geart_20.model.Commission::class.java)
                    if (commission != null) {
                        allCommissions.add(commission)
                    }
                }
                updateDisplayedList() // Actualizar la vista automáticamente al recibir datos
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })

        findViewById<Button>(R.id.btnViewProfile)?.setOnClickListener {
            startActivity(android.content.Intent(this, ProfileActivity::class.java))
        }
        findViewById<Button>(R.id.btnChats)?.setOnClickListener {
            startActivity(android.content.Intent(this, ChatListActivity::class.java))
        }
        findViewById<Button>(R.id.btnNotifications)?.setOnClickListener {
            startActivity(android.content.Intent(this, NotificationsActivity::class.java))
        }
    }

    private fun acceptCommissionDialog(commission: Commission) {
        AlertDialog.Builder(this)
            .setTitle("Aceptar Comisión")
            .setMessage("¿Deseas aceptar el trabajo: '${commission.description}' por $${commission.price}?")
            .setPositiveButton("Aceptar") { _, _ ->
                val updates = mapOf(
                    "status" to "ACCEPTED",
                    "artistId" to (FirebaseAuth.getInstance().currentUser?.uid ?: "")
                )
                dbRef.child(commission.id).updateChildren(updates)
                Toast.makeText(this, "¡Comisión aceptada!", Toast.LENGTH_SHORT).show()
                notificarComisionAceptada(commission)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // --- LÓGICA DEL CLIENTE ---
    private fun setupClientPanel() {
        setContentView(R.layout.activity_main_client)
        setupLogout()

        // 1. Botón para abrir el perfil (Fase 4)
        findViewById<Button>(R.id.btnViewProfile)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // 2. Botón para crear nueva comisión
        findViewById<Button>(R.id.btnNewCommission)?.setOnClickListener {
            val intent = Intent(this, CreateCommissionActivity::class.java)
            startActivity(intent)
        }

        // 3. Configurar la lista visual (RecyclerView)
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvCommissionsClient)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        val commissionsList = mutableListOf<Commission>()

        val adapter = com.example.geart_20.ui.CommissionAdapter(commissionsList) { commission ->
            showCommissionDetailsDialog(commission)
        }
        recyclerView.adapter = adapter

        // 4. Leer las comisiones de ESTE cliente desde Firebase
        val clientId = currentUserData?.id ?: return
        FirebaseDatabase.getInstance().getReference("commissions")
            .orderByChild("clientId").equalTo(clientId)
            .addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    commissionsList.clear()
                    for (data in snapshot.children) {
                        val commission = data.getValue(Commission::class.java)
                        commission?.let { commissionsList.add(it) }
                    }
                    adapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
            })

        findViewById<Button>(R.id.btnExploreArtists)?.setOnClickListener {
            startActivity(android.content.Intent(this, ExploreActivity::class.java))
        }
        findViewById<Button>(R.id.btnChats)?.setOnClickListener {
            startActivity(android.content.Intent(this, ChatListActivity::class.java))
        }
        findViewById<Button>(R.id.btnNotifications)?.setOnClickListener {
            startActivity(android.content.Intent(this, NotificationsActivity::class.java))
        }
    }

    private fun setupLogout() {
        findViewById<Button>(R.id.btnLogout)?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            irAlLogin()
        }
    }

    private fun irAlLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun notificarComisionAceptada(commission: Commission) {
        val clientId = commission.clientId
        if (clientId.isEmpty()) return
        val artistName = currentUserData?.name ?: "El artista"
        val notifId = FirebaseDatabase.getInstance().getReference("notifications").child(clientId).push().key ?: return
        val notif = com.example.geart_20.model.NotificationItem(
            id = notifId,
            type = "commission_accepted",
            message = "$artistName aceptó tu comisión \"${commission.title}\"",
            relatedUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            relatedUserName = artistName,
            relatedId = commission.id,
            timestamp = System.currentTimeMillis()
        )
        FirebaseDatabase.getInstance().getReference("notifications").child(clientId).child(notifId).setValue(notif)
    }

    private fun showCommissionDetailsDialog(commission: Commission) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Detalles de la Comisión")

        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(64, 32, 64, 32)

        // 1. Título
        layout.addView(TextView(this).apply {
            text = "Título: ${commission.title}"; textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD); setPadding(0, 0, 0, 16)
        })

        // 2. Descripción
        layout.addView(TextView(this).apply {
            text = "Descripción:\n${commission.description}"; textSize = 16f; setPadding(0, 0, 0, 16)
        })

        // 3. Precio
        layout.addView(TextView(this).apply {
            text = "Precio ofrecido: $${commission.price}"; textSize = 16f
            setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
            setTypeface(null, android.graphics.Typeface.BOLD); setPadding(0, 0, 0, 16)
        })

        // 4. Estado (ACTUALIZADO CON DIRECT_REQUEST)
        val estadoTraducido = when(commission.status) {
            "PENDING" -> "Pendiente de un artista"
            "DIRECT_REQUEST" -> "Solicitud Directa (Privada)"
            "ACCEPTED" -> "En progreso"
            "COMPLETED" -> "Obra finalizada"
            else -> commission.status
        }
        layout.addView(TextView(this).apply {
            text = "Estado actual: $estadoTraducido"; textSize = 15f
            setTypeface(null, android.graphics.Typeface.ITALIC); setPadding(0, 0, 0, 24)
        })

        // 5. Perfiles (Cliente y Artista)
        val tvClient = TextView(this)
        layout.addView(tvClient)
        FirebaseDatabase.getInstance().getReference("users").child(commission.clientId).get().addOnSuccessListener {
            val name = it.child("name").value ?: "Usuario"
            tvClient.text = "👤 Solicitado por: $name (Ver Perfil)"
            tvClient.setTextColor(resources.getColor(android.R.color.holo_blue_dark, null))
            tvClient.setOnClickListener { startActivity(android.content.Intent(this, ProfileActivity::class.java).putExtra("USER_ID", commission.clientId)) }
        }

        if (commission.artistId.isNotEmpty()) {
            val tvArtist = TextView(this)
            layout.addView(tvArtist)
            FirebaseDatabase.getInstance().getReference("users").child(commission.artistId).get().addOnSuccessListener {
                val name = it.child("name").value ?: "Usuario"
                tvArtist.text = "🎨 Asignado a: $name (Ver Perfil)"
                tvArtist.setTextColor(resources.getColor(android.R.color.holo_purple, null))
                tvArtist.setOnClickListener { startActivity(android.content.Intent(this, ProfileActivity::class.java).putExtra("USER_ID", commission.artistId)) }
            }
        }

        // 6. Referencia
        if (commission.referenceImageUrl.isNotEmpty()) {
            layout.addView(TextView(this).apply { text = "Referencia visual adjunta:"; setTypeface(null, android.graphics.Typeface.BOLD); setPadding(0, 16, 0, 8) })
            layout.addView(TextView(this).apply {
                text = commission.referenceImageUrl; textSize = 14f
                android.text.util.Linkify.addLinks(this, android.text.util.Linkify.WEB_URLS)
                movementMethod = android.text.method.LinkMovementMethod.getInstance()
            })
            val imageView = ImageView(this).apply { layoutParams = android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 600); scaleType = ImageView.ScaleType.CENTER_CROP }
            layout.addView(imageView)
            com.bumptech.glide.Glide.with(this).load(commission.referenceImageUrl).into(imageView)
        }

        builder.setView(layout)

        // --- LÓGICA DE BOTONES ---
        // Artista
        if (currentUserData?.role == "ARTIST") {
            if (commission.status == "PENDING") {
                builder.setPositiveButton("Aceptar Trabajo") { _, _ ->
                    dbRef.child(commission.id).updateChildren(mapOf("status" to "ACCEPTED", "artistId" to (FirebaseAuth.getInstance().currentUser?.uid ?: "")))
                }
            }
            // NUEVO: Lógica para solicitudes directas (Aceptar o Rechazar)
            else if (commission.status == "DIRECT_REQUEST" && commission.artistId == FirebaseAuth.getInstance().currentUser?.uid) {
                builder.setPositiveButton("Aceptar Solicitud") { _, _ ->
                    dbRef.child(commission.id).child("status").setValue("ACCEPTED")
                    Toast.makeText(this, "¡Has aceptado la comisión directa!", Toast.LENGTH_SHORT).show()
                    notificarComisionAceptada(commission)
                }
                builder.setNegativeButton("Rechazar") { _, _ ->
                    dbRef.child(commission.id).removeValue()
                    Toast.makeText(this, "Solicitud rechazada", Toast.LENGTH_SHORT).show()
                }
            }
            else if (commission.status == "ACCEPTED" && commission.artistId == FirebaseAuth.getInstance().currentUser?.uid) {
                // EL ARTISTA ENTRA AL CHAT
                builder.setPositiveButton("Abrir Chat") { _, _ ->
                    val intent = android.content.Intent(this, ChatActivity::class.java)
                    intent.putExtra("COMMISSION_ID", commission.id)
                    intent.putExtra("COMMISSION_TITLE", commission.title)
                    intent.putExtra("IS_CLIENT", false) // Es artista
                    startActivity(intent)
                }
            }
        }

        // Cliente
        else if (currentUserData?.role == "CLIENT" && commission.clientId == currentUserData?.id) {
            if (commission.status == "ACCEPTED") {
                // EL CLIENTE ENTRA AL CHAT
                builder.setPositiveButton("Abrir Chat") { _, _ ->
                    val intent = android.content.Intent(this, ChatActivity::class.java)
                    intent.putExtra("COMMISSION_ID", commission.id)
                    intent.putExtra("COMMISSION_TITLE", commission.title)
                    intent.putExtra("IS_CLIENT", true) // Es cliente
                    startActivity(intent)
                }
            } else if (commission.status == "COMPLETED" && !commission.isRated) {
                builder.setPositiveButton("Calificar Artista") { _, _ ->
                    showRatingDialog(commission)
                }
            } else if (commission.isRated) {
                builder.setPositiveButton("Ya calificado", null)
            }
        }

        // Cambiado a NeutralButton para que no sobrescriba el botón de "Rechazar" del Artista
        builder.setNeutralButton("Cerrar", null).show()
    }

    // Nueva función para calificar
    private fun showRatingDialog(commission: Commission) {
        // Validación de seguridad extra
        if (commission.isRated) {
            Toast.makeText(this, "Ya has calificado esta comisión.", Toast.LENGTH_SHORT).show()
            return
        }

        val ratingBar = android.widget.RatingBar(this).apply { numStars = 5; stepSize = 1.0f }
        val container = android.widget.LinearLayout(this).apply { gravity = android.view.Gravity.CENTER; addView(ratingBar) }

        AlertDialog.Builder(this)
            .setTitle("Calificar al Artista")
            .setView(container)
            // Dentro de showRatingDialog...
            // Dentro de showRatingDialog...
            .setPositiveButton("Enviar") { _, _ ->
                val rating = ratingBar.rating.toDouble()
                // PASAMOS EL OBJETO COMMISSION COMPLETO
                updateArtistRating(commission, rating)

                // Ya no es necesario el updateChildren aquí,
                // la transacción de arriba se encarga de todo.
            }.show()
            .show()
    }

    private fun updateArtistRating(commission: Commission, newRating: Double) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(commission.artistId)

        // Primero, verificamos si esta comisión específica YA fue calificada
        // (Asegúrate de que 'isRated' esté en tu modelo Commission)
        if (commission.isRated) {
            Toast.makeText(this, "Esta comisión ya fue calificada.", Toast.LENGTH_SHORT).show()
            return
        }

        userRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val user = mutableData.getValue(User::class.java) ?: return Transaction.success(mutableData)

                val newCount = user.ratingCount + 1
                // Fórmula: (PromedioActual * CantidadActual + NuevaNota) / NuevaCantidad
                val newAverage = ((user.rating * user.ratingCount) + newRating) / newCount

                mutableData.child("rating").value = newAverage
                mutableData.child("ratingCount").value = newCount

                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (committed) {
                    // Una vez que el promedio subió, marcamos la comisión como calificada
                    dbRef.child(commission.id).updateChildren(mapOf("isRated" to true))
                }
            }
        })
    }
}