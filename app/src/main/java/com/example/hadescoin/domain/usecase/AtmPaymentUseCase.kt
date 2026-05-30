package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.repository.WalletRepository

class AtmPaymentUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke(phoneNumber: String, amount: Double, reference: String): Result<Unit> {
        return repository.paymentAtm(phoneNumber, amount, reference)
    }
}
