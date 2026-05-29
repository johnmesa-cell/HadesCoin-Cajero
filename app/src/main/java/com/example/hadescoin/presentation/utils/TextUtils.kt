package com.example.hadescoin.presentation.utils

// Solo getInitials sigue en uso (HomeView — bienvenida)
fun getInitials(fullName: String?): String {
    if (fullName.isNullOrBlank()) return "?"
    val parts = fullName.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> (parts[0].take(1) + parts[1].take(1)).uppercase()
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "?"
    }
}
