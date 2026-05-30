package com.example.hadescoin.domain.model

data class WalletTransaction(
    val id:               String = "",
    val senderId:         String = "",
    val receiverId:       String = "",
    val amount:           Double = 0.0,
    val type:             String = "TRANSFER",
    val direction:        String = "OUT",
    val timestamp:        String = "",
    // Campos de retiro con código temporal
    val verificationCode: String = "",
    val withdrawalAmount: Double = 0.0,
    val expiresAt:        String = ""
)
