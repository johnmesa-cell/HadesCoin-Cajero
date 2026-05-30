package com.example.hadescoin.data.repository

import com.example.hadescoin.data.datasource.FirebaseTransactionDataSource
import com.example.hadescoin.data.datasource.FirebaseUserDataSource
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction
import com.example.hadescoin.domain.repository.WalletRepository
import com.google.firebase.database.DataSnapshot
import java.time.Instant

class WalletRepositoryImpl(
    private val userDataSource:        FirebaseUserDataSource        = FirebaseUserDataSource(),
    private val transactionDataSource: FirebaseTransactionDataSource = FirebaseTransactionDataSource()
) : WalletRepository {

    private fun mapUser(snapshot: DataSnapshot): AppUser {
        return AppUser(
            id             = snapshot.key ?: "",
            documentNumber = snapshot.child("documentNumber").getValue(String::class.java) ?: "",
            phoneNumber    = snapshot.child("phoneNumber").getValue(String::class.java)    ?: "",
            fullName       = snapshot.child("fullName").getValue(String::class.java)       ?: "",
            pin            = snapshot.child("pin").getValue(String::class.java)            ?: "",
            balance        = snapshot.child("balance").getValue(Double::class.java)        ?: 0.0,
            createdAt      = snapshot.child("createdAt").getValue(String::class.java)      ?: ""
        )
    }

    private suspend fun markAsFailed(phoneNumber: String, storedTxId: String) {
        if (storedTxId.isNotBlank()) {
            transactionDataSource.updateTransactionField(storedTxId, "type", "WITHDRAWAL_FAILED")
            transactionDataSource.updateTransactionField(storedTxId, "timestamp", Instant.now().toString())
        }
        userDataSource.updateUserField(phoneNumber, "withdrawalCode",   "")
        userDataSource.updateUserField(phoneNumber, "withdrawalAmount", "")
        userDataSource.updateUserField(phoneNumber, "withdrawalExpiry", "")
        userDataSource.updateUserField(phoneNumber, "withdrawalTxId",   "")
    }

    override suspend fun markWithdrawalFailed(phoneNumber: String) {
        try {
            val snapshot   = userDataSource.getUser(phoneNumber) ?: return
            val storedTxId = snapshot.child("withdrawalTxId").getValue(String::class.java) ?: ""
            markAsFailed(phoneNumber, storedTxId)
        } catch (_: Exception) {}
    }

    private fun mapTransaction(snapshot: DataSnapshot, currentPhone: String): WalletTransaction {
        val senderId  = snapshot.child("senderId").getValue(String::class.java)  ?: ""
        val type      = snapshot.child("type").getValue(String::class.java)      ?: "DEPOSIT"
        val direction = when (type) {
            "DEPOSIT"                                  -> "IN"
            "WITHDRAW", "PAYMENT",
            "WITHDRAWAL_PENDING", "WITHDRAWAL_COMPLETED" -> "OUT"
            else -> if (senderId == currentPhone) "OUT" else "IN"
        }
        return WalletTransaction(
            id               = snapshot.key ?: "",
            senderId         = senderId,
            receiverId       = snapshot.child("receiverId").getValue(String::class.java)       ?: "",
            amount           = snapshot.child("amount").getValue(Double::class.java)           ?: 0.0,
            type             = type,
            direction        = direction,
            timestamp        = snapshot.child("timestamp").getValue(String::class.java)        ?: "",
            verificationCode = snapshot.child("verificationCode").getValue(String::class.java) ?: "",
            withdrawalAmount = snapshot.child("withdrawalAmount").getValue(Double::class.java) ?: 0.0,
            expiresAt        = snapshot.child("expiresAt").getValue(String::class.java)        ?: ""
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

    override suspend fun deposit(phoneNumber: String, amount: Double, pin: String): Result<Unit> {
        return try {
            val snapshot = userDataSource.getUser(phoneNumber)
                ?: return Result.failure(Exception("Usuario no encontrado"))
            val user = mapUser(snapshot)
            if (user.pin != pin) return Result.failure(Exception("PIN incorrecto"))
            depositAtm(phoneNumber, amount)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun depositAtm(phoneNumber: String, amount: Double): Result<Unit> {
        return try {
            val snapshot = userDataSource.getUser(phoneNumber)
                ?: return Result.failure(Exception("Usuario no encontrado"))
            val user = mapUser(snapshot)
            if (amount <= 0) return Result.failure(Exception("El monto debe ser mayor a cero"))
            userDataSource.updateBalance(phoneNumber, user.balance + amount)
            transactionDataSource.saveTransaction(mapOf(
                "senderId"  to phoneNumber, "receiverId" to phoneNumber,
                "amount"    to amount,      "type"       to "DEPOSIT",
                "timestamp" to Instant.now().toString()
            ))
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun withdraw(phoneNumber: String, amount: Double, pin: String): Result<Unit> {
        return try {
            val snapshot = userDataSource.getUser(phoneNumber)
                ?: return Result.failure(Exception("Usuario no encontrado"))
            val user = mapUser(snapshot)
            if (user.pin != pin)       return Result.failure(Exception("PIN incorrecto"))
            if (amount <= 0)           return Result.failure(Exception("El monto debe ser mayor a cero"))
            if (user.balance < amount) return Result.failure(Exception("Saldo insuficiente"))
            userDataSource.updateBalance(phoneNumber, user.balance - amount)
            transactionDataSource.saveTransaction(mapOf(
                "senderId"  to phoneNumber, "receiverId" to phoneNumber,
                "amount"    to amount,      "type"       to "WITHDRAW",
                "timestamp" to Instant.now().toString()
            ))
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun payment(phoneNumber: String, amount: Double, reference: String, pin: String): Result<Unit> {
        return try {
            val snapshot = userDataSource.getUser(phoneNumber)
                ?: return Result.failure(Exception("Usuario no encontrado"))
            val user = mapUser(snapshot)
            if (user.pin != pin) return Result.failure(Exception("PIN incorrecto"))
            paymentAtm(phoneNumber, amount, reference)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun paymentAtm(phoneNumber: String, amount: Double, reference: String): Result<Unit> {
        return try {
            val snapshot = userDataSource.getUser(phoneNumber)
                ?: return Result.failure(Exception("Usuario no encontrado"))
            val user = mapUser(snapshot)
            if (amount <= 0)           return Result.failure(Exception("El monto debe ser mayor a cero"))
            if (user.balance < amount) return Result.failure(Exception("Saldo insuficiente"))
            if (reference.isBlank())   return Result.failure(Exception("La referencia no puede estar vacía"))
            userDataSource.updateBalance(phoneNumber, user.balance - amount)
            transactionDataSource.saveTransaction(mapOf(
                "senderId"  to phoneNumber, "receiverId" to reference,
                "amount"    to amount,      "type"       to "PAYMENT",
                "timestamp" to Instant.now().toString()
            ))
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun processWithdrawal(
        phoneNumber: String,
        code:        String,
        amount:      Double
    ): Result<Unit> {
        return try {
            val snapshot = userDataSource.getUser(phoneNumber)
                ?: return Result.failure(Exception("Usuario no encontrado"))
            val user = mapUser(snapshot)

            val storedCode   = snapshot.child("withdrawalCode").getValue(String::class.java)               ?: ""
            val storedAmount = snapshot.child("withdrawalAmount").getValue(String::class.java)?.toDoubleOrNull() ?: 0.0
            val storedExpiry = snapshot.child("withdrawalExpiry").getValue(String::class.java)              ?: ""
            val storedTxId   = snapshot.child("withdrawalTxId").getValue(String::class.java)               ?: ""

            if (storedCode.isBlank())  return Result.failure(Exception("No hay un código de retiro generado para este usuario"))
            if (storedCode != code)    return Result.failure(Exception("Código incorrecto"))

            if (storedExpiry.isNotBlank()) {
                val expiry = Instant.parse(storedExpiry)
                if (Instant.now().isAfter(expiry)) {
                    markAsFailed(phoneNumber, storedTxId)
                    return Result.failure(Exception("El código ha expirado. Genera uno nuevo desde la app."))
                }
            }

            if (amount > storedAmount) return Result.failure(Exception("El monto supera el autorizado (máx: $${"%,.0f".format(storedAmount)})"))
            if (user.balance < amount) return Result.failure(Exception("Saldo insuficiente"))

            userDataSource.updateBalance(phoneNumber, user.balance - amount)
            
            if (storedTxId.isNotBlank()) {
                transactionDataSource.updateTransactionField(storedTxId, "type",      "WITHDRAWAL_COMPLETED")
                transactionDataSource.updateTransactionField(storedTxId, "amount",    amount)
                transactionDataSource.updateTransactionField(storedTxId, "timestamp", Instant.now().toString())
            }

            // Limpiar campos withdrawal del usuario
            userDataSource.updateUserField(phoneNumber, "withdrawalCode",   "")
            userDataSource.updateUserField(phoneNumber, "withdrawalAmount", "")
            userDataSource.updateUserField(phoneNumber, "withdrawalExpiry", "")
            userDataSource.updateUserField(phoneNumber, "withdrawalTxId",   "")

            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}
