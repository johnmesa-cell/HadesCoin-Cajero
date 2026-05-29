package com.example.hadescoin.presentation.atm

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.presentation.components.*
import com.example.hadescoin.ui.theme.*
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────
// Entry point (con NavController + ViewModel)
// ─────────────────────────────────────────────────────────────────────
@Composable
fun AtmView(
    phoneNumber: String,
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
        phoneNumber = phoneNumber,
        cargando    = cargando,
        onBack      = { navController.popBackStack() },
        onExecute   = { op, amount, pin, ref ->
            viewModel.execute(op, phoneNumber, amount, pin, ref)
        }
    )

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

// ─────────────────────────────────────────────────────────────────────
// Contenido puró (previsualización + test)
// ─────────────────────────────────────────────────────────────────────
@Composable
fun AtmViewContent(
    phoneNumber: String,
    cargando: Boolean,
    appUser: AppUser? = null,
    onBack: () -> Unit = {},
    onExecute: (AtmOperation, Double, String, String) -> Unit = { _, _, _, _ -> }
) {
    var selectedOp by remember { mutableStateOf(AtmOperation.DEPOSIT) }
    var amountText by remember { mutableStateOf("") }
    var reference  by remember { mutableStateOf("") }
    var pin        by remember { mutableStateOf("") }

    val amount    = amountText.toDoubleOrNull() ?: 0.0
    val canSubmit = amount > 0 && pin.length == 4 &&
            (selectedOp != AtmOperation.PAYMENT || reference.isNotBlank())

    val accentColor = when (selectedOp) {
        AtmOperation.DEPOSIT  -> HadesCyan
        AtmOperation.WITHDRAW -> HadesOrange
        AtmOperation.PAYMENT  -> HadesPurple
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
                        tint               = HadesCyan,
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
                        text       = stringResource(R.string.atm_title),
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Black,
                        color      = HadesOnDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Tarjeta de saldo ─────────────────────────────────
            AtmBalanceCard(appUser = appUser, accentColor = accentColor)

            Spacer(modifier = Modifier.height(24.dp))

            // ── Selector de operación ───────────────────────────
            Text(
                text          = stringResource(R.string.atm_subtitle),
                fontSize      = 11.sp,
                letterSpacing = 1.sp,
                color         = HadesOnDark.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AtmOperationChip(
                    label    = stringResource(R.string.atm_deposit_title),
                    icon     = Icons.Filled.ArrowDownward,
                    color    = HadesCyan,
                    selected = selectedOp == AtmOperation.DEPOSIT,
                    onClick  = { selectedOp = AtmOperation.DEPOSIT; amountText = ""; reference = ""; pin = "" },
                    modifier = Modifier.weight(1f)
                )
                AtmOperationChip(
                    label    = stringResource(R.string.atm_withdraw_title),
                    icon     = Icons.Filled.ArrowUpward,
                    color    = HadesOrange,
                    selected = selectedOp == AtmOperation.WITHDRAW,
                    onClick  = { selectedOp = AtmOperation.WITHDRAW; amountText = ""; reference = ""; pin = "" },
                    modifier = Modifier.weight(1f)
                )
                AtmOperationChip(
                    label    = stringResource(R.string.atm_payment_title),
                    icon     = Icons.Filled.CreditCard,
                    color    = HadesPurple,
                    selected = selectedOp == AtmOperation.PAYMENT,
                    onClick  = { selectedOp = AtmOperation.PAYMENT; amountText = ""; reference = ""; pin = "" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Formulario animado ──────────────────────────────
            AnimatedContent(
                targetState = selectedOp,
                transitionSpec = {
                    fadeIn(animationSpec = androidx.compose.animation.core.tween(220)) togetherWith
                    fadeOut(animationSpec = androidx.compose.animation.core.tween(120))
                },
                label = "atm_form_transition"
            ) { op ->
                val label = when (op) {
                    AtmOperation.DEPOSIT  -> "> ${stringResource(R.string.atm_deposit_title)}"
                    AtmOperation.WITHDRAW -> "> ${stringResource(R.string.atm_withdraw_title)}"
                    AtmOperation.PAYMENT  -> "> ${stringResource(R.string.atm_payment_title)}"
                }

                HadesCardBox {
                    Text(
                        text          = label,
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

                    if (op == AtmOperation.PAYMENT) {
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

                    // ── Resumen previo ──────────────────────────────
                    AnimatedVisibility(
                        visible = amount > 0,
                        enter   = fadeIn() + expandVertically(),
                        exit    = fadeOut() + shrinkVertically()
                    ) {
                        AtmPreviewBanner(
                            op          = op,
                            amount      = amount,
                            balance     = appUser?.balance,
                            accentColor = accentColor
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    HadesButton(
                        text         = stringResource(R.string.btn_atm_confirm),
                        textCargando = stringResource(R.string.text_loading),
                        onClick      = { onExecute(op, amount, pin, reference) },
                        enabled      = canSubmit && !cargando,
                        cargando     = cargando
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Tarjeta de saldo en la parte superior
// ─────────────────────────────────────────────────────────────────────
@Composable
private fun AtmBalanceCard(appUser: AppUser?, accentColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(HadesNavyDark, accentColor.copy(alpha = 0.12f), HadesNavyDark)
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text          = "SALDO ACTUAL",
                    fontSize      = 9.sp,
                    letterSpacing = 2.sp,
                    color         = HadesOnDark.copy(alpha = 0.45f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                if (appUser != null) {
                    Text(
                        text       = "$ ${String.format(Locale.US, "%,.2f", appUser.balance)}",
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Black,
                        color      = accentColor
                    )
                } else {
                    Text(
                        text       = "$ ————",
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Black,
                        color      = HadesOnDark.copy(alpha = 0.25f)
                    )
                }
            }
            Box(
                modifier         = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Filled.AccountBalanceWallet,
                    contentDescription = null,
                    tint               = accentColor,
                    modifier           = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Banner de resumen previo a confirmar
// ─────────────────────────────────────────────────────────────────────
@Composable
private fun AtmPreviewBanner(
    op: AtmOperation,
    amount: Double,
    balance: Double?,
    accentColor: Color
) {
    val resultBalance = when {
        balance == null                                   -> null
        op == AtmOperation.DEPOSIT                       -> balance + amount
        op == AtmOperation.WITHDRAW || op == AtmOperation.PAYMENT -> balance - amount
        else -> balance
    }
    val insufficient = resultBalance != null && resultBalance < 0

    Spacer(modifier = Modifier.height(12.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (insufficient) HadesOrange.copy(alpha = 0.10f)
                else accentColor.copy(alpha = 0.08f)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text          = if (insufficient) "⚠ SALDO INSUFICIENTE" else "✓ RESUMEN",
                fontSize      = 9.sp,
                letterSpacing = 2.sp,
                fontWeight    = FontWeight.Black,
                color         = if (insufficient) HadesOrange else accentColor
            )
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "MONTO",    fontSize = 10.sp, color = HadesOnDark.copy(alpha = 0.5f), letterSpacing = 1.sp)
                Text(
                    text       = "$ ${String.format(Locale.US, "%,.2f", amount)}",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = accentColor
                )
            }
            if (resultBalance != null) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "SALDO RESULTANTE", fontSize = 10.sp, color = HadesOnDark.copy(alpha = 0.5f), letterSpacing = 1.sp)
                    Text(
                        text       = "$ ${String.format(Locale.US, "%,.2f", resultBalance)}",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (insufficient) HadesOrange else HadesOnDark
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}

// ─────────────────────────────────────────────────────────────────────
// Chip de operación (Depósito / Retiro / Pago)
// ─────────────────────────────────────────────────────────────────────
@Composable
private fun AtmOperationChip(
    label: String,
    icon: ImageVector,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg     = if (selected) color.copy(alpha = 0.18f) else HadesNavyDark
    val border = if (selected) color else HadesOnDark.copy(alpha = 0.12f)

    Surface(
        onClick        = onClick,
        modifier       = modifier,
        shape          = RoundedCornerShape(14.dp),
        color          = bg,
        border         = androidx.compose.foundation.BorderStroke(1.5.dp, border),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier            = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = if (selected) color else HadesOnDark.copy(alpha = 0.35f),
                modifier           = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text          = label,
                fontSize      = 9.sp,
                fontWeight    = if (selected) FontWeight.Black else FontWeight.Normal,
                letterSpacing = 1.sp,
                textAlign     = TextAlign.Center,
                color         = if (selected) color else HadesOnDark.copy(alpha = 0.35f)
            )
            if (selected) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true, name = "ATM — Depósito (sin usuario)")
@Composable
fun AtmViewDepositPreview() {
    HadesCoinTheme {
        AtmViewContent(phoneNumber = "3001234567", cargando = false)
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "ATM — Retiro (con saldo)")
@Composable
fun AtmViewWithdrawPreview() {
    HadesCoinTheme {
        AtmViewContent(
            phoneNumber = "3001234567",
            cargando    = false,
            appUser     = AppUser(fullName = "Juan Pérez", balance = 850.0, phoneNumber = "3001234567")
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "ATM — Pago (con saldo)")
@Composable
fun AtmViewPaymentPreview() {
    HadesCoinTheme {
        AtmViewContent(
            phoneNumber = "3001234567",
            cargando    = false,
            appUser     = AppUser(fullName = "Juan Pérez", balance = 200.0, phoneNumber = "3001234567")
        )
    }
}
