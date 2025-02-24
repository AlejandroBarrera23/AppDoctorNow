package com.example.appdoctornow.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.appdoctornow.model.Medico

@Dao
interface MedicoDao {

    @Insert
    suspend fun insert(medico: Medico)

    @Query("SELECT * FROM medico")
    suspend fun getAllMedicos(): List<Medico> // Devuelve una lista de médicos

    @Query("SELECT * FROM medico WHERE especialidad = :especialidad")
    suspend fun getMedicosByEspecialidad(especialidad: String): List<Medico>

    @Query("SELECT * FROM medico WHERE id = :id")
    suspend fun getMedicoById(id: Int): Medico?
}