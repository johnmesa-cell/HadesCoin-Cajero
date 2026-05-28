package com.example.hadescoin.data.repository

import com.example.hadescoin.data.datasource.FirebaseTransactionDataSource
import com.example.hadescoin.data.datasource.FirebaseUserDataSource
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction
import com.example.hadescoin.domain.repository.WalletRepository
import java.time.Instant

class WalletRepositoryImpl(
    private val userDataSource: FirebaseUserDataSource = FirebaseUserDataSource(),
    private val transactionDataSource: FirebaseTransactionDataSource = FirebaseTransactionDataSource()
) : WalletRepository {

    // ── Mapeo DataSnapshot → AppUser ────────────────────────────────────
    private fun mapUser(snapshot: com.google.firebase.database.DataSnapshot): AppUser {
        return AppUser(
            id             = snapshot.key ?: "",
            documentNumber = snapshot.child("documentNumber").getValue(String::class.java) ?: "",
            phoneNumber    = snapshot.child("phoneNumber").getValue(String::class.java) ?: "",
            fullName       = snapshot.child("fullName").getValue(String::class.java) ?: "",
            pin            = snapshot.child("pin").getValue(String::class.java) ?: "",
            balance        = snapshot.child("balance").getValue(Double::class.java) ?: 0.0,
            createdAt      = snapshot.child("createdAt").getValue(String::class.java) ?: ""
        )
    }

    // ── Mapeo DataSnapshot → WalletTransaction ───────────────────────────
    private fun mapTransaction(
        snapshot: com.google.firebase.database.DataSnapshot,
        currentPhone: String
    ): WalletTransaction {
        val senderId   = snapshot.child("senderId").getValue(String::class.java) ?: ""
        val receiverId = snapshot.child("receiverId").getValue(String::class.java) ?: ""
        val type       = snapshot.child("type").getValue(String::class.java) ?: "TRANSFER"
        val direction  = when {
            type != "TRANSFER"           -> if (senderId == currentPhone) "OUT" else "IN"
            senderId == currentPhone     -> "OUT"
            else                         -> "IN"
        }
        return WalletTransaction(
            id         = snapshot.key ?: "",
            senderId   = senderId,
            receiverId = receiverId,
            amount     = snapshot.child("amount").getValue(Double::class.java) ?: 0.0,
            type       = type,
            direction  = direction,
            timestamp  = snapshot.child("timestamp").getValue(String::class.java) ?: ""
        )
    }

    override suspend fun getWalletData(phoneNumber: String): Pair<AppUser?, List<WalletTransaction>> {
        val userSnapshot = userDataSource.getUser(phoneNumber) ?: return Pair(null, emptyList())
        val user = mapUser(userSnapshot)
        val transactions = transactionDataSource
            .getTransactionsByPhone(phoneNumber)
            .map { mapTransaction(it, phoneNumber) }
        return Pair(user, transactions)
    }

    override suspend fun getUserByPhone(phoneNumber: String): AppUser? {
        val snapshot = userDataSource.getUser(phoneNumber) ?: return null
        return mapUser(snapshot)
    }

    override suspend fun transferFunds(
        senderPhone: String,
        receiverPhone: String,
        amount: Double,
        pin: String
    ): Result<Unit> {
        return try {
            val senderSnapshot = userDataSource.getUser(senderPhone)
                ?: return Result.failure(Exception("Usuario no encontrado"))
            val sender = mapUser(senderSnapshot)

            if (sender.pin != pin)
                return Result.failure(Exception("PIN incorrecto"))
            if (sender.balance < amount)
                return Result.failure(Exception("Saldo insuficiente"))
            if (senderPhone == receiverPhone)
                return Result.failure(Exception("No puedes transferirte a ti mismo"))

            val receiverSnapshot = userDataSource.getUser(receiverPhone)
                ?: return Result.failure(Exception("El destinatario no existe"))
            val receiver = mapUser(receiverSnapshot)

            userDataSource.updateBalance(senderPhone,   sender.balance   - amount)
            userDataSource.updateBalance(receiverPhone, receiver.balance + amount)

            val data = mapOf(
                "senderId"   to senderPhone,
                "receiverId" to receiverPhone,
                "amount"     to amount,
                "type"       to "TRANSFER",
                "timestamp"  to Instant.now().toString()
            )
            transactionDataSource.saveTransaction(data)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
