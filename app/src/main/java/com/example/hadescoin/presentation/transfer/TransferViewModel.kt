package com.example.hadescoin.presentation.transfer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.usecase.GetWalletDataUseCase
import com.example.hadescoin.domain.usecase.TransferUseCase
import kotlinx.coroutines.launch

class TransferViewModel(
    private val transferUseCase: TransferUseCase = ServiceLocator.provideTransferUseCase(),
    private val getWalletDataUseCase: GetWalletDataUseCase = ServiceLocator.provideGetWalletDataUseCase()
) : ViewModel() {

    private val _cargando        = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _senderBalance   = MutableLiveData(0.0)
    val senderBalance: LiveData<Double> = _senderBalance

    private val _transferExitosa = MutableLiveData<Boolean?>(null)
    val transferExitosa: LiveData<Boolean?> = _transferExitosa

    private val _transferError   = MutableLiveData<String?>(null)
    val transferError: LiveData<String?> = _transferError

    fun loadSenderBalance(phoneNumber: String) {
        viewModelScope.launch {
            try {
                val (user, _) = getWalletDataUseCase(phoneNumber)
                _senderBalance.value = user?.balance ?: 0.0
            } catch (_: Exception) {
                _senderBalance.value = 0.0
            }
        }
    }

    fun transfer(senderPhone: String, receiverPhone: String, amount: Double, pin: String) {
        if (amount <= 0)                  { _transferError.value = "El monto debe ser mayor a cero.";    return }
        if (receiverPhone.length != 10)   { _transferError.value = "Teléfono destinatario inválido.";    return }
        if (pin.length != 4)              { _transferError.value = "El PIN debe tener 4 dígitos.";       return }
        if (senderPhone == receiverPhone) { _transferError.value = "No puedes transferirte a ti mismo."; return }

        viewModelScope.launch {
            _cargando.value = true
            val result = transferUseCase(senderPhone, receiverPhone, amount, pin)
            result.fold(
                onSuccess = {
                    val (updatedUser, _) = getWalletDataUseCase(senderPhone)
                    _senderBalance.value   = updatedUser?.balance ?: 0.0
                    _transferExitosa.value = true
                },
                onFailure = {
                    _transferError.value = it.message ?: "Error inesperado"
                }
            )
            _cargando.value = false
        }
    }

    fun clearExito() { _transferExitosa.value = null }
    fun clearError() { _transferError.value = null }
}
