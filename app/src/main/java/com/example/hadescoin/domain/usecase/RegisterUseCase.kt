package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(user: AppUser): Boolean {
        return repository.register(user)
    }
}