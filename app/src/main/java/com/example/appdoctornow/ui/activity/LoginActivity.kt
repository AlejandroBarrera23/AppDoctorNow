package com.example.appdoctornow.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appdoctornow.R
import com.example.appdoctornow.data.local.database.AppDatabase
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etNumeroIdentificacion: EditText
    private lateinit var etContraseña: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvMensajeError: TextView

    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar vistas
        etNumeroIdentificacion = findViewById(R.id.etNumeroIdentificacion)
        etContraseña = findViewById(R.id.etContraseña)
        btnLogin = findViewById(R.id.btnLogin)
        tvMensajeError = findViewById(R.id.tvMensajeError)
        val btnRegresar = findViewById<Button>(R.id.btnRegresarLogin)

        // Configurar el botón de regresar
        btnRegresar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Cierra la actividad actual
        }

        // Inicializar la base de datos
        database = AppDatabase.getDatabase(this)

        // Configurar el botón de login
        btnLogin.setOnClickListener {
            val numeroIdentificacion = etNumeroIdentificacion.text.toString()
            val contraseña = etContraseña.text.toString()

            if (numeroIdentificacion.isEmpty() || contraseña.isEmpty()) {
                mostrarError("Todos los campos son obligatorios.")
                return@setOnClickListener
            }

            // Llamar a la función de login en una corrutina
            lifecycleScope.launch {
                try {
                    val usuario = database.usuarioDao().login(numeroIdentificacion, contraseña)
                    if (usuario != null) {
                        // Guardar el ID del usuario en SharedPreferences
                        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putInt("user_id", usuario.id) // Guardar el ID del usuario
                        editor.apply()

                        // Inicio de sesión exitoso
                        val intent = when (usuario.tipoUsuario) {
                            "Paciente" -> Intent(this@LoginActivity, DashboardPacienteActivity::class.java)
                            else -> Intent(this@LoginActivity, DashboardAdminActivity::class.java)
                        }
                        startActivity(intent)
                        finish() // Cerrar la actividad de login
                    } else {
                        // Credenciales incorrectas
                        mostrarError("Usuario o contraseña incorrectos.")
                    }
                } catch (e: Exception) {
                    // Manejar errores inesperados
                    mostrarError("Error al iniciar sesión: ${e.message}")
                }
            }
        }
    }

    private fun mostrarError(mensaje: String) {
        tvMensajeError.text = mensaje
        tvMensajeError.visibility = TextView.VISIBLE
    }
}