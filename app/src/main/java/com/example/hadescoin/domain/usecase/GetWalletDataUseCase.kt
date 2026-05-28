package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction
import com.example.hadescoin.domain.repository.WalletRepository

class GetWalletDataUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke(phoneNumber: String): Pair<AppUser?, List<WalletTransaction>> {
        return repository.getWalletData(phoneNumber)
    }
}
