package com.example.hadescoin.domain.repository

import com.example.hadescoin.domain.model.AppUser

interface AuthRepository {
    suspend fun login(phoneNumber: String, pin: String): Boolean
    suspend fun register(user: AppUser): Boolean
}
