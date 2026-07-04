package com.example.geart_20.repository

import com.example.geart_20.model.User
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val database = FirebaseDatabase.getInstance().reference
    private val usersRef = database.child("users")

    // Guardar usando el objeto User completo
    suspend fun saveUser(user: User) {
        try {
            usersRef.child(user.id).setValue(user).await()
        } catch (e: Exception) { e.printStackTrace() }
    }

    // Obtener el rol directamente del snapshot convertido a objeto User
    suspend fun getUserRole(uid: String): String? {
        return try {
            val snapshot = database.child("users").child(uid).get().await()
            val user = snapshot.getValue(User::class.java)
            user?.role // Retorna el rol del objeto User
        } catch (e: Exception) { null }
    }


}