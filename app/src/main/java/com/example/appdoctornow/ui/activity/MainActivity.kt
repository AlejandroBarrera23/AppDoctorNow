package com.example.appdoctornow.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.appdoctornow.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referencias a los botones
        val btnIniciarSesion = findViewById<Button>(R.id.btnIniciarSesion)
        val btnCrearCuenta = findViewById<Button>(R.id.btnCrearCuenta)

        // Configurar el botón de iniciar sesión
        btnIniciarSesion.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Configurar el botón de crear cuenta
        btnCrearCuenta.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }
}