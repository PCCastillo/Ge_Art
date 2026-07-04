package com.example.geart_20.repository

import com.example.geart_20.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().getReference("users")

    suspend fun registerUser(email: String, pass: String, name: String, role: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid
                ?: return Result.failure(IllegalStateException("No se pudo crear el usuario"))

            val newUser = User(id = uid, name = name, email = email, role = role)
            db.child(uid).setValue(newUser).await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, pass: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}