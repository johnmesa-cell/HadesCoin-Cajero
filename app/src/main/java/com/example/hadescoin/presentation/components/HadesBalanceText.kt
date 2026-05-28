package com.example.hadescoin.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hadescoin.ui.theme.HadesOnDark
import java.util.Locale

@Composable
fun HadesBalanceText(
    balance: Double,
    visible: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (visible)
                "$ ${String.format(Locale.US, "%,.2f", balance)}"
            else "$ ••••••",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            color = HadesOnDark
        )
        IconButton(
            onClick = onToggle,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (visible) Icons.Filled.Visibility
                              else Icons.Filled.VisibilityOff,
                contentDescription = if (visible) "Ocultar saldo" else "Mostrar saldo",
                tint = HadesOnDark.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

