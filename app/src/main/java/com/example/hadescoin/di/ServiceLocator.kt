package com.example.hadescoin.di

import com.example.hadescoin.data.datasource.FirebaseTransactionDataSource
import com.example.hadescoin.data.datasource.FirebaseUserDataSource
import com.example.hadescoin.data.repository.AuthRepositoryImpl
import com.example.hadescoin.data.repository.WalletRepositoryImpl
import com.example.hadescoin.domain.usecase.GetWalletDataUseCase
import com.example.hadescoin.domain.usecase.LoginUseCase
import com.example.hadescoin.domain.usecase.RegisterUseCase
import com.example.hadescoin.domain.usecase.TransferUseCase

object ServiceLocator {

    private val firebaseUserDataSource by lazy { FirebaseUserDataSource() }
    private val firebaseTransactionDataSource by lazy { FirebaseTransactionDataSource() }

    private val authRepository by lazy { AuthRepositoryImpl(firebaseUserDataSource) }
    private val walletRepository by lazy { WalletRepositoryImpl(firebaseUserDataSource, firebaseTransactionDataSource) }

    fun provideLoginUseCase(): LoginUseCase = LoginUseCase(authRepository)
    fun provideRegisterUseCase(): RegisterUseCase = RegisterUseCase(authRepository)
    fun provideGetWalletDataUseCase(): GetWalletDataUseCase = GetWalletDataUseCase(walletRepository)
    fun provideTransferUseCase(): TransferUseCase = TransferUseCase(walletRepository)
}
