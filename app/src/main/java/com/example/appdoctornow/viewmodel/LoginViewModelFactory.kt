package com.example.appdoctornow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appdoctornow.data.repository.LoginRepository

class LoginViewModelFactory(private val repository: LoginRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}