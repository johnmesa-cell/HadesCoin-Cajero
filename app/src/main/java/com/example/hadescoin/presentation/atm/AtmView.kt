package com.example.hadescoin.presentation.atm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
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
            onConfirmation = {
                showExito = false
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
    phoneNumber: String,
    cargando: Boolean,
    onBack: () -> Unit = {},
    onExecute: (AtmOperation, Double, String, String) -> Unit = { _, _, _, _ -> }
) {
    var selectedOp  by remember { mutableStateOf(AtmOperation.DEPOSIT) }
    var amountText  by remember { mutableStateOf("") }
    var reference   by remember { mutableStateOf("") }
    var pin         by remember { mutableStateOf("") }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val canSubmit = amount > 0 && pin.length == 4 &&
            (selectedOp != AtmOperation.PAYMENT || reference.isNotBlank())

    HadesBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // ── Header ──────────────────────────────────────────────────
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

            Spacer(modifier = Modifier.height(28.dp))

            // ── Selector de operación ────────────────────────────────────
            Text(
                text          = stringResource(R.string.atm_subtitle),
                fontSize      = 11.sp,
                letterSpacing = 1.sp,
                color         = HadesOnDark.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(24.dp))

            // ── Formulario ───────────────────────────────────────────────
            val sectionColor = when (selectedOp) {
                AtmOperation.DEPOSIT  -> HadesCyan
                AtmOperation.WITHDRAW -> HadesOrange
                AtmOperation.PAYMENT  -> HadesPurple
            }
            val sectionLabel = when (selectedOp) {
                AtmOperation.DEPOSIT  -> "> ${stringResource(R.string.atm_deposit_title)}"
                AtmOperation.WITHDRAW -> "> ${stringResource(R.string.atm_withdraw_title)}"
                AtmOperation.PAYMENT  -> "> ${stringResource(R.string.atm_payment_title)}"
            }

            HadesCardBox {
                Text(
                    text          = sectionLabel,
                    fontSize      = 12.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color         = sectionColor
                )

                HadesTextField(
                    value         = amountText,
                    onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() || c == '.' }) amountText = it },
                    label         = stringResource(R.string.atm_label_amount),
                    keyboardType  = KeyboardType.Decimal
                )

                if (selectedOp == AtmOperation.PAYMENT) {
                    HadesTextField(
                        value         = reference,
                        onValueChange = { if (it.length <= 30) reference = it },
                        label         = stringResource(R.string.atm_label_reference),
                        keyboardType  = KeyboardType.Text
                    )
                }

                HadesTextField(
                    value         = pin,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pin = it },
                    label         = stringResource(R.string.atm_label_pin),
                    isPassword    = true,
                    keyboardType  = KeyboardType.NumberPassword
                )

                Spacer(modifier = Modifier.height(4.dp))

                HadesButton(
                    text         = stringResource(R.string.btn_atm_confirm),
                    textCargando = stringResource(R.string.text_loading),
                    onClick      = { onExecute(selectedOp, amount, pin, reference) },
                    enabled      = canSubmit && !cargando,
                    cargando     = cargando
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun AtmOperationChip(
    label: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) color.copy(alpha = 0.18f) else HadesNavyDark
    val border = if (selected) color else HadesOnDark.copy(alpha = 0.12f)

    Surface(
        onClick   = onClick,
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        color     = bg,
        border    = androidx.compose.foundation.BorderStroke(1.5.dp, border),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier            = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = if (selected) color else HadesOnDark.copy(alpha = 0.4f),
                modifier           = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text          = label,
                fontSize      = 10.sp,
                fontWeight    = if (selected) FontWeight.Black else FontWeight.Normal,
                letterSpacing = 1.sp,
                color         = if (selected) color else HadesOnDark.copy(alpha = 0.4f)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "ATM — Depósito")
@Composable
fun AtmViewDepositPreview() {
    HadesCoinTheme {
        AtmViewContent(phoneNumber = "3001234567", cargando = false)
    }
}
