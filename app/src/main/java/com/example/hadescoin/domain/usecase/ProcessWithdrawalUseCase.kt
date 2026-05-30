package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.repository.WalletRepository

class ProcessWithdrawalUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke(
        phoneNumber: String,
        code:        String,
        amount:      Double
    ): Result<Unit> {
        if (phoneNumber.isBlank()) return Result.failure(Exception("Teléfono requerido"))
        if (code.length != 6)     return Result.failure(Exception("El código debe tener 6 dígitos"))
        if (amount <= 0)          return Result.failure(Exception("El monto debe ser mayor a cero"))
        return repository.processWithdrawal(phoneNumber, code, amount)
    }
}
