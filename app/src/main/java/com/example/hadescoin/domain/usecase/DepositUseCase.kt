package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.repository.WalletRepository

class DepositUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke(phoneNumber: String, amount: Double, pin: String): Result<Unit> {
        return repository.deposit(phoneNumber, amount, pin)
    }
}
