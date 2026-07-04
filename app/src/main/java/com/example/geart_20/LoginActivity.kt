package com.example.geart_20

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.geart_20.viewmodel.LoginViewModel
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()

    // Registrar callback para solicitud de permiso de notificaciones
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Ya sea concedido o no, el token FCM se generará igual
        if (isGranted) {
            Toast.makeText(this, "Notificaciones activadas", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crear canales de notificación (Android 8+)
        NotificationHelper.createNotificationChannels(this)

        // Solicitar permiso de notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Si ya hay sesión, vamos directo al panel
        if (FirebaseAuth.getInstance().currentUser != null) {
            // Actualizar token FCM al iniciar sesión desde una sesión previa
            actualizarFcmToken()
            irAMain()
            return
        }

        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPass = findViewById<EditText>(R.id.etPass)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val rgRole = findViewById<RadioGroup>(R.id.rgRole)
        val rbArtist = findViewById<RadioButton>(R.id.rbArtist)

        // Candado: Botón de registro bloqueado hasta que elija un rol
        btnRegister.isEnabled = false
        rgRole.setOnCheckedChangeListener { _, checkedId ->
            btnRegister.isEnabled = (checkedId != -1)
        }

        // --- LÓGICA DE REGISTRO ---
        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString().trim()
            val role = if (rbArtist.isChecked) "ARTIST" else "CLIENT"

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val result = viewModel.handleLogin(email, pass, "Usuario Nuevo", role)
                if (result.isSuccess) {
                    actualizarFcmToken()
                    irAMain()
                } else {
                    Toast.makeText(this@LoginActivity, authErrorMessage(result.exceptionOrNull()), Toast.LENGTH_LONG).show()
                }
            }
        }

        // --- LÓGICA DE INGRESO ---
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Ingresa tu correo y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val result = viewModel.handleSignIn(email, pass)
                if (result.isSuccess) {
                    actualizarFcmToken()
                    irAMain()
                } else {
                    Toast.makeText(this@LoginActivity, authErrorMessage(result.exceptionOrNull()), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Traduce el error real de Firebase Auth a un mensaje entendible,
     * en vez de mostrar siempre "Credenciales incorrectas" sin importar la causa.
     */
    private fun authErrorMessage(error: Throwable?): String {
        return when {
            error is FirebaseNetworkException -> "Sin conexión a internet. Revisa tu red o la VPN."
            error is FirebaseAuthException -> when (error.errorCode) {
                "ERROR_INVALID_EMAIL" -> "El correo no tiene un formato válido"
                "ERROR_USER_NOT_FOUND" -> "No existe una cuenta con ese correo"
                "ERROR_WRONG_PASSWORD" -> "Contraseña incorrecta"
                "ERROR_INVALID_CREDENTIAL" -> "Correo o contraseña incorrectos"
                "ERROR_USER_DISABLED" -> "Esta cuenta fue deshabilitada"
                "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Intenta de nuevo más tarde"
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Ese correo ya está registrado"
                "ERROR_WEAK_PASSWORD" -> "La contraseña es muy corta (mín. 6 caracteres)"
                "ERROR_OPERATION_NOT_ALLOWED" -> "El inicio de sesión con correo/contraseña no está habilitado en el proyecto"
                else -> error.message ?: "Error de autenticación (${error.errorCode})"
            }
            else -> error?.message ?: "Ocurrió un error inesperado"
        }
    }

    /**
     * Actualizar el token FCM en Firebase después de login/registro.
     * Esto asegura que el token esté asociado al usuario actual.
     */
    private fun actualizarFcmToken() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.uid)
                .child("fcmToken")
                .setValue(token)
        }
    }

    private fun irAMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
