package com.example.appdoctornow.utils

import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.widget.EditText
import java.util.regex.Pattern

object Validaciones {

    // Validar si un correo electrónico es válido
    fun esCorreoValido(correo: String): Boolean {
        val regex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$")
        return regex.matches(correo)
    }

    // Validar si un número de celular es válido
    fun esCelularValido(celular: String): Boolean {
        // Validar que tenga entre 7 y 15 dígitos (ajusta según tu país)
        return celular.length in 7..15
    }

    // Configurar un campo para nombres y apellidos (mayúsculas, solo letras, espacios y tildes)
    fun configurarCampoNombresApellidos(editText: EditText) {
        // Convertir a mayúsculas conforme se escribe
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val texto = s.toString().uppercase()
                if (texto != s.toString()) {
                    editText.setText(texto)
                    editText.setSelection(texto.length) // Mover el cursor al final
                }
            }
        })

        // Filtrar caracteres no permitidos (solo letras, espacios y tildes)
        editText.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            val regex = Regex("[^A-Za-z áéíóúÁÉÍÓÚ]")
            if (regex.containsMatchIn(source)) {
                ""
            } else {
                null
            }
        })
    }

    // Configurar un campo de correo electrónico (minúsculas y validación de formato)
    fun configurarCampoCorreo(editText: EditText) {
        // Convertir a minúsculas conforme se escribe
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val texto = s.toString().lowercase()
                if (texto != s.toString()) {
                    editText.setText(texto)
                    editText.setSelection(texto.length) // Mover el cursor al final
                }
            }
        })
    }

    // Configurar un campo de celular (solo números y validación de longitud)
    fun configurarCampoCelular(editText: EditText) {
        // Solo permitir números
        editText.inputType = InputType.TYPE_CLASS_NUMBER
    }
}