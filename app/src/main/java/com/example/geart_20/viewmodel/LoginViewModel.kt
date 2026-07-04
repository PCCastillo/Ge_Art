package com.example.geart_20.viewmodel

import androidx.lifecycle.ViewModel
import com.example.geart_20.repository.AuthRepository

class LoginViewModel : ViewModel() {
    private val authRepo = AuthRepository()

    suspend fun handleLogin(email: String, pass: String, name: String, role: String): Result<Unit> {
        return authRepo.registerUser(email, pass, name, role)
    }

    // Esta función debe existir
    suspend fun handleSignIn(email: String, pass: String): Result<Unit> {
        return authRepo.signIn(email, pass)
    }
}