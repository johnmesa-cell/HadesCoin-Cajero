package com.example.hadescoin.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ExitToApp
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hadescoin.R
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.presentation.components.HadesBackground
import com.example.hadescoin.presentation.components.ShowLoadingAlertDialog
import com.example.hadescoin.presentation.components.ShowMessageAlertDialog
import com.example.hadescoin.ui.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// Entry point (con NavController + ViewModel)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun HomeView(
    phoneNumber: String,
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val cargando by viewModel.cargando.observeAsState(false)
    val appUser  by viewModel.appUser.observeAsState()
    val error    by viewModel.error.observeAsState()

    var showError    by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    LaunchedEffect(phoneNumber) { viewModel.loadUserData(phoneNumber) }

    LaunchedEffect(error) {
        error?.let { mensajeError = it; showError = true }
    }

    HomeViewContent(
        appUser    = appUser,
        cargando   = cargando,
        onDeposit  = { navController.navigate("atm/$phoneNumber/DEPOSIT") },
        onWithdraw = { navController.navigate("atm/$phoneNumber/WITHDRAW") },
        onPayment  = { navController.navigate("atm/$phoneNumber/PAYMENT") },
        onLogout   = {
            navController.navigate("login") { popUpTo(0) { inclusive = true } }
        }
    )

    if (cargando) ShowLoadingAlertDialog()

    if (showError) {
        ShowMessageAlertDialog(
            onConfirmation = { viewModel.clearError(); showError = false },
            dialogTitle    = stringResource(R.string.dialog_error_title),
            dialogText     = mensajeError
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Contenido puro (previsualización + test)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun HomeViewContent(
    appUser:    AppUser?,
    cargando:   Boolean,
    onDeposit:  () -> Unit = {},
    onWithdraw: () -> Unit = {},
    onPayment:  () -> Unit = {},
    onLogout:   () -> Unit = {}
) {
    HadesBackground {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(0.dp))

            // ── Bloque superior: marca + bienvenida ─────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.padding(top = 72.dp)
            ) {
                Text(
                    text          = stringResource(R.string.home_app_label),
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 5.sp,
                    color         = HadesPurple
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text          = stringResource(R.string.atm_title),
                    fontSize      = 30.sp,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color         = HadesOnDark,
                    textAlign     = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, HadesCyan, HadesOrange, Color.Transparent)
                            )
                        )
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text      = stringResource(
                        R.string.home_greeting,
                        appUser?.fullName ?: stringResource(R.string.home_no_data)
                    ),
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color      = HadesOnDark.copy(alpha = 0.7f),
                    textAlign  = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text          = stringResource(R.string.home_select_operation),
                    fontSize      = 12.sp,
                    color         = HadesOnDark.copy(alpha = 0.4f),
                    letterSpacing = 1.sp,
                    textAlign     = TextAlign.Center
                )
            }

            // ── Botones de operación ────────────────────────────────────────
            Column(
                modifier            = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AtmActionButton(
                    label    = stringResource(R.string.atm_deposit_title),
                    sublabel = stringResource(R.string.atm_deposit_desc),
                    icon     = Icons.Filled.ArrowDownward,
                    color    = HadesCyan,
                    onClick  = onDeposit
                )
                AtmActionButton(
                    label    = stringResource(R.string.atm_withdraw_title),
                    sublabel = stringResource(R.string.atm_withdraw_desc),
                    icon     = Icons.Filled.ArrowUpward,
                    color    = HadesOrange,
                    onClick  = onWithdraw
                )
                AtmActionButton(
                    label    = stringResource(R.string.atm_payment_title),
                    sublabel = stringResource(R.string.atm_payment_desc),
                    icon     = Icons.Filled.CreditCard,
                    color    = HadesPurple,
                    onClick  = onPayment
                )
            }

            // ── Pie: finalizar sesión ───────────────────────────────────────
            TextButton(
                onClick  = onLogout,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Icon(
                    imageVector        = Icons.Filled.ExitToApp,
                    contentDescription = null,
                    tint               = HadesOnDark.copy(alpha = 0.35f),
                    modifier           = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text          = stringResource(R.string.btn_logout),
                    fontSize      = 12.sp,
                    letterSpacing = 1.sp,
                    color         = HadesOnDark.copy(alpha = 0.35f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Botón de acción del cajero
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AtmActionButton(
    label:    String,
    sublabel: String,
    icon:     ImageVector,
    color:    Color,
    onClick:  () -> Unit
) {
    Surface(
        onClick        = onClick,
        modifier       = Modifier.fillMaxWidth(),
        shape          = RoundedCornerShape(18.dp),
        color          = HadesNavyDark,
        border         = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = label,
                    tint               = color,
                    modifier           = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text          = label,
                    fontSize      = 15.sp,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color         = color
                )
                Text(
                    text     = sublabel,
                    fontSize = 11.sp,
                    color    = HadesOnDark.copy(alpha = 0.45f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews  —  mismo patrón que HadesCoin original
// ─────────────────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true, name = "Home Cajero — con usuario")
@Composable
fun HomeViewPreview() {
    HadesCoinTheme {
        HomeViewContent(
            appUser  = AppUser(fullName = "Juan Pérez", phoneNumber = "3001234567"),
            cargando = false
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Home Cajero — sin usuario")
@Composable
fun HomeViewEmptyPreview() {
    HadesCoinTheme {
        HomeViewContent(
            appUser  = null,
            cargando = false
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Home Cajero — cargando")
@Composable
fun HomeViewLoadingPreview() {
    HadesCoinTheme {
        HomeViewContent(
            appUser  = null,
            cargando = true
        )
    }
}
