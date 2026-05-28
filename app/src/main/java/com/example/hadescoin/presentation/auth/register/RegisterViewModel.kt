package com.example.hadescoin.presentation.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.usecase.RegisterUseCase
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase = ServiceLocator.provideRegisterUseCase()
) : ViewModel() {

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _registroExitoso = MutableLiveData<Boolean?>()
    val registroExitoso: LiveData<Boolean?> = _registroExitoso

    private val _registroError = MutableLiveData<String?>()
    val registroError: LiveData<String?> = _registroError

    fun register(
        fullName: String,
        documentNumber: String,
        phoneNumber: String,
        pin: String,
        confirmPin: String
    ) {
        if (fullName.isBlank() || documentNumber.isBlank() ||
            phoneNumber.isBlank() || pin.isBlank() || confirmPin.isBlank()) {
            _registroError.value = "Por favor completa todos los campos"
            return
        }

        if (!esNombreCompletoValido(fullName)) {
            _registroError.value = "El nombre completo solo debe contener letras"
            return
        }

        if (!esDocumentoValido(documentNumber)) {
            _registroError.value = "El documento debe tener entre 5 y 10 dígitos y contener solo números"
            return
        }

        if (!esTelefonoValido(phoneNumber)) {
            _registroError.value = "El teléfono debe tener exactamente 10 dígitos, empezar por 3 y contener solo números"
            return
        }

        if (!esPinValido(pin)) {
            _registroError.value = "El PIN debe tener exactamente 4 dígitos y contener solo números"
            return
        }

        if (pin != confirmPin) {
            _registroError.value = "Los PINs no coinciden"
            return
        }

        viewModelScope.launch {
            _cargando.value = true
            try {
                val user = AppUser(
                    id             = phoneNumber,
                    documentNumber = documentNumber,
                    phoneNumber    = phoneNumber,
                    fullName       = fullName,
                    pin            = pin,
                    balance        = 0.0
                )
                val success = registerUseCase(user)
                if (success) {
                    _registroExitoso.value = true
                } else {
                    _registroError.value = "Ya existe una cuenta con ese número de teléfono"
                }
            } catch (e: Exception) {
                _registroError.value = "Error de conexión: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    private fun esDocumentoValido(documentNumber: String): Boolean {
        return documentNumber.length in 5..10 && documentNumber.all { it.isDigit() }
    }

    private fun esNombreCompletoValido(fullName: String): Boolean {
        return fullName.isNotBlank() && fullName.all { it.isLetter() || it.isWhitespace() }
    }

    private fun esTelefonoValido(phoneNumber: String): Boolean {
        return phoneNumber.length == 10 && phoneNumber.firstOrNull() == '3' && phoneNumber.all { it.isDigit() }
    }

    private fun esPinValido(pin: String): Boolean {
        return pin.length == 4 && pin.all { it.isDigit() }
    }

    fun clearError() {
        _registroError.value = null
    }
}
