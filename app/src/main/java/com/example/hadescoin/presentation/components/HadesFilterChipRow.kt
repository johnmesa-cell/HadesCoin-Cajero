package com.example.hadescoin.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hadescoin.ui.theme.HadesCyan
import com.example.hadescoin.ui.theme.HadesNavyDark
import com.example.hadescoin.ui.theme.HadesOnDark
import com.example.hadescoin.ui.theme.HadesPurple

@Composable
fun HadesFilterChipRow(
    opciones: List<String>,
    seleccionado: String,
    onSeleccion: (String) -> Unit,
    modifier: Modifier = Modifier,
    labelTransform: (String) -> String = { it }
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opciones.forEach { opcion ->
            FilterChip(
                selected = seleccionado == opcion,
                onClick = { onSeleccion(opcion) },
                label = {
                    Text(
                        text = labelTransform(opcion).uppercase(),
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = HadesCyan.copy(alpha = 0.2f),
                    selectedLabelColor = HadesCyan,
                    containerColor = HadesNavyDark,
                    labelColor = HadesOnDark.copy(alpha = 0.5f)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = seleccionado == opcion,
                    selectedBorderColor = HadesCyan.copy(alpha = 0.5f),
                    borderColor = HadesPurple.copy(alpha = 0.3f)
                )
            )
        }
    }
}

