package com.example.appdoctornow.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdoctornow.data.local.dao.UsuarioDao
import com.example.appdoctornow.data.repository.LoginRepository
import com.example.appdoctornow.model.Usuario
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: LoginRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> get() = _loginResult

    fun login(numeroIdentificacion: String, contraseña: String) {
        viewModelScope.launch {
            val usuario = repository.login(numeroIdentificacion, contraseña)
            if (usuario != null) {
                _loginResult.value = LoginResult.Success(usuario)
            } else {
                _loginResult.value = LoginResult.Error("Usuario o contraseña incorrectos.")
            }
        }
    }
}

sealed class LoginResult {
    data class Success(val usuario: Usuario) : LoginResult()
    data class Error(val mensaje: String) : LoginResult()
}