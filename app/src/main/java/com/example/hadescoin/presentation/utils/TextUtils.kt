package com.example.hadescoin.presentation.utils

import java.util.Locale

fun getInitials(fullName: String?): String {
    if (fullName.isNullOrBlank()) return "?"
    val parts = fullName.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> (parts[0].take(1) + parts[1].take(1)).uppercase(Locale.getDefault())
        parts.size == 1 -> parts[0].take(2).uppercase(Locale.getDefault())
        else -> "?"
    }
}

fun translateTransactionType(type: String): String {
    return when (type.uppercase()) {
        "TODOS"    -> "Todos"
        "DEPOSIT"  -> "Depósito"
        "WITHDRAW" -> "Retiro"
        "TRANSFER" -> "Transferencia"
        "INCOME"   -> "Ingreso"
        "PAYMENT"  -> "Pago"
        else       -> type
    }
}

fun formatTimestamp(timestamp: String): String {
    return try {
        val meses = listOf(
            "ene", "feb", "mar", "abr", "may", "jun",
            "jul", "ago", "sep", "oct", "nov", "dic"
        )
        val partes = timestamp.take(10).split("-")
        if (partes.size == 3) {
            val dia = partes[2].trimStart('0')
            val mes = meses[partes[1].toInt() - 1]
            val anio = partes[0]
            "$dia $mes $anio"
        } else timestamp.take(10)
    } catch (e: Exception) {
        timestamp.take(10)
    }
}

