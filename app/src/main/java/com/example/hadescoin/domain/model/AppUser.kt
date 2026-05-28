package com.example.hadescoin.domain.model
// domain/model/AppUser.kt
data class AppUser(
    val id: String = "",
    val documentNumber: String = "",
    val phoneNumber: String = "",
    val fullName: String = "",
    val pin: String = "",
    val balance: Double = 0.0,
    val createdAt: String = ""
)
