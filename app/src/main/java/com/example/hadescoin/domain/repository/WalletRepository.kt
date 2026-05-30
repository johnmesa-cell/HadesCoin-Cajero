package com.example.hadescoin.domain.repository

import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction

interface WalletRepository {
    suspend fun getWalletData(phoneNumber: String): Pair<AppUser?, List<WalletTransaction>>
    suspend fun getUserByPhone(phoneNumber: String): AppUser?
    suspend fun deposit(phoneNumber: String, amount: Double, pin: String): Result<Unit>
    suspend fun depositAtm(phoneNumber: String, amount: Double): Result<Unit>
    suspend fun withdraw(phoneNumber: String, amount: Double, pin: String): Result<Unit>
    suspend fun payment(phoneNumber: String, amount: Double, reference: String, pin: String): Result<Unit>
    suspend fun paymentAtm(phoneNumber: String, amount: Double, reference: String): Result<Unit>

    /**
     * Procesa un retiro en cajero usando código temporal.
     * Valida:
     *  - Que el usuario exista
     *  - Que el código coincida con withdrawalCode en Firebase
     *  - Que no haya expirado (withdrawalExpiry)
     *  - Que el monto solicitado sea <= withdrawalAmount autorizado
     *  - Que haya saldo suficiente
     * Si todo es válido: descuenta saldo, guarda TX WITHDRAWAL_COMPLETED, limpia campos withdrawal.
     */
    suspend fun processWithdrawal(
        phoneNumber: String,
        code:        String,
        amount:      Double
    ): Result<Unit>

    suspend fun markWithdrawalFailed(phoneNumber: String)
}
