package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.repository.WalletRepository

class AtmDepositUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke(phoneNumber: String, amount: Double): Result<Unit> {
        return repository.depositAtm(phoneNumber, amount)
    }
}
