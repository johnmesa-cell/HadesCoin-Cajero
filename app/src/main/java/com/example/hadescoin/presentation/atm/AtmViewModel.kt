package com.example.hadescoin.presentation.atm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.usecase.DepositUseCase
import com.example.hadescoin.domain.usecase.PaymentUseCase
import com.example.hadescoin.domain.usecase.WithdrawUseCase
import kotlinx.coroutines.launch

enum class AtmOperation { DEPOSIT, WITHDRAW, PAYMENT }

class AtmViewModel(
    private val depositUseCase:  DepositUseCase  = ServiceLocator.provideDepositUseCase(),
    private val withdrawUseCase: WithdrawUseCase = ServiceLocator.provideWithdrawUseCase(),
    private val paymentUseCase:  PaymentUseCase  = ServiceLocator.providePaymentUseCase()
) : ViewModel() {

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _exito = MutableLiveData<String?>()
    val exito: LiveData<String?> = _exito

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun execute(
        operation: AtmOperation,
        phoneNumber: String,
        amount: Double,
        pin: String,
        reference: String = ""
    ) {
        if (amount <= 0) { _error.value = "El monto debe ser mayor a cero"; return }
        if (pin.length != 4) { _error.value = "El PIN debe tener 4 dígitos"; return }
        if (operation == AtmOperation.PAYMENT && reference.isBlank()) {
            _error.value = "La referencia no puede estar vacía"; return
        }

        viewModelScope.launch {
            _cargando.value = true
            val result = when (operation) {
                AtmOperation.DEPOSIT  -> depositUseCase(phoneNumber, amount, pin)
                AtmOperation.WITHDRAW -> withdrawUseCase(phoneNumber, amount, pin)
                AtmOperation.PAYMENT  -> paymentUseCase(phoneNumber, amount, reference, pin)
            }
            result.fold(
                onSuccess = {
                    _exito.value = when (operation) {
                        AtmOperation.DEPOSIT  -> "Depósito de $${ "%,.2f".format(amount) } realizado con éxito."
                        AtmOperation.WITHDRAW -> "Retiro de $${ "%,.2f".format(amount) } realizado con éxito."
                        AtmOperation.PAYMENT  -> "Pago de $${ "%,.2f".format(amount) } registrado con éxito."
                    }
                },
                onFailure = { _error.value = it.message }
            )
            _cargando.value = false
        }
    }

    fun clearExito() { _exito.value = null }
    fun clearError() { _error.value = null }
}
