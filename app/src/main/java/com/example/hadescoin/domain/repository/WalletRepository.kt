package com.example.hadescoin.domain.repository

import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction

interface WalletRepository {
    suspend fun getWalletData(phoneNumber: String): Pair<AppUser?, List<WalletTransaction>>

    suspend fun transferFunds(
        senderPhone: String,
        receiverPhone: String,
        amount: Double,
        pin: String
    ): Result<Unit>

    suspend fun getUserByPhone(phoneNumber: String): AppUser?
}