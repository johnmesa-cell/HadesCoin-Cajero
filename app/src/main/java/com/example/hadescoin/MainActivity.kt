package com.example.hadescoin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
// Importamos únicamente la navegación de tu proyecto
import com.example.hadescoin.presentation.navigation.AppNavigation
import com.example.hadescoin.ui.theme.HadesCoinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilita el diseño de pantalla completa (EdgeToEdge) como hace el profe
        enableEdgeToEdge()

        setContent {
            // Usamos el tema de tu aplicación
            HadesCoinTheme {
                // El punto de entrada es ÚNICAMENTE el AppNavigation
                // Esto hace que la navegación tome el control desde el segundo 1
                AppNavigation()
            }
        }
    }
}