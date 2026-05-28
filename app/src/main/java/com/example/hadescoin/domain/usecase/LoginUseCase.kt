package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(phoneNumber: String, pin: String): Boolean {
        return repository.login(phoneNumber, pin)
    }
}