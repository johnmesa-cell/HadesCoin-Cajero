package com.example.hadescoin.di

import com.example.hadescoin.data.datasource.FirebaseTransactionDataSource
import com.example.hadescoin.data.datasource.FirebaseUserDataSource
import com.example.hadescoin.data.repository.AuthRepositoryImpl
import com.example.hadescoin.data.repository.WalletRepositoryImpl
import com.example.hadescoin.domain.usecase.DepositUseCase
import com.example.hadescoin.domain.usecase.GetWalletDataUseCase
import com.example.hadescoin.domain.usecase.LoginUseCase
import com.example.hadescoin.domain.usecase.PaymentUseCase
import com.example.hadescoin.domain.usecase.WithdrawUseCase

object ServiceLocator {

    private val firebaseUserDataSource        by lazy { FirebaseUserDataSource() }
    private val firebaseTransactionDataSource by lazy { FirebaseTransactionDataSource() }

    private val authRepository   by lazy { AuthRepositoryImpl(firebaseUserDataSource) }
    private val walletRepository by lazy { WalletRepositoryImpl(firebaseUserDataSource, firebaseTransactionDataSource) }

    fun provideLoginUseCase():          LoginUseCase          = LoginUseCase(authRepository)
    fun provideGetWalletDataUseCase():  GetWalletDataUseCase  = GetWalletDataUseCase(walletRepository)
    fun provideDepositUseCase():        DepositUseCase        = DepositUseCase(walletRepository)
    fun provideWithdrawUseCase():       WithdrawUseCase       = WithdrawUseCase(walletRepository)
    fun providePaymentUseCase():        PaymentUseCase        = PaymentUseCase(walletRepository)
}
