package com.example.appdoctornow.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "usuario",
    indices = [
        Index(value = ["numeroIdentificacion"], unique = true),
        Index(value = ["correo"], unique = true)
    ]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val numeroIdentificacion: String, // Cédula o pasaporte (único)
    val nombres: String,
    val apellidos: String,
    val correo: String, // Único
    val celular: String,
    val tipoUsuario: String, // Paciente, Administrador/Recepcionista
    val estado: String, // Estado del usuario (activo, inactivo, etc.)
    val contraseña: String // Nuevo campo: contraseña
)