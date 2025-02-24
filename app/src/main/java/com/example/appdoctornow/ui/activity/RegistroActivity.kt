package com.example.appdoctornow.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appdoctornow.R
import com.example.appdoctornow.data.local.database.AppDatabase
import com.example.appdoctornow.model.Usuario
import com.example.appdoctornow.utils.Validaciones
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistroActivity : AppCompatActivity() {

    private lateinit var etNumeroIdentificacion: EditText
    private lateinit var etNombres: EditText
    private lateinit var etApellidos: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etCelular: EditText
    private lateinit var etContraseña: EditText
    private lateinit var etConfirmarContraseña: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var tvMensajeError: TextView

    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // Inicializar vistas
        etNumeroIdentificacion = findViewById(R.id.etNumeroIdentificacion)
        etNombres = findViewById(R.id.etNombres)
        etApellidos = findViewById(R.id.etApellidos)
        etCorreo = findViewById(R.id.etCorreo)
        etCelular = findViewById(R.id.etCelular)
        etContraseña = findViewById(R.id.etContraseña)
        etConfirmarContraseña = findViewById(R.id.etConfirmarContraseña)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        tvMensajeError = findViewById(R.id.tvMensajeError)
        val btnRegresar = findViewById<Button>(R.id.btnRegresarRegistro)

        // Inicializar la base de datos
        database = AppDatabase.getDatabase(this)

        btnRegresar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Cierra la actividad actual
        }

        // Configurar el botón de registro
        btnRegistrar.setOnClickListener {
            registrarUsuario()
        }

        // Configurar los campos usando las funciones de Validaciones
        Validaciones.configurarCampoNombresApellidos(etNombres)
        Validaciones.configurarCampoNombresApellidos(etApellidos)
        Validaciones.configurarCampoCorreo(etCorreo)
        Validaciones.configurarCampoCelular(etCelular)
    }

    private fun registrarUsuario() {
        val numeroIdentificacion = etNumeroIdentificacion.text.toString()
        val nombres = etNombres.text.toString()
        val apellidos = etApellidos.text.toString()
        val correo = etCorreo.text.toString()
        val celular = etCelular.text.toString()
        val contraseña = etContraseña.text.toString()
        val confirmarContraseña = etConfirmarContraseña.text.toString()

        // Validaciones
        if (numeroIdentificacion.isEmpty() || nombres.isEmpty() || apellidos.isEmpty() ||
            correo.isEmpty() || celular.isEmpty() || contraseña.isEmpty() || confirmarContraseña.isEmpty()
        ) {
            mostrarError("Todos los campos son obligatorios.")
            return
        }

        if (contraseña != confirmarContraseña) {
            mostrarError("Las contraseñas no coinciden.")
            return
        }

        if (!Validaciones.esCorreoValido(correo)) {
            mostrarError("El correo electrónico no es válido.")
            return
        }

        if (!Validaciones.esCelularValido(celular)) {
            mostrarError("El número de celular no es válido.")
            return
        }

        // Verificar duplicados en segundo plano
        CoroutineScope(Dispatchers.IO).launch {
            val existeNumeroIdentificacion = database.usuarioDao().getUsuarioByNumeroIdentificacion(numeroIdentificacion) != null
            val existeCorreo = database.usuarioDao().getUsuarioByCorreo(correo) != null

            withContext(Dispatchers.Main) {
                if (existeNumeroIdentificacion) {
                    mostrarError("El número de cédula o pasaporte ya está registrado.")
                    return@withContext
                }

                if (existeCorreo) {
                    mostrarError("El correo electrónico ya está registrado.")
                    return@withContext
                }

                // Crear el usuario
                val usuario = Usuario(
                    numeroIdentificacion = numeroIdentificacion,
                    nombres = nombres,
                    apellidos = apellidos,
                    correo = correo,
                    celular = celular,
                    tipoUsuario = "Paciente", // Por defecto es paciente
                    estado = "Activo",
                    contraseña = contraseña
                )

                // Insertar el usuario en la base de datos
                CoroutineScope(Dispatchers.IO).launch {
                    database.usuarioDao().insert(usuario)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegistroActivity, "Usuario registrado exitosamente.", Toast.LENGTH_SHORT).show()
                        finish() // Cerrar la actividad de registro
                    }
                }
            }
        }
    }

    private fun mostrarError(mensaje: String) {
        tvMensajeError.text = mensaje
        tvMensajeError.visibility = TextView.VISIBLE
    }
}