package com.example.geart_20

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.geart_20.model.Comment
import com.example.geart_20.model.Commission
import com.example.geart_20.model.User
import com.example.geart_20.ui.CommentAdapter
import com.example.geart_20.ui.CommissionAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private var currentUserName = "Anónimo"
    private var currentUserId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val ivProfilePic = findViewById<ImageView>(R.id.ivProfilePic)
        val tvRoleAndRating = findViewById<TextView>(R.id.tvRoleAndRating)
        val etName = findViewById<EditText>(R.id.etName)
        val etProfileImageUrl = findViewById<EditText>(R.id.etProfileImageUrl)
        val etBio = findViewById<EditText>(R.id.etBio)
        val etSocialLinks = findViewById<EditText>(R.id.etSocialLinks)
        val btnSaveProfile = findViewById<Button>(R.id.btnSaveProfile)

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val viewedUserId = intent.getStringExtra("USER_ID") ?: currentUserId
        val isOwnProfile = (viewedUserId == currentUserId)

        if (!isOwnProfile) {
            btnSaveProfile.visibility = View.GONE
            etName.isEnabled = false
            etProfileImageUrl.isEnabled = false
            etBio.isEnabled = false
            etSocialLinks.keyListener = null
            etSocialLinks.isFocusable = false
            findViewById<View>(R.id.etProfileImageUrl).visibility = View.GONE
        }

        val dbRef = FirebaseDatabase.getInstance().getReference("users").child(viewedUserId)

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    etName.setText(user.name)
                    etProfileImageUrl.setText(user.profileImageUrl)
                    etBio.setText(user.bio)
                    etSocialLinks.setText(user.socialLinks)
                    android.text.util.Linkify.addLinks(etSocialLinks, android.text.util.Linkify.WEB_URLS)
                    etSocialLinks.movementMethod = android.text.method.LinkMovementMethod.getInstance()

                    if (user.role == "ARTIST") {
                        tvRoleAndRating.text = "🎨 Artista\n⭐ ${user.rating} (${user.ratingCount} reseñas)"

                        // --- LÓGICA DE COMISIÓN DIRECTA ---
                        val btnDirectCommission = findViewById<Button>(R.id.btnDirectCommission)
                        FirebaseDatabase.getInstance().getReference("users").child(currentUserId).get()
                            .addOnSuccessListener { mySnapshot ->
                                val myRole = mySnapshot.child("role").value?.toString() ?: ""
                                if (myRole == "CLIENT" && !isOwnProfile) {
                                    btnDirectCommission.visibility = View.VISIBLE
                                    btnDirectCommission.setOnClickListener {
                                        val intent = android.content.Intent(this@ProfileActivity, CreateCommissionActivity::class.java)
                                        intent.putExtra("TARGET_ARTIST_ID", viewedUserId)
                                        startActivity(intent)
                                    }
                                }
                            }

                        // --- LÓGICA DE SOLICITUDES DIRECTAS (SOLO DUEÑO) ---
                        if (isOwnProfile) {
                            val tvDirectRequestsTitle = findViewById<TextView>(R.id.tvDirectRequestsTitle)
                            val rvDirectRequests = findViewById<RecyclerView>(R.id.rvDirectRequests)

                            tvDirectRequestsTitle.visibility = View.VISIBLE
                            rvDirectRequests.visibility = View.VISIBLE

                            val directList = mutableListOf<Commission>()
                            val directAdapter = CommissionAdapter(directList) {
                                Toast.makeText(this@ProfileActivity, "Ve a tu Panel Principal para gestionar esta solicitud.", Toast.LENGTH_LONG).show()
                            }
                            rvDirectRequests.adapter = directAdapter
                            rvDirectRequests.layoutManager = LinearLayoutManager(this@ProfileActivity)

                            FirebaseDatabase.getInstance().getReference("commissions")
                                .orderByChild("artistId").equalTo(viewedUserId)
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(commSnapshot: DataSnapshot) {
                                        directList.clear()
                                        for (data in commSnapshot.children) {
                                            val comm = data.getValue(Commission::class.java)
                                            if (comm != null && comm.status == "DIRECT_REQUEST") {
                                                directList.add(comm)
                                            }
                                        }
                                        directAdapter.notifyDataSetChanged()
                                    }
                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }

                        // --- LÓGICA DE PORTAFOLIO ---
                        val tvPortfolioTitle = findViewById<TextView>(R.id.tvPortfolioTitle)
                        val rvPortfolio = findViewById<RecyclerView>(R.id.rvPortfolio)
                        tvPortfolioTitle.visibility = View.VISIBLE
                        rvPortfolio.visibility = View.VISIBLE
                        val portfolioList = mutableListOf<Commission>()
                        val portfolioAdapter = CommissionAdapter(portfolioList) { commission ->
                            mostrarDetalleComision(commission)
                        }
                        rvPortfolio.adapter = portfolioAdapter
                        rvPortfolio.layoutManager = LinearLayoutManager(this@ProfileActivity)
                        FirebaseDatabase.getInstance().getReference("commissions")
                            .orderByChild("artistId").equalTo(viewedUserId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(commSnapshot: DataSnapshot) {
                                    portfolioList.clear()
                                    for (data in commSnapshot.children) {
                                        val comm = data.getValue(Commission::class.java)
                                        if (comm != null && (comm.status == "COMPLETED" || comm.status == "CANCELED")) {
                                            portfolioList.add(comm)
                                        }
                                    }
                                    portfolioAdapter.notifyDataSetChanged()
                                }
                                override fun onCancelled(error: DatabaseError) {}
                            })
                    } else {
                        tvRoleAndRating.text = "👤 Cliente"
                    }

                    if (!isOwnProfile) {
                        val btnChat = findViewById<Button>(R.id.btnChat)
                        btnChat.visibility = View.VISIBLE
                        btnChat.setOnClickListener {
                            iniciarChatPersonal(viewedUserId, user.name)
                        }
                    }

                    if (user.profileImageUrl.isNotEmpty()) {
                        Glide.with(this@ProfileActivity).load(user.profileImageUrl).circleCrop().into(ivProfilePic)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // (Lógica de guardar perfil y comentarios sin cambios...)
        btnSaveProfile.setOnClickListener {
            val updates = mapOf("name" to etName.text.toString().trim(), "profileImageUrl" to etProfileImageUrl.text.toString().trim(), "bio" to etBio.text.toString().trim(), "socialLinks" to etSocialLinks.text.toString().trim())
            dbRef.updateChildren(updates).addOnSuccessListener { Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show() }
        }

        val rvComments = findViewById<RecyclerView>(R.id.rvComments)
        val etNewComment = findViewById<EditText>(R.id.etNewComment)
        val btnPostComment = findViewById<Button>(R.id.btnPostComment)
        val commentList = mutableListOf<Comment>()
        val commentAdapter = CommentAdapter(commentList)
        rvComments.adapter = commentAdapter
        rvComments.layoutManager = LinearLayoutManager(this)
        FirebaseDatabase.getInstance().getReference("comments").child(viewedUserId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentList.clear()
                for (c in snapshot.children) { commentList.add(c.getValue(Comment::class.java)!!) }
                commentAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        btnPostComment.setOnClickListener {
            val text = etNewComment.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener
            // Fetch the user name from Firebase to avoid race condition
            FirebaseDatabase.getInstance().getReference("users").child(currentUserId).get()
                .addOnSuccessListener { snap ->
                    val userName = snap.child("name").value?.toString() ?: "Anónimo"
                    val comment = Comment(FirebaseDatabase.getInstance().getReference("comments").push().key ?: "", currentUserId, userName, text)
                    FirebaseDatabase.getInstance().getReference("comments").child(viewedUserId).child(comment.id).setValue(comment)

                    if (viewedUserId != currentUserId) {
                        val notifId = FirebaseDatabase.getInstance().getReference("notifications").child(viewedUserId).push().key ?: return@addOnSuccessListener
                        val notif = com.example.geart_20.model.NotificationItem(
                            id = notifId,
                            type = "new_comment",
                            message = "$userName comentó en tu perfil: $text",
                            relatedUserId = currentUserId,
                            relatedUserName = userName,
                            timestamp = System.currentTimeMillis()
                        )
                        FirebaseDatabase.getInstance().getReference("notifications").child(viewedUserId).child(notifId).setValue(notif)
                    }
                }
        }
    }

    private fun iniciarChatPersonal(otherUserId: String, otherUserName: String) {
        val uids = listOf(currentUserId, otherUserId).sorted()
        val chatId = "personal_${uids[0]}_${uids[1]}"
        val dbPersonalChats = FirebaseDatabase.getInstance().getReference("personalChats")
        val dbUserChats = FirebaseDatabase.getInstance().getReference("userPersonalChats")

        dbPersonalChats.child(chatId).child("participants").child(currentUserId).setValue(true)
        dbPersonalChats.child(chatId).child("participants").child(otherUserId).setValue(true)

        dbUserChats.child(currentUserId).child(chatId).child("otherUserId").setValue(otherUserId)
        dbUserChats.child(currentUserId).child(chatId).child("otherUserName").setValue(otherUserName)
        dbUserChats.child(currentUserId).child(chatId).child("lastTimestamp").setValue(System.currentTimeMillis())

        FirebaseDatabase.getInstance().getReference("users").child(currentUserId).get()
            .addOnSuccessListener {
                val myName = it.child("name").value?.toString() ?: "Usuario"
                dbUserChats.child(otherUserId).child(chatId).child("otherUserId").setValue(currentUserId)
                dbUserChats.child(otherUserId).child(chatId).child("otherUserName").setValue(myName)
                dbUserChats.child(otherUserId).child(chatId).child("lastTimestamp").setValue(System.currentTimeMillis())
            }

        startActivity(android.content.Intent(this, ChatActivity::class.java).apply {
            putExtra("CHAT_ID", chatId)
            putExtra("CHAT_TITLE", otherUserName)
            putExtra("OTHER_USER_ID", otherUserId)
            putExtra("IS_PERSONAL", true)
        })
    }

    private fun mostrarDetalleComision(commission: Commission) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(commission.title.ifEmpty { "Sin título" })

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(64, 32, 64, 32)

        layout.addView(TextView(this).apply {
            text = commission.description
            textSize = 16f
            setPadding(0, 0, 0, 16)
        })

        layout.addView(TextView(this).apply {
            text = "Precio: $${commission.price}"
            textSize = 16f
            setTextColor(resources.getColor(R.color.blue_primary, null))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 8)
        })

        layout.addView(TextView(this).apply {
            text = "Estado: Obra finalizada"
            textSize = 15f
            setTypeface(null, android.graphics.Typeface.ITALIC)
            setPadding(0, 0, 0, 16)
        })

        val tvClient = TextView(this)
        layout.addView(tvClient)
        FirebaseDatabase.getInstance().getReference("users").child(commission.clientId).get()
            .addOnSuccessListener {
                val name = it.child("name").value ?: "Usuario"
                tvClient.text = "Solicitado por: $name"
                tvClient.setTextColor(resources.getColor(R.color.blue_primary, null))
                tvClient.setPadding(0, 0, 0, 16)
                tvClient.setOnClickListener {
                    startActivity(android.content.Intent(this@ProfileActivity, ProfileActivity::class.java).putExtra("USER_ID", commission.clientId))
                }
            }

        if (commission.finalProductUrl.isNotEmpty()) {
            layout.addView(TextView(this).apply {
                text = "Resultado final:"
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 8, 0, 4)
            })
            val ivFinal = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 600)
                scaleType = ImageView.ScaleType.CENTER_CROP
                setPadding(0, 0, 0, 16)
            }
            layout.addView(ivFinal)
            Glide.with(this).load(commission.finalProductUrl).into(ivFinal)
        }

        if (commission.referenceImageUrl.isNotEmpty()) {
            layout.addView(TextView(this).apply {
                text = "Referencia visual:"
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 8, 0, 4)
            })
            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 600)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            layout.addView(imageView)
            Glide.with(this).load(commission.referenceImageUrl).into(imageView)
        }

        builder.setView(layout)
        builder.setNeutralButton("Cerrar", null).show()
    }
}