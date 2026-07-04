package com.example.geart_20.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.geart_20.ProfileActivity
import com.example.geart_20.R
import com.example.geart_20.model.User

class ArtistAdapter(
    private var artistList: List<User>
) : RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    class ArtistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPic: ImageView = view.findViewById(R.id.ivArtistPic)
        val tvName: TextView = view.findViewById(R.id.tvArtistName)
        val tvRating: TextView = view.findViewById(R.id.tvArtistRating)
        val btnView: Button = view.findViewById(R.id.btnViewArtist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_artist, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val artist = artistList[position]

        // Si el artista no tiene nombre configurado, mostramos "Artista Anónimo"
        holder.tvName.text = artist.name.ifEmpty { "Artista Anónimo" }

        // Formateamos las estrellas para que solo tengan 1 decimal
        val formattedRating = String.format("%.1f", artist.rating)
        holder.tvRating.text = "$formattedRating (${artist.ratingCount} reseñas)"

        if (artist.profileImageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(artist.profileImageUrl)
                .circleCrop()
                .into(holder.ivPic)
        } else {
            holder.ivPic.setImageResource(android.R.color.transparent) // Limpiar si no hay foto
        }

        // Al hacer clic en "Ver", abrimos su perfil
        holder.btnView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ProfileActivity::class.java)
            intent.putExtra("USER_ID", artist.id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = artistList.size

    // Esta función nos ayudará a actualizar la lista cuando el cliente busque un nombre
    fun updateList(newList: List<User>) {
        artistList = newList
        notifyDataSetChanged()
    }
}