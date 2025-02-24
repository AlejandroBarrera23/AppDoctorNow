package com.example.appdoctornow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.appdoctornow.model.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insert(usuario: Usuario)

    @Update
    suspend fun update(usuario: Usuario)

    @Query("SELECT * FROM usuario WHERE id = :id")
    suspend fun getUsuarioById(id: Int): Usuario?

    @Query("SELECT * FROM usuario WHERE numeroIdentificacion = :numeroIdentificacion")
    suspend fun getUsuarioByNumeroIdentificacion(numeroIdentificacion: String): Usuario?

    @Query("SELECT * FROM usuario WHERE correo = :correo")
    suspend fun getUsuarioByCorreo(correo: String): Usuario?

    @Query("SELECT * FROM usuario")
    fun getAllUsuarios(): Flow<List<Usuario>>

    @Query("SELECT * FROM usuario WHERE numeroIdentificacion = :numeroIdentificacion AND contraseña = :contraseña")
    suspend fun login(numeroIdentificacion: String, contraseña: String): Usuario?
}