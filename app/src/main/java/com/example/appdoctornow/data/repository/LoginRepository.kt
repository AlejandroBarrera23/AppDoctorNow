package com.example.appdoctornow.data.repository

import com.example.appdoctornow.data.local.dao.UsuarioDao
import com.example.appdoctornow.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginRepository(private val usuarioDao: UsuarioDao) {

    suspend fun login(numeroIdentificacion: String, contraseña: String): Usuario? {
        return withContext(Dispatchers.IO) {
            usuarioDao.login(numeroIdentificacion, contraseña)
        }
    }
}