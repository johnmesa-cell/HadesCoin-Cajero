package com.example.hadescoin.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

data class HadesSummaryItem(
    val label: String,
    val valor: Double,
    val color: Color,
    val prefijo: String = ""
)

@Composable
fun HadesSummaryRow(
    items: List<HadesSummaryItem>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { item ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(item.color.copy(alpha = 0.08f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = item.label,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        color = item.color.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${item.prefijo}$ ${String.format(Locale.US, "%,.2f", item.valor)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = item.color
                    )
                }
            }
        }
    }
}
