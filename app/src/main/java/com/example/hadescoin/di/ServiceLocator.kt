package com.example.hadescoin.di

import android.content.Context
import com.example.hadescoin.data.datasource.FirebaseTransactionDataSource
import com.example.hadescoin.data.datasource.FirebaseUserDataSource
import com.example.hadescoin.data.datasource.local.BlockLocalDataSource
import com.example.hadescoin.data.repository.WalletRepositoryImpl
import com.example.hadescoin.domain.repository.WalletRepository
import com.example.hadescoin.domain.usecase.*

object ServiceLocator {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val firebaseUserDataSource        by lazy { FirebaseUserDataSource() }
    private val firebaseTransactionDataSource by lazy { FirebaseTransactionDataSource() }
    private val blockLocalDataSource          by lazy { BlockLocalDataSource(appContext) }

    private val walletRepository by lazy { WalletRepositoryImpl(firebaseUserDataSource, firebaseTransactionDataSource) }

    fun provideBlockLocalDataSource():   BlockLocalDataSource    = blockLocalDataSource
    fun provideWalletRepository():       WalletRepository        = walletRepository
    fun provideGetWalletDataUseCase():    GetWalletDataUseCase    = GetWalletDataUseCase(walletRepository)
    fun provideAtmDepositUseCase():       AtmDepositUseCase       = AtmDepositUseCase(walletRepository)
    fun provideAtmPaymentUseCase():       AtmPaymentUseCase       = AtmPaymentUseCase(walletRepository)
    fun provideProcessWithdrawalUseCase(): ProcessWithdrawalUseCase = ProcessWithdrawalUseCase(walletRepository)
}
