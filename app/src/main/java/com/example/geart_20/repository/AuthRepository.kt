package com.example.geart_20.repository

import com.example.geart_20.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().getReference("users")

    suspend fun registerUser(email: String, pass: String, name: String, role: String): Boolean {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: return false

            val newUser = User(id = uid, name = name, email = email, role = role)
            db.child(uid).setValue(newUser).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun signIn(email: String, pass: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}