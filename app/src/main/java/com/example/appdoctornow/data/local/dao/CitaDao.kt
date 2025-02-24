package com.example.appdoctornow.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.appdoctornow.model.Cita

@Dao
interface CitaDao {

    @Insert
    suspend fun insert(cita: Cita)

    @Query("SELECT * FROM cita WHERE idUsuario = :idUsuario AND estado = 'Confirmada'")
    suspend fun getCitasByUsuario(idUsuario: Int): List<Cita>

    @Query("SELECT * FROM cita WHERE fecha = :fecha AND hora = :hora AND estado = 'Confirmada'")
    suspend fun getCitaByFechaHora(fecha: String, hora: String): Cita?

    @Query("SELECT hora FROM cita WHERE fecha = :fecha")
    suspend fun getHorasOcupadas(fecha: String): List<String>

    @Query("SELECT * FROM cita WHERE idUsuario = :idUsuario AND fecha = :fecha AND hora = :hora AND estado IN ('Confirmada', 'Pendiente')")
    suspend fun getCitaByUsuarioYFechaHora(idUsuario: Int, fecha: String, hora: String): Cita?

    @Query("SELECT hora FROM cita WHERE fecha = :fecha AND idMedico = :idMedico AND estado IN ('Confirmada', 'Pendiente')")
    suspend fun getHorasOcupadas(fecha: String, idMedico: Int): List<String>

    @Update
    suspend fun update(cita: Cita)
}