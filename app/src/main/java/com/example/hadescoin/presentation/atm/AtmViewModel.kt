package com.example.hadescoin.presentation.atm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.data.datasource.local.BlockLocalDataSource
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.usecase.AtmDepositUseCase
import com.example.hadescoin.domain.usecase.AtmPaymentUseCase
import com.example.hadescoin.domain.usecase.ProcessWithdrawalUseCase
import com.example.hadescoin.domain.repository.WalletRepository
import kotlinx.coroutines.launch

enum class AtmOperation { DEPOSIT, PAYMENT, WITHDRAW_CODE }

class AtmViewModel(
    private val depositUseCase:     AtmDepositUseCase     = ServiceLocator.provideAtmDepositUseCase(),
    private val paymentUseCase:     AtmPaymentUseCase     = ServiceLocator.provideAtmPaymentUseCase(),
    private val processWithdrawal:  ProcessWithdrawalUseCase = ServiceLocator.provideProcessWithdrawalUseCase(),
    private val blockDataSource:    BlockLocalDataSource  = ServiceLocator.provideBlockLocalDataSource(),
    private val repository:         WalletRepository      = ServiceLocator.provideWalletRepository()
) : ViewModel() {

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _exito = MutableLiveData<String?>()
    val exito: LiveData<String?> = _exito

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // ── Bloqueo temporal por intentos fallidos ──────────────────────────
    val MAX_ATTEMPTS              = 3
    val BLOCK_DURATION_MS         = 3 * 60 * 1000L   // 3 minutos (pruebas)

    private val _bloqueado        = MutableLiveData(false)
    val bloqueado: LiveData<Boolean> = _bloqueado

    private val _segundosBloqueo  = MutableLiveData(0)
    val segundosBloqueo: LiveData<Int> = _segundosBloqueo

    private val _bloqueadoHastaMs = MutableLiveData(0L)
    val bloqueadoHastaMs: LiveData<Long> = _bloqueadoHastaMs

    init {
        val hasta = blockDataSource.getBlockedUntilMs()
        val ahora = System.currentTimeMillis()
        if (hasta > ahora) {
            _bloqueado.value       = true
            _bloqueadoHastaMs.value = hasta
            _segundosBloqueo.value = ((hasta - ahora) / 1000).toInt()
        }
    }

    // ── Operaciones estándar (depósito, pago) ─────────────────
    fun execute(
        operation:   AtmOperation,
        phoneNumber: String,
        amount:      Double,
        reference:   String = ""
    ) {
        if (amount <= 0)  { _error.value = "El monto debe ser mayor a cero"; return }
        if (operation == AtmOperation.PAYMENT && reference.isBlank()) {
            _error.value = "La referencia no puede estar vacía"; return
        }
        viewModelScope.launch {
            _cargando.value = true
            val result = when (operation) {
                AtmOperation.DEPOSIT  -> depositUseCase(phoneNumber, amount)
                AtmOperation.PAYMENT  -> paymentUseCase(phoneNumber, amount, reference)
                else                  -> Result.failure(Exception("Operación inválida"))
            }
            result.fold(
                onSuccess = {
                    _exito.value = when (operation) {
                        AtmOperation.DEPOSIT  -> "Depósito de $${"%,.0f".format(amount)} realizado con éxito."
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
        val ahora = System.currentTimeMillis()
        val hasta = blockDataSource.getBlockedUntilMs()

        // Revisar bloqueo activo
        if (ahora < hasta) {
            val secsLeft = ((hasta - ahora) / 1000).toInt()
            _bloqueado.value       = true
            _bloqueadoHastaMs.value = hasta
            _segundosBloqueo.value = secsLeft
            _error.value = "Demasiados intentos fallidos. Espera $secsLeft segundos."
            return
        } else if (_bloqueado.value == true) {
            _bloqueado.value        = false
            _bloqueadoHastaMs.value = 0L
        }

        if (phoneNumber.isBlank()) { _error.value = "Ingresa el número de teléfono"; return }
        if (code.length != 6)     { _error.value = "El código debe tener 6 dígitos";  return }
        if (amount <= 0)          { _error.value = "El monto debe ser mayor a cero";    return }

        viewModelScope.launch {
            _cargando.value = true
            val result = processWithdrawal(phoneNumber, code, amount)
            result.fold(
                onSuccess = {
                    blockDataSource.clear()
                    _bloqueado.value        = false
                    _bloqueadoHastaMs.value = 0L
                    _exito.value   = "Retiro de $${"%,.0f".format(amount)} procesado con éxito."
                },
                onFailure = { e ->
                    val intentos = blockDataSource.getFailedAttempts() + 1
                    blockDataSource.saveFailedAttempts(intentos)
                    if (intentos >= MAX_ATTEMPTS) {
                        val hastaNuevo = System.currentTimeMillis() + BLOCK_DURATION_MS
                        blockDataSource.saveBlockedUntilMs(hastaNuevo)
                        _bloqueado.value        = true
                        _bloqueadoHastaMs.value = hastaNuevo
                        _segundosBloqueo.value  = (BLOCK_DURATION_MS / 1000).toInt()
                        
                        viewModelScope.launch {
                            repository.markWithdrawalFailed(phoneNumber)
                        }

                        _error.value           = "3 intentos fallidos. Bloqueado por ${BLOCK_DURATION_MS / 60000} minuto(s)."
                    } else {
                        _error.value = "${e.message} (intento $intentos/$MAX_ATTEMPTS)"
                    }
                }
            )
            _cargando.value = false
        }
    }

    fun clearExito() { _exito.value = null }
    fun clearError() { _error.value = null }
}
