package com.example.hadescoin.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.hadescoin.ui.theme.HadesCyan
import com.example.hadescoin.ui.theme.HadesOnDark
import com.example.hadescoin.ui.theme.HadesPurple

@Composable
fun HadesTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isError: Boolean = false,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    prefix: (@Composable () -> Unit)? = null,
    supportingText: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        enabled = enabled,
        isError = isError,
        visualTransformation = if (isPassword) PasswordVisualTransformation()
                               else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        prefix = prefix,
        supportingText = supportingText,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = HadesCyan,
            unfocusedBorderColor = HadesPurple.copy(alpha = 0.5f),
            focusedLabelColor    = HadesCyan,
            unfocusedLabelColor  = HadesOnDark.copy(alpha = 0.5f),
            cursorColor          = HadesCyan,
            focusedTextColor     = HadesOnDark,
            unfocusedTextColor   = HadesOnDark,
            disabledTextColor    = HadesOnDark.copy(alpha = 0.5f),
            disabledBorderColor  = HadesPurple.copy(alpha = 0.25f),
            disabledLabelColor   = HadesOnDark.copy(alpha = 0.3f)
        )
    )
}



