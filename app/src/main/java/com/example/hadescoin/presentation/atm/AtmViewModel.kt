package com.example.hadescoin.presentation.atm

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.R
import com.example.hadescoin.data.datasource.local.BlockLocalDataSource
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.model.ServiceItem
import com.example.hadescoin.domain.usecase.AtmDepositUseCase
import com.example.hadescoin.domain.usecase.AtmPaymentUseCase
import com.example.hadescoin.domain.usecase.ProcessWithdrawalUseCase
import com.example.hadescoin.domain.repository.WalletRepository
import kotlinx.coroutines.launch

enum class AtmOperation { DEPOSIT, PAYMENT, WITHDRAW_CODE }

class AtmViewModel(
    private val depositUseCase:    AtmDepositUseCase     = ServiceLocator.provideAtmDepositUseCase(),
    // paymentUseCase ya no se usa para PAYMENT — ahora se usa WalletRepository.payment() con PIN
    private val paymentUseCase:    AtmPaymentUseCase     = ServiceLocator.provideAtmPaymentUseCase(),
    private val processWithdrawal: ProcessWithdrawalUseCase = ServiceLocator.provideProcessWithdrawalUseCase(),
    private val blockDataSource:   BlockLocalDataSource  = ServiceLocator.provideBlockLocalDataSource(),
    private val repository:        WalletRepository      = ServiceLocator.provideWalletRepository()
) : ViewModel() {

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _exito = MutableLiveData<String?>()
    val exito: LiveData<String?> = _exito

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // ── Lista de servicios disponibles (igual que en el proyecto principal) ──
    val servicios: List<ServiceItem> = listOf(
        ServiceItem("energia",   Icons.Filled.ElectricBolt,         R.string.payment_servicio_energia),
        ServiceItem("agua",      Icons.Filled.WaterDrop,            R.string.payment_servicio_agua),
        ServiceItem("gas",       Icons.Filled.LocalFireDepartment,  R.string.payment_servicio_gas),
        ServiceItem("internet",  Icons.Filled.Wifi,                 R.string.payment_servicio_internet),
        ServiceItem("telefono",  Icons.Filled.PhoneAndroid,         R.string.payment_servicio_telefono),
        ServiceItem("tv",        Icons.Filled.Tv,                   R.string.payment_servicio_tv),
        ServiceItem("gimnasio",  Icons.Filled.FitnessCenter,        R.string.payment_servicio_gimnasio),
        ServiceItem("streaming", Icons.Filled.PlayCircle,           R.string.payment_servicio_streaming),
        ServiceItem("seguro",    Icons.Filled.Shield,               R.string.payment_servicio_seguro),
        ServiceItem("matricula", Icons.Filled.School,               R.string.payment_servicio_matricula)
    )

    // ── Bloqueo temporal por intentos fallidos (retiro con código) ──────────
    val MAX_ATTEMPTS      = 3
    val BLOCK_DURATION_MS = 3 * 60 * 1000L

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
            _bloqueado.value        = true
            _bloqueadoHastaMs.value = hasta
            _segundosBloqueo.value  = ((hasta - ahora) / 1000).toInt()
        }
    }

    // ── Depósito ATM (sin PIN) ───────────────────────────────────────────────
    fun executeDeposit(phoneNumber: String, amount: Double) {
        if (amount <= 0) { _error.value = "El monto debe ser mayor a cero"; return }
        viewModelScope.launch {
            _cargando.value = true
            depositUseCase(phoneNumber, amount).fold(
                onSuccess = { _exito.value = "Depósito de $\${\"%,.0f\".format(amount)} realizado con éxito." },
                onFailure = { _error.value = it.message }
            )
            _cargando.value = false
        }
    }

    // ── Pago de servicio con PIN (validación en Firebase) ───────────────────
    fun executePayment(
        phoneNumber: String,
        amount:      Double,
        reference:   String,
        pin:         String
    ) {
        if (phoneNumber.length != 10) { _error.value = "Ingresa un teléfono válido de 10 dígitos"; return }
        if (reference.isBlank())      { _error.value = "La referencia no puede estar vacía"; return }
        if (amount <= 0)              { _error.value = "El monto debe ser mayor a cero"; return }
        if (pin.length != 4)          { _error.value = "El PIN debe tener 4 dígitos"; return }
        viewModelScope.launch {
            _cargando.value = true
            // Usa payment() con PIN — Firebase valida que el PIN coincida
            repository.payment(phoneNumber, amount, reference, pin).fold(
                onSuccess = { _exito.value = "Pago de $\${\"%,.0f\".format(amount)} registrado con éxito." },
                onFailure = { _error.value = it.message }
            )
            _cargando.value = false
        }
    }

    // ── Retiro con código temporal ───────────────────────────────────────────
    fun executeWithdrawalCode(phoneNumber: String, code: String, amount: Double) {
        val ahora = System.currentTimeMillis()
        val hasta = blockDataSource.getBlockedUntilMs()

        if (ahora < hasta) {
            val secsLeft = ((hasta - ahora) / 1000).toInt()
            _bloqueado.value        = true
            _bloqueadoHastaMs.value = hasta
            _segundosBloqueo.value  = secsLeft
            _error.value = "Demasiados intentos fallidos. Espera $secsLeft segundos."
            return
        }

        if (_bloqueado.value == true) {
            _bloqueado.value        = false
            _bloqueadoHastaMs.value = 0L
            blockDataSource.clear()
        }

        if (phoneNumber.isBlank()) { _error.value = "Ingresa el número de teléfono"; return }
        if (code.length != 6)     { _error.value = "El código debe tener 6 dígitos";  return }
        if (amount <= 0)          { _error.value = "El monto debe ser mayor a cero";   return }

        viewModelScope.launch {
            _cargando.value = true
            val result = processWithdrawal(phoneNumber, code, amount)
            result.fold(
                onSuccess = {
                    blockDataSource.clear()
                    _bloqueado.value        = false
                    _bloqueadoHastaMs.value = 0L
                    _exito.value = "Retiro de $\${\"%,.0f\".format(amount)} procesado con éxito."
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
                        repository.markWithdrawalFailed(phoneNumber)
                        _error.value = "3 intentos fallidos. Bloqueado por ${BLOCK_DURATION_MS / 60000} minuto(s)."
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
