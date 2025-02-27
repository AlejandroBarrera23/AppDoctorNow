package com.example.appdoctornow.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appdoctornow.R
import com.example.appdoctornow.data.local.database.AppDatabase
import kotlinx.coroutines.launch

class LoginAdminActivity : AppCompatActivity() {

    private lateinit var etNumeroIdentificacionAdmin: EditText
    private lateinit var etContraseñaAdmin: EditText
    private lateinit var btnLoginAdmin: Button
    private lateinit var tvMensajeErrorAdmin: TextView

    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_admin)

        // Inicializar vistas
        etNumeroIdentificacionAdmin = findViewById(R.id.etNumeroIdentificacionAdmin)
        etContraseñaAdmin = findViewById(R.id.etContraseñaAdmin)
        btnLoginAdmin = findViewById(R.id.btnLoginAdmin)
        tvMensajeErrorAdmin = findViewById(R.id.tvMensajeErrorAdmin)
        val btnRegresar = findViewById<Button>(R.id.btnRegresarLoginAdmin)

        // Configurar el botón de regresar
        btnRegresar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Cierra la actividad actual
        }

        // Inicializar la base de datos
        database = AppDatabase.getDatabase(this)

        // Configurar el botón de login
        btnLoginAdmin.setOnClickListener {
            val usuario = etNumeroIdentificacionAdmin.text.toString()
            val contraseña = etContraseñaAdmin.text.toString()

            if (usuario.isEmpty() || contraseña.isEmpty()) {
                mostrarError("Todos los campos son obligatorios.")
                return@setOnClickListener
            }

            // Llamar a la función de login en una corrutina
            lifecycleScope.launch {
                try {
                    val usuario = database.usuarioDao().login(usuario, contraseña)
                    if (usuario != null) {
                        // Verificar si el usuario es de tipo "Administrador" o "Recepcionista"
                        if (usuario.tipoUsuario == "Administrador" || usuario.tipoUsuario == "Recepcionista") {
                            // Guardar el ID del usuario en SharedPreferences
                            val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putInt("user_id", usuario.id) // Guardar el ID del usuario
                            editor.apply()

                            // Redirigir según el tipo de usuario
                            val intent = when (usuario.tipoUsuario) {
                                "Administrador" -> Intent(this@LoginAdminActivity, DashboardAdminActivity::class.java)
                                "Recepcionista" -> Intent(this@LoginAdminActivity, DashboardAdminActivity::class.java)
                                else -> Intent(this@LoginAdminActivity, MainActivity::class.java) // Caso por defecto
                            }
                            startActivity(intent)
                            finish() // Cerrar la actividad de login
                        } else {
                            // Mostrar mensaje de error si el usuario no es de tipo "Administrador" o "Recepcionista"
                            mostrarError("Solo administradores y recepcionistas pueden iniciar sesión.")
                        }
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
        tvMensajeErrorAdmin.text = mensaje
        tvMensajeErrorAdmin.visibility = TextView.VISIBLE
    }
}