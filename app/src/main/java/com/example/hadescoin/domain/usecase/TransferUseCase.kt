package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.repository.WalletRepository

class TransferUseCase(private val walletRepository: WalletRepository) {
    suspend operator fun invoke(
        senderPhone: String,
        receiverPhone: String,
        amount: Double,
        pin: String
    ): Result<Unit> {
        return walletRepository.transferFunds(senderPhone, receiverPhone, amount, pin)
    }
}

