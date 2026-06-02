package com.example.hadescoin.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Modelo local para representar un servicio pagable.
 * NO se guarda en Firebase, solo se usa en la UI.
 *
 * @param id Identificador único del servicio (usado internamente)
 * @param icono Ícono Material 3 para mostrar en la UI
 * @param nombreRes ID del string resource para el nombre del servicio (R.string.payment_servicio_xxx)
 */
data class ServiceItem(
    val id:        String,
    val icono:     ImageVector,
    val nombreRes: Int
)
