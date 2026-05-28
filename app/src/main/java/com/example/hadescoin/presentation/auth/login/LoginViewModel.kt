package com.example.hadescoin.presentation.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.usecase.LoginUseCase
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase = ServiceLocator.provideLoginUseCase()
) : ViewModel() {

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _loginExitoso = MutableLiveData<String?>()
    val loginExitoso: LiveData<String?> = _loginExitoso

    private val _loginError = MutableLiveData<String?>()
    val loginError: LiveData<String?> = _loginError

    fun login(phoneNumber: String, pin: String) {
        if (!esTelefonoValido(phoneNumber)) {
            _loginError.value = "El teléfono debe tener 10 dígitos y empezar por 3"
            return
        }

        if (!esPinValido(pin)) {
            _loginError.value = "El PIN debe tener exactamente 4 dígitos y contener solo números"
            return
        }

        viewModelScope.launch {
            _cargando.value = true
            try {
                val success = loginUseCase(phoneNumber, pin)
                if (success) {
                    _loginExitoso.value = phoneNumber
                } else {
                    _loginError.value = "Teléfono o PIN incorrectos"
                }
            } catch (e: Exception) {
                _loginError.value = "Error de conexión: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    private fun esTelefonoValido(phoneNumber: String): Boolean {
        return phoneNumber.length == 10 && phoneNumber.firstOrNull() == '3' && phoneNumber.all { it.isDigit() }
    }

    private fun esPinValido(pin: String): Boolean {
        return pin.length == 4 && pin.all { it.isDigit() }
    }

    fun clearError() {
        _loginError.value = null
    }
}