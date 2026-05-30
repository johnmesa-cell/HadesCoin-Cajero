package com.example.hadescoin.presentation.atm

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hadescoin.R
import com.example.hadescoin.presentation.components.*
import com.example.hadescoin.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun AtmView(
    operation:   AtmOperation,
    navController: NavController,
    viewModel: AtmViewModel = viewModel()
) {
    val cargando          by viewModel.cargando.observeAsState(false)
    val exito             by viewModel.exito.observeAsState()
    val error             by viewModel.error.observeAsState()
    val bloqueadoHastaMs  by viewModel.bloqueadoHastaMs.observeAsState(0L)

    var segundosRestantes by remember { mutableStateOf(0) }

    LaunchedEffect(bloqueadoHastaMs) {
        while (System.currentTimeMillis() < bloqueadoHastaMs) {
            segundosRestantes = ((bloqueadoHastaMs - System.currentTimeMillis()) / 1000).toInt()
            delay(1000)
        }
        segundosRestantes = 0
    }

    val estaBloqueado = bloqueadoHastaMs > System.currentTimeMillis()

    var showExito by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var mensaje   by remember { mutableStateOf("") }

    LaunchedEffect(exito) {
        exito?.let { mensaje = it; showExito = true; viewModel.clearExito() }
    }
    LaunchedEffect(error) {
        error?.let { mensaje = it; showError = true; viewModel.clearError() }
    }

    if (operation == AtmOperation.WITHDRAW_CODE) {
        WithdrawCodeAtmContent(
            cargando    = cargando,
            bloqueado   = estaBloqueado,
            secsBloqueo = segundosRestantes,
            onBack      = { navController.popBackStack() },
            onExecute   = { phone, code, amount ->
                viewModel.executeWithdrawalCode(phone, code, amount)
            }
        )
    } else {
        AtmViewContent(
            operation = operation,
            cargando  = cargando,
            onBack    = { navController.popBackStack() },
            onExecute = { phone, amount, ref ->
                viewModel.execute(operation, phone, amount, ref)
            }
        )
    }

    if (cargando) ShowLoadingAlertDialog()

    if (showExito) {
        ShowMessageAlertDialog(
            onConfirmation = { showExito = false; navController.popBackStack() },
            dialogTitle    = stringResource(R.string.dialog_success_title),
            dialogText     = mensaje
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

// ─── Pantalla de retiro con código temporal (sin PIN) ─────────────────────
@Composable
fun WithdrawCodeAtmContent(
    cargando:    Boolean,
    bloqueado:   Boolean,
    secsBloqueo: Int,
    onBack:      () -> Unit = {},
    onExecute:   (phone: String, code: String, amount: Double) -> Unit = { _, _, _ -> }
) {
    var phone      by remember { mutableStateOf("") }
    var code       by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    val amount     = amountText.toDoubleOrNull() ?: 0.0
    val canSubmit  = phone.length == 10 && phone.startsWith("3") && code.length == 6 && amount > 0 && !bloqueado && !cargando

    HadesBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(48.dp))

            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint     = HadesOrange,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(text = "HADESCOIN ATM", fontSize = 10.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Black, color = HadesPurple)
                    Text(text = "Retiro con Código", fontSize = 18.sp, fontWeight = FontWeight.Black, color = HadesOrange)
                }
            }

            Spacer(Modifier.height(32.dp))

            // Banner de bloqueo
            AnimatedVisibility(
                visible = bloqueado,
                enter   = fadeIn(tween(300)) + expandVertically(tween(300)),
                exit    = fadeOut(tween(300)) + shrinkVertically(tween(300))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(listOf(HadesOrange.copy(alpha = 0.15f), HadesOrange.copy(alpha = 0.05f))),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, HadesOrange.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = HadesOrange, modifier = Modifier.size(18.dp))
                        Text(
                            text       = "🔒 Bloqueado por $secsBloqueo seg. Demasiados intentos fallidos.",
                            fontSize   = 12.sp,
                            color      = HadesOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            Spacer(Modifier.height(8.dp))

            HadesCardBox {
                Text(text = "> RETIRO CON CÓDIGO TEMPORAL", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = HadesOrange)

                HadesTextField(
                    value         = phone,
                    onValueChange = { v -> if (v.length <= 10 && v.all { c -> c.isDigit() }) phone = v },
                    label         = "Teléfono del usuario",
                    keyboardType  = KeyboardType.Phone
                )

                HadesTextField(
                    value         = code,
                    onValueChange = { v -> if (v.length <= 6 && v.all { c -> c.isDigit() }) code = v },
                    label         = "Código temporal (6 dígitos)",
                    keyboardType  = KeyboardType.Number
                )

                HadesTextField(
                    value         = amountText,
                    onValueChange = { v -> if (v.length <= 12 && v.all { c -> c.isDigit() || c == '.' }) amountText = v },
                    label         = "Monto a retirar",
                    keyboardType  = KeyboardType.Decimal
                )

                Spacer(Modifier.height(4.dp))

                HadesButton(
                    text         = "PROCESAR RETIRO",
                    textCargando = "PROCESANDO...",
                    onClick      = { onExecute(phone, code, amount) },
                    enabled      = canSubmit,
                    cargando     = cargando
                )
            }

            Spacer(Modifier.height(12.dp))

            // Info instructiva
            Text(
                text      = "El usuario genera el código desde su app HadesCoin.\nEl código expira en 25 minutos y es de un solo uso.",
                fontSize  = 11.sp,
                color     = HadesOnDark.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(48.dp))
        }
    }
}

