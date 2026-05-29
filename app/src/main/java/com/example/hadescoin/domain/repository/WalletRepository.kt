package com.example.hadescoin.domain.repository

import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction

interface WalletRepository {
    suspend fun getWalletData(phoneNumber: String): Pair<AppUser?, List<WalletTransaction>>
    suspend fun getUserByPhone(phoneNumber: String): AppUser?
    suspend fun deposit(phoneNumber: String, amount: Double, pin: String): Result<Unit>
    suspend fun withdraw(phoneNumber: String, amount: Double, pin: String): Result<Unit>
    suspend fun payment(phoneNumber: String, amount: Double, reference: String, pin: String): Result<Unit>
}
