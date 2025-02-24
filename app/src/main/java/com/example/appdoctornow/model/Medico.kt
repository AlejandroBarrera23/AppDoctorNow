package com.example.appdoctornow.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medico")
data class Medico(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombres: String,
    val apellidos: String,
    val especialidad: String,
    val estado: String
){
    override fun toString(): String {
        return "$nombres $apellidos"
    }
}
