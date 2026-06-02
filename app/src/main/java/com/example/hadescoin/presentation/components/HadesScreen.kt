package com.example.hadescoin.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Contenedor base para todas las pantallas del cajero.
 * Combina el fondo con gradiente con el respeto automático
 * de la barra de estado y la barra de navegación del dispositivo.
 */
@Composable
fun HadesScreen(content: @Composable BoxScope.() -> Unit) {
    HadesBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            content  = content
        )
    }
}