// ─── Pantalla estándar (depósito / pago) ────────────────
@Composable
fun AtmViewContent(
    operation: AtmOperation,
    cargando:  Boolean,
    onBack:    () -> Unit = {},
    onExecute: (String, Double, String) -> Unit = { _, _, _ -> }
) {
    var phone      by remember(operation) { mutableStateOf("") }
    var amountText by remember(operation) { mutableStateOf("") }
    var reference  by remember(operation) { mutableStateOf("") }

    val amount    = amountText.toDoubleOrNull() ?: 0.0
    val canSubmit = phone.length == 10 && phone.startsWith("3") && amount > 0 &&
            (operation != AtmOperation.PAYMENT || reference.isNotBlank())

    val accentColor  = when (operation) {
        AtmOperation.DEPOSIT  -> HadesCyan
        else                  -> HadesPurple
    }
    val sectionLabel = when (operation) {
        AtmOperation.DEPOSIT  -> "> ${stringResource(R.string.atm_deposit_title)}"
        else                  -> "> ${stringResource(R.string.atm_payment_title)}"
    }
    val confirmLabel = when (operation) {
        AtmOperation.DEPOSIT  -> stringResource(R.string.btn_atm_deposit)
        else                  -> stringResource(R.string.btn_atm_pay)
    }

    HadesBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(48.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back), tint = accentColor, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(text = stringResource(R.string.home_app_label), fontSize = 10.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Black, color = HadesPurple)
                    Text(
                        text = when (operation) {
                            AtmOperation.DEPOSIT  -> stringResource(R.string.atm_deposit_title)
                            else                  -> stringResource(R.string.atm_payment_title)
                        },
                        fontSize = 18.sp, fontWeight = FontWeight.Black, color = accentColor
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
            HadesCardBox {
                Text(text = sectionLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = accentColor)
                
                HadesTextField(
                    value         = phone,
                    onValueChange = { v -> if (v.length <= 10 && v.all { c -> c.isDigit() }) phone = v },
                    label         = "Teléfono del usuario",
                    keyboardType  = KeyboardType.Phone
                )

                HadesTextField(
                    value = amountText, onValueChange = { if (it.length <= 12 && it.all { c -> c.isDigit() || c == '.' }) amountText = it },
                    label = stringResource(R.string.atm_label_amount), keyboardType = KeyboardType.Decimal
                )
                if (operation == AtmOperation.PAYMENT) {
                    HadesTextField(
                        value = reference, onValueChange = { if (it.length <= 30) reference = it },
                        label = stringResource(R.string.atm_label_reference), keyboardType = KeyboardType.Text
                    )
                }
                Spacer(Modifier.height(4.dp))
                HadesButton(
                    text = confirmLabel, textCargando = stringResource(R.string.text_loading),
                    onClick = { onExecute(phone, amount, reference) },
                    enabled = canSubmit && !cargando, cargando = cargando
                )
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "ATM — Retiro con Código")
@Composable
fun AtmWithdrawCodePreview() {
    HadesCoinTheme { WithdrawCodeAtmContent(cargando = false, bloqueado = false, secsBloqueo = 0) }
}

@Preview(showBackground = true, showSystemUi = true, name = "ATM — Bloqueado")
@Composable
fun AtmWithdrawCodeBlockedPreview() {
    HadesCoinTheme { WithdrawCodeAtmContent(cargando = false, bloqueado = true, secsBloqueo = 142) }
}

@Preview(showBackground = true, showSystemUi = true, name = "ATM — Depósito")
@Composable
fun AtmDepositPreview() {
    HadesCoinTheme { AtmViewContent(operation = AtmOperation.DEPOSIT, cargando = false) }
}
