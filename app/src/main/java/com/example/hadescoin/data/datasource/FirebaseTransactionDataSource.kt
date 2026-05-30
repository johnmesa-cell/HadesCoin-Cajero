package com.example.hadescoin.data.datasource

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FirebaseTransactionDataSource {

    private val database = FirebaseDatabase.getInstance().getReference("transactions")

    suspend fun getTransactionsByPhone(phoneNumber: String): List<DataSnapshot> {
        // Dos queries separadas porque Firebase Realtime Database no soporta OR nativo
        val bySender = database
            .orderByChild("senderId")
            .equalTo(phoneNumber)
            .get().await()

        val byReceiver = database
            .orderByChild("receiverId")
            .equalTo(phoneNumber)
            .get().await()

        val seen = mutableSetOf<String>()
        val result = mutableListOf<DataSnapshot>()

        for (child in bySender.children) {
            val key = child.key ?: continue
            if (seen.add(key)) result.add(child)
        }
        for (child in byReceiver.children) {
            val key = child.key ?: continue
            if (seen.add(key)) result.add(child)
        }

        return result
    }

    suspend fun saveTransaction(data: Map<String, Any>): Boolean {
        return try {
            database.push().setValue(data).await()
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun updateTransactionField(txId: String, field: String, value: Any): Boolean {
        return try {
            database.child(txId).child(field).setValue(value).await()
            true
        } catch (_: Exception) { false }
    }
}
