package com.example.appdoctornow.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "cita",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["idUsuario"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Medico::class,
            parentColumns = ["id"],
            childColumns = ["idMedico"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Cita(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idUsuario: Int,
    val idMedico: Int,
    val fecha: String,
    val hora: String,
    var estado: String // Cambiado a "var" para permitir modificaciones
)