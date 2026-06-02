package com.example.hadescoin.presentation.atm

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.hadescoin.domain.model.ServiceItem
import com.example.hadescoin.presentation.components.*
import com.example.hadescoin.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun AtmView(
    operation:     AtmOperation,
    navController: NavController,
    viewModel:     AtmViewModel = viewModel()
) {
    val cargando         by viewModel.cargando.observeAsState(false)
    val exito            by viewModel.exito.observeAsState()
    val error            by viewModel.error.observeAsState()
    val bloqueadoHastaMs by viewModel.bloqueadoHastaMs.observeAsState(0L)

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

    LaunchedEffect(exito) { exito?.let { mensaje = it; showExito = true; viewModel.clearExito() } }
    LaunchedEffect(error) { error?.let { mensaje = it; showError = true; viewModel.clearError() } }

    when (operation) {
        AtmOperation.WITHDRAW_CODE -> WithdrawCodeAtmContent(
            cargando    = cargando,
            bloqueado   = estaBloqueado,
            secsBloqueo = segundosRestantes,
            onBack      = { navController.popBackStack() },
            onExecute   = { phone, code, amount ->
                viewModel.executeWithdrawalCode(phone, code, amount)
            }
        )
        AtmOperation.PAYMENT -> PaymentAtmContent(
            servicios = viewModel.servicios,
            cargando  = cargando,
            onBack    = { navController.popBackStack() },
            onExecute = { phone, amount, reference, pin ->
                viewModel.executePayment(phone, amount, reference, pin)
            }
        )
        AtmOperation.DEPOSIT -> AtmDepositContent(
            cargando  = cargando,
            onBack    = { navController.popBackStack() },
            onExecute = { phone, amount ->
                viewModel.executeDeposit(phone, amount)
            }
        )
    }

    if (cargando) ShowLoadingAlertDialog()

    if (showExito) {
        AlertDialog(
            onDismissRequest = { showExito = false; navController.popBackStack() },
            containerColor   = HadesNavyDark,
            icon = {
                Icon(
                    imageVector        = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint               = HadesCyan,
                    modifier           = Modifier.size(48.dp)
                )
            },
            title         = { Text(stringResource(R.string.dialog_success_title), color = HadesCyan) },
            text          = { Text(mensaje, color = HadesOnDark) },
            confirmButton = {
                HadesButton(
                    text    = stringResource(R.string.btn_accept),
                    onClick = { showExito = false; navController.popBackStack() }
                )
            }
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

// ══════════════════════════════════════════════════════════════
// PAGO DE SERVICIOS — flujo de 3 pasos
// ══════════════════════════════════════════════════════════════

@Composable
fun PaymentAtmContent(
    servicios: List<ServiceItem>,
    cargando:  Boolean,
    onBack:    () -> Unit,
    onExecute: (phone: String, amount: Double, reference: String, pin: String) -> Unit
) {
    var paso                 by remember { mutableStateOf(1) }
    var servicioSeleccionado by remember { mutableStateOf<ServiceItem?>(null) }
    var referencia           by remember { mutableStateOf("") }
    var montoText            by remember { mutableStateOf("") }
    var phone                by remember { mutableStateOf("") }
    var pin                  by remember { mutableStateOf("") }

    HadesScreen {   // ← HadesBackground + safeDrawingPadding
        when (paso) {
            // ── Paso 1: Grid de categorías ──────────────────────────────────
            1 -> LazyVerticalGrid(
                columns               = GridCells.Fixed(2),
                verticalArrangement   = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier              = Modifier.fillMaxSize().padding(24.dp)
            ) {
                item(span = { GridItemSpan(2) }) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.cd_back),
                                    tint     = HadesPurple,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("HADESCOIN ATM", fontSize = 10.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Black, color = HadesPurple)
                                Text(stringResource(R.string.atm_payment_title), fontSize = 18.sp, fontWeight = FontWeight.Black, color = HadesPurple)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text       = stringResource(R.string.payment_paso1_titulo),
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = HadesOnDark.copy(alpha = 0.7f),
                            textAlign  = TextAlign.Center,
                            modifier   = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }
                items(servicios) { servicio ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(HadesNavyDark)
                            .border(1.dp, HadesOnDark.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                            .clickable { servicioSeleccionado = servicio; paso = 2 }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier         = Modifier.size(48.dp).clip(CircleShape).background(HadesPurple.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(servicio.icono, contentDescription = null, tint = HadesPurple, modifier = Modifier.size(24.dp))
                            }
                            Text(
                                text       = stringResource(servicio.nombreRes),
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = HadesOnDark.copy(alpha = 0.8f),
                                textAlign  = TextAlign.Center,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }

            // ── Paso 2: Referencia y monto ─────────────────────────────────
            2 -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { paso = 1 }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_back), tint = HadesPurple, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("HADESCOIN ATM", fontSize = 10.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Black, color = HadesPurple)
                        Text(stringResource(R.string.atm_payment_title), fontSize = 18.sp, fontWeight = FontWeight.Black, color = HadesPurple)
                    }
                }
                servicioSeleccionado?.let { srv ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(HadesNavyDark)
                            .border(1.dp, HadesPurple.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(srv.icono, null, tint = HadesPurple, modifier = Modifier.size(24.dp))
                            Text(stringResource(srv.nombreRes), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HadesPurple)
                        }
                    }
                }
                Text(stringResource(R.string.payment_paso2_titulo), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = HadesOnDark.copy(alpha = 0.7f))
                HadesTextField(
                    value         = referencia,
                    onValueChange = { if (it.length <= 40) referencia = it },
                    label         = stringResource(R.string.payment_referencia_hint),
                    keyboardType  = KeyboardType.Number
                )
                HadesTextField(
                    value         = montoText,
                    onValueChange = { if (it.length <= 12 && it.all { c -> c.isDigit() || c == '.' }) montoText = it },
                    label         = stringResource(R.string.payment_monto_hint),
                    keyboardType  = KeyboardType.Decimal
                )
                Spacer(Modifier.weight(1f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HadesButton(text = stringResource(R.string.btn_back), onClick = { paso = 1 }, modifier = Modifier.weight(1f))
                    HadesButton(
                        text     = stringResource(R.string.btn_continue),
                        onClick  = { paso = 3 },
                        enabled  = referencia.isNotBlank() && (montoText.toDoubleOrNull() ?: 0.0) > 0,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Paso 3: Teléfono + PIN del usuario ─────────────────────────
            3 -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { paso = 2 }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_back), tint = HadesPurple, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("HADESCOIN ATM", fontSize = 10.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Black, color = HadesPurple)
                        Text(stringResource(R.string.payment_paso3_titulo), fontSize = 18.sp, fontWeight = FontWeight.Black, color = HadesPurple)
                    }
                }
                servicioSeleccionado?.let { srv ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(HadesNavyDark)
                            .border(1.dp, HadesOnDark.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(srv.icono, null, tint = HadesPurple, modifier = Modifier.size(22.dp))
                                Column {
                                    Text(stringResource(R.string.payment_resumen_servicio), fontSize = 10.sp, color = HadesOnDark.copy(alpha = 0.5f))
                                    Text(stringResource(srv.nombreRes), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HadesOnDark)
                                }
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(HadesOnDark.copy(alpha = 0.05f)))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(stringResource(R.string.payment_resumen_referencia), fontSize = 10.sp, color = HadesOnDark.copy(alpha = 0.5f))
                                    Text(referencia, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HadesOnDark)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(stringResource(R.string.payment_resumen_monto), fontSize = 10.sp, color = HadesOnDark.copy(alpha = 0.5f))
                                    Text("$$montoText", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HadesPurple)
                                }
                            }
                        }
                    }
                }
                Text(
                    text     = "El usuario debe ingresar su teléfono y PIN para autorizar el pago.",
                    fontSize = 11.sp,
                    color    = HadesOnDark.copy(alpha = 0.45f)
                )
                HadesTextField(
                    value         = phone,
                    onValueChange = { v -> if (v.length <= 10 && v.all { c -> c.isDigit() }) phone = v },
                    label         = stringResource(R.string.payment_phone_hint),
                    keyboardType  = KeyboardType.Phone
                )
                HadesTextField(
                    value         = pin,
                    onValueChange = { v -> if (v.length <= 4 && v.all { c -> c.isDigit() }) pin = v },
                    label         = stringResource(R.string.payment_pin_hint),
                    keyboardType  = KeyboardType.NumberPassword,
                    isPassword    = true
                )
                Spacer(Modifier.weight(1f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HadesButton(
                        text     = stringResource(R.string.btn_back),
                        onClick  = { paso = 2 },
                        enabled  = !cargando,
                        modifier = Modifier.weight(1f)
                    )
                    HadesButton(
                        text         = stringResource(R.string.btn_atm_pay),
                        textCargando = stringResource(R.string.text_loading),
                        onClick      = { onExecute(phone, montoText.toDoubleOrNull() ?: 0.0, referencia, pin) },
                        enabled      = phone.length == 10 && pin.length == 4 && !cargando,
                        cargando     = cargando,
                        modifier     = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
// DEPÓSITO ATM
// ══════════════════════════════════════════════════════════════

@Composable
fun AtmDepositContent(
    cargando:  Boolean,
    onBack:    () -> Unit,
    onExecute: (phone: String, amount: Double) -> Unit
) {
    var phone      by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    val amount     = amountText.toDoubleOrNull() ?: 0.0
    val canSubmit  = phone.length == 10 && phone.startsWith("3") && amount > 0 && !cargando

    HadesScreen {   // ← HadesBackground + safeDrawingPadding
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_back), tint = HadesCyan, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("HADESCOIN ATM", fontSize = 10.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Black, color = HadesPurple)
                    Text(stringResource(R.string.atm_deposit_title), fontSize = 18.sp, fontWeight = FontWeight.Black, color = HadesCyan)
                }
            }
            Spacer(Modifier.height(24.dp))
            HadesCardBox {
                Text("> ${stringResource(R.string.atm_deposit_title)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = HadesCyan)
                HadesTextField(
                    value         = phone,
                    onValueChange = { v -> if (v.length <= 10 && v.all { c -> c.isDigit() }) phone = v },
                    label         = stringResource(R.string.atm_label_phone),
                    keyboardType  = KeyboardType.Phone
                )
                HadesTextField(
                    value         = amountText,
                    onValueChange = { if (it.length <= 12 && it.all { c -> c.isDigit() || c == '.' }) amountText = it },
                    label         = stringResource(R.string.atm_label_amount),
                    keyboardType  = KeyboardType.Decimal
                )
                Spacer(Modifier.height(4.dp))
                HadesButton(
                    text         = stringResource(R.string.btn_atm_deposit),
                    textCargando = stringResource(R.string.text_loading),
                    onClick      = { onExecute(phone, amount) },
                    enabled      = canSubmit,
                    cargando     = cargando
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
// RETIRO CON CÓDIGO TEMPORAL
// ══════════════════════════════════════════════════════════════

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

    HadesScreen {   // ← HadesBackground + safeDrawingPadding
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_back), tint = HadesOrange, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("HADESCOIN ATM", fontSize = 10.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Black, color = HadesPurple)
                    Text("Retiro con Código", fontSize = 18.sp, fontWeight = FontWeight.Black, color = HadesOrange)
                }
            }
            Spacer(Modifier.height(16.dp))

            AnimatedVisibility(
                visible = bloqueado,
                enter   = fadeIn(tween(300)) + expandVertically(tween(300)),
                exit    = fadeOut(tween(300)) + shrinkVertically(tween(300))
            ) {
                Column {
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
                            Icon(Icons.Filled.Lock, null, tint = HadesOrange, modifier = Modifier.size(18.dp))
                            Text("🔒 Bloqueado por $secsBloqueo seg. Demasiados intentos fallidos.", fontSize = 12.sp, color = HadesOrange, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            HadesCardBox {
                Text("> RETIRO CON CÓDIGO TEMPORAL", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = HadesOrange)
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
            Text(
                text      = "El usuario genera el código desde su app HadesCoin.\nEl código expira en 25 minutos y es de un solo uso.",
                fontSize  = 11.sp,
                color     = HadesOnDark.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )
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
    HadesCoinTheme { AtmDepositContent(cargando = false, onBack = {}, onExecute = { _, _ -> }) }
}
