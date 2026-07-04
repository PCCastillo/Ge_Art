package com.example.geart_20

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.geart_20.viewmodel.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si ya hay sesión, vamos directo al panel
        if (FirebaseAuth.getInstance().currentUser != null) {
            irAMain()
            return
        }

        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPass = findViewById<EditText>(R.id.etPass)
        val etName = findViewById<EditText>(R.id.etName)
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
            val name = etName.text.toString().trim()
            val role = if (rbArtist.isChecked) "ARTIST" else "CLIENT"

            if (email.isEmpty() || pass.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val success = viewModel.handleLogin(email, pass, name, role)
                if (success) {
                    irAMain()
                } else {
                    Toast.makeText(this@LoginActivity, "Error: El correo ya existe o la contraseña es muy corta (mín. 6 caracteres)", Toast.LENGTH_LONG).show()
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
                val success = viewModel.handleSignIn(email, pass)
                if (success) {
                    irAMain()
                } else {
                    Toast.makeText(this@LoginActivity, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun irAMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}