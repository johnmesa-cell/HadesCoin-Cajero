package com.example.hadescoin.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Diálogo de carga — bloquea la pantalla mientras Firebase responde.
 * No tiene botones ni se puede cerrar tocando fuera.
 */
@Composable
fun ShowLoadingAlertDialog() {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Cargando...") },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        },
        confirmButton = { }
    )
}

/**
 * Diálogo de mensaje — muestra errores o confirmaciones.
 * Recibe Strings directamente, compatible con LiveData de todos los ViewModels.
 *
 * @param onConfirmation  Acción al pulsar Aceptar
 * @param dialogTitle     Título del diálogo (ejemplo: "Error", "Éxito")
 * @param dialogText      Mensaje del cuerpo
 */
@Composable
fun ShowMessageAlertDialog(
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String
) {
    AlertDialog(
        title = { Text(text = dialogTitle) },
        text  = { Text(text = dialogText) },
        onDismissRequest = { },
        confirmButton = {
            Button(onClick = { onConfirmation() }) {
                Text("Aceptar")
            }
        }
    )
}
