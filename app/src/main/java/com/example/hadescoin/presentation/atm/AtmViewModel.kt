package com.example.hadescoin.presentation.atm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.usecase.DepositUseCase
import com.example.hadescoin.domain.usecase.PaymentUseCase
import com.example.hadescoin.domain.usecase.ProcessWithdrawalUseCase
import com.example.hadescoin.domain.usecase.WithdrawUseCase
import kotlinx.coroutines.launch

enum class AtmOperation { DEPOSIT, WITHDRAW, PAYMENT, WITHDRAW_CODE }

class AtmViewModel(
    private val depositUseCase:     DepositUseCase     = ServiceLocator.provideDepositUseCase(),
    private val withdrawUseCase:    WithdrawUseCase    = ServiceLocator.provideWithdrawUseCase(),
    private val paymentUseCase:     PaymentUseCase     = ServiceLocator.providePaymentUseCase(),
    private val processWithdrawal:  ProcessWithdrawalUseCase = ServiceLocator.provideProcessWithdrawalUseCase()
) : ViewModel() {

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _exito = MutableLiveData<String?>()
    val exito: LiveData<String?> = _exito

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // ── Bloqueo temporal por intentos fallidos ──────────────────────────
    private var failedAttempts    = 0
    private var blockedUntilMs    = 0L
    val MAX_ATTEMPTS              = 3
    val BLOCK_DURATION_MS         = 3 * 60 * 1000L   // 3 minutos (pruebas)

    private val _bloqueado        = MutableLiveData(false)
    val bloqueado: LiveData<Boolean> = _bloqueado

    private val _segundosBloqueo  = MutableLiveData(0)
    val segundosBloqueo: LiveData<Int> = _segundosBloqueo

    // ── Operaciones estándar (depósito, retiro con PIN, pago) ─────────────────
    fun execute(
        operation:   AtmOperation,
        phoneNumber: String,
        amount:      Double,
        pin:         String,
        reference:   String = ""
    ) {
        if (amount <= 0)  { _error.value = "El monto debe ser mayor a cero"; return }
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
                else                  -> Result.failure(Exception("Operación inválida"))
            }
            result.fold(
                onSuccess = {
                    _exito.value = when (operation) {
                        AtmOperation.DEPOSIT  -> "Depósito de $${"%,.0f".format(amount)} realizado con éxito."
                        AtmOperation.WITHDRAW -> "Retiro de $${"%,.0f".format(amount)} realizado con éxito."
                        AtmOperation.PAYMENT  -> "Pago de $${"%,.0f".format(amount)} registrado con éxito."
                        else                  -> "Operación exitosa."
                    }
                },
                onFailure = { _error.value = it.message }
            )
            _cargando.value = false
        }
    }

    // ── Retiro con código temporal ────────────────────────────────────────────
    fun executeWithdrawalCode(phoneNumber: String, code: String, amount: Double) {
        val now = System.currentTimeMillis()

        // Revisar bloqueo activo
        if (now < blockedUntilMs) {
            val secsLeft = ((blockedUntilMs - now) / 1000).toInt()
            _bloqueado.value     = true
            _segundosBloqueo.value = secsLeft
            _error.value = "Demasiados intentos fallidos. Espera $secsLeft segundos."
            return
        } else if (_bloqueado.value == true) {
            _bloqueado.value = false
            failedAttempts   = 0
        }

        if (phoneNumber.isBlank()) { _error.value = "Ingresa el número de teléfono"; return }
        if (code.length != 6)     { _error.value = "El código debe tener 6 dígitos";  return }
        if (amount <= 0)          { _error.value = "El monto debe ser mayor a cero";    return }

        viewModelScope.launch {
            _cargando.value = true
            val result = processWithdrawal(phoneNumber, code, amount)
            result.fold(
                onSuccess = {
                    failedAttempts = 0
                    _exito.value   = "Retiro de $${"%,.0f".format(amount)} procesado con éxito."
                },
                onFailure = { e ->
                    failedAttempts++
                    if (failedAttempts >= MAX_ATTEMPTS) {
                        blockedUntilMs         = System.currentTimeMillis() + BLOCK_DURATION_MS
                        _bloqueado.value       = true
                        _segundosBloqueo.value = (BLOCK_DURATION_MS / 1000).toInt()
                        _error.value           = "3 intentos fallidos. Bloqueado por ${BLOCK_DURATION_MS / 60000} minuto(s)."
                    } else {
                        _error.value = "${e.message} (intento $failedAttempts/$MAX_ATTEMPTS)"
                    }
                }
            )
            _cargando.value = false
        }
    }

    fun clearExito() { _exito.value = null }
    fun clearError() { _error.value = null }
}
