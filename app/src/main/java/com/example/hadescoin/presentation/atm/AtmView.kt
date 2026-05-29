package com.example.hadescoin.presentation.atm

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hadescoin.R
import com.example.hadescoin.presentation.components.*
import com.example.hadescoin.ui.theme.*

@Composable
fun AtmView(
    phoneNumber: String,
    operation: AtmOperation,
    navController: NavController,
    viewModel: AtmViewModel = viewModel()
) {
    val cargando by viewModel.cargando.observeAsState(false)
    val exito    by viewModel.exito.observeAsState()
    val error    by viewModel.error.observeAsState()

    var showExito by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var mensaje   by remember { mutableStateOf("") }

    LaunchedEffect(exito) {
        exito?.let { mensaje = it; showExito = true; viewModel.clearExito() }
    }
    LaunchedEffect(error) {
        error?.let { mensaje = it; showError = true; viewModel.clearError() }
    }

    AtmViewContent(
        operation = operation,
        cargando  = cargando,
        onBack    = { navController.popBackStack() },
        onExecute = { amount, pin, ref ->
            viewModel.execute(operation, phoneNumber, amount, pin, ref)
        }
    )

    if (cargando) ShowLoadingAlertDialog()

    if (showExito) {
        ShowMessageAlertDialog(
            onConfirmation = {
                showExito = false
                // Vuelve al menú del cajero (home), no al login
                navController.popBackStack()
            },
            dialogTitle = stringResource(R.string.dialog_success_title),
            dialogText  = mensaje
        )
    }

    if (showError) {
        ShowMessageAlertDialog(
            onConfirmation = { showError = false },
            dialogTitle    = stringResource(R.string.dialog_error_title),
            dialogText     = mensaje
        )
    }
}

@Composable
fun AtmViewContent(
    operation: AtmOperation,
    cargando: Boolean,
    onBack: () -> Unit = {},
    onExecute: (Double, String, String) -> Unit = { _, _, _ -> }
) {
    var amountText by remember(operation) { mutableStateOf("") }
    var reference  by remember(operation) { mutableStateOf("") }
    var pin        by remember(operation) { mutableStateOf("") }

    val amount    = amountText.toDoubleOrNull() ?: 0.0
    val canSubmit = amount > 0 && pin.length == 4 &&
            (operation != AtmOperation.PAYMENT || reference.isNotBlank())

    val accentColor = when (operation) {
        AtmOperation.DEPOSIT  -> HadesCyan
        AtmOperation.WITHDRAW -> HadesOrange
        AtmOperation.PAYMENT  -> HadesPurple
    }
    val sectionLabel = when (operation) {
        AtmOperation.DEPOSIT  -> "> ${stringResource(R.string.atm_deposit_title)}"
        AtmOperation.WITHDRAW -> "> ${stringResource(R.string.atm_withdraw_title)}"
        AtmOperation.PAYMENT  -> "> ${stringResource(R.string.atm_payment_title)}"
    }
    val confirmLabel = when (operation) {
        AtmOperation.DEPOSIT  -> stringResource(R.string.btn_atm_deposit)
        AtmOperation.WITHDRAW -> stringResource(R.string.btn_atm_withdraw)
        AtmOperation.PAYMENT  -> stringResource(R.string.btn_atm_pay)
    }

    HadesBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // ── Header ────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint               = accentColor,
                        modifier           = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text          = stringResource(R.string.home_app_label),
                        fontSize      = 10.sp,
                        letterSpacing = 4.sp,
                        fontWeight    = FontWeight.Black,
                        color         = HadesPurple
                    )
                    Text(
                        text       = when (operation) {
                            AtmOperation.DEPOSIT  -> stringResource(R.string.atm_deposit_title)
                            AtmOperation.WITHDRAW -> stringResource(R.string.atm_withdraw_title)
                            AtmOperation.PAYMENT  -> stringResource(R.string.atm_payment_title)
                        },
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Black,
                        color      = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Formulario ─────────────────────────────────────────
            HadesCardBox {
                Text(
                    text          = sectionLabel,
                    fontSize      = 12.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color         = accentColor
                )

                HadesTextField(
                    value         = amountText,
                    onValueChange = {
                        if (it.length <= 12 && it.all { c -> c.isDigit() || c == '.' }) amountText = it
                    },
                    label        = stringResource(R.string.atm_label_amount),
                    keyboardType = KeyboardType.Decimal
                )

                if (operation == AtmOperation.PAYMENT) {
                    HadesTextField(
                        value         = reference,
                        onValueChange = { if (it.length <= 30) reference = it },
                        label         = stringResource(R.string.atm_label_reference),
                        keyboardType  = KeyboardType.Text
                    )
                }

                HadesTextField(
                    value         = pin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { c -> c.isDigit() }) pin = it
                    },
                    label        = stringResource(R.string.atm_label_pin),
                    isPassword   = true,
                    keyboardType = KeyboardType.NumberPassword
                )

                Spacer(modifier = Modifier.height(4.dp))

                HadesButton(
                    text         = confirmLabel,
                    textCargando = stringResource(R.string.text_loading),
                    onClick      = { onExecute(amount, pin, reference) },
                    enabled      = canSubmit && !cargando,
                    cargando     = cargando
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "ATM — Depósito")
@Composable
fun AtmDepositPreview() {
    HadesCoinTheme { AtmViewContent(operation = AtmOperation.DEPOSIT, cargando = false) }
}

@Preview(showBackground = true, showSystemUi = true, name = "ATM — Retiro")
@Composable
fun AtmWithdrawPreview() {
    HadesCoinTheme { AtmViewContent(operation = AtmOperation.WITHDRAW, cargando = false) }
}

@Preview(showBackground = true, showSystemUi = true, name = "ATM — Pago")
@Composable
fun AtmPaymentPreview() {
    HadesCoinTheme { AtmViewContent(operation = AtmOperation.PAYMENT, cargando = false) }
}
