package com.example.geart_20

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geart_20.model.User
import com.example.geart_20.ui.ArtistAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ExploreActivity : AppCompatActivity() {

    private val allArtists = mutableListOf<User>()
    private lateinit var adapter: ArtistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)

        val rvArtists = findViewById<RecyclerView>(R.id.rvArtists)
        val etSearchArtist = findViewById<EditText>(R.id.etSearchArtist)

        adapter = ArtistAdapter(allArtists)
        rvArtists.layoutManager = LinearLayoutManager(this)
        rvArtists.adapter = adapter

        // 1. Descargar solo a los usuarios con rol de ARTIST
        FirebaseDatabase.getInstance().getReference("users")
            .orderByChild("role").equalTo("ARTIST")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    allArtists.clear()
                    for (data in snapshot.children) {
                        val user = data.getValue(User::class.java)
                        if (user != null) {
                            allArtists.add(user)
                        }
                    }
                    // Ordenamos la lista de mayor a menor rating por defecto
                    allArtists.sortByDescending { it.rating }
                    adapter.updateList(allArtists)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ExploreActivity, "Error al cargar artistas", Toast.LENGTH_SHORT).show()
                }
            })

        // 2. Lógica de la barra de búsqueda en tiempo real
        etSearchArtist.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()

                // Filtramos la lista original basándonos en el nombre escrito
                val filteredList = allArtists.filter {
                    it.name.lowercase().contains(query)
                }

                adapter.updateList(filteredList)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}