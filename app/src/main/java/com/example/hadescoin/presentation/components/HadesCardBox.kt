package com.example.hadescoin.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.hadescoin.ui.theme.HadesCyan
import com.example.hadescoin.ui.theme.HadesNavy
import com.example.hadescoin.ui.theme.HadesNavyDark
import com.example.hadescoin.ui.theme.HadesPurple

@Composable
fun HadesCardBox(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(HadesPurple, HadesCyan.copy(alpha = 0.5f))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                Brush.verticalGradient(
                    colors = listOf(HadesNavyDark, HadesNavy)
                )
            )
            .padding(24.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

