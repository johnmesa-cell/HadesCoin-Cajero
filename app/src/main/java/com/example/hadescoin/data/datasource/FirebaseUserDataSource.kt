package com.example.hadescoin.data.datasource

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FirebaseUserDataSource {

    private val database = FirebaseDatabase.getInstance().getReference("users")

    suspend fun getUser(phoneNumber: String): DataSnapshot? {
        val snapshot = database.child(phoneNumber).get().await()
        return if (snapshot.exists()) snapshot else null
    }

    suspend fun saveUser(phoneNumber: String, userData: Map<String, Any>): Boolean {
        return try {
            val snapshot = database.child(phoneNumber).get().await()
            if (snapshot.exists()) return false
            database.child(phoneNumber).setValue(userData).await()
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun updateBalance(phoneNumber: String, newBalance: Double): Boolean {
        return try {
            database.child(phoneNumber).child("balance").setValue(newBalance).await()
            true
        } catch (_: Exception) {
            false
        }
    }
}
