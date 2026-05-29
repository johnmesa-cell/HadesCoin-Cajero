package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.repository.WalletRepository

class PaymentUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke(phoneNumber: String, amount: Double, reference: String, pin: String): Result<Unit> {
        return repository.payment(phoneNumber, amount, reference, pin)
    }
}
