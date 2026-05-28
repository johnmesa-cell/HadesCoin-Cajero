package com.example.hadescoin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Tema único oscuro — siempre dark, sin dynamic color (Android 12+)
private val HadesDarkColorScheme = darkColorScheme(
    primary          = HadesPurple,      // Títulos, bordes activos, TextButton
    onPrimary        = Color.White,      // Texto sobre elementos primary
    secondary        = HadesOrange,      // Botón principal
    onSecondary      = HadesOnOrange,    // Texto encima del botón naranja
    background       = HadesBlack,       // Fondo general de la app
    onBackground     = HadesOnDark,      // Texto sobre el fondo
    surface          = HadesNavy,        // Cards y superficies elevadas
    onSurface        = HadesOnDark,      // Texto dentro de las cards
    outline          = HadesPurple,      // Borde de OutlinedTextField sin foco
    primaryContainer = HadesNavy,        // Contenedor de campos enfocados
)

@Composable
fun HadesCoinTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HadesDarkColorScheme,
        typography  = Typography,
        content     = content
    )
}
