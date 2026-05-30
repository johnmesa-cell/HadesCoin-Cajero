package com.example.hadescoin

import android.app.Application
import com.example.hadescoin.di.ServiceLocator

class HadesCoinApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
