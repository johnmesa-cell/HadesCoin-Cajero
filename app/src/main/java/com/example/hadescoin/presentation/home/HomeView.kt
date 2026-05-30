package com.example.hadescoin.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CreditCard
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
import com.example.hadescoin.presentation.atm.AtmViewModel
import com.example.hadescoin.presentation.components.HadesBackground
import com.example.hadescoin.presentation.components.ShowLoadingAlertDialog
import com.example.hadescoin.presentation.components.ShowMessageAlertDialog
import com.example.hadescoin.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun HomeView(
    navController: NavController,
    homeViewModel: HomeViewModel = viewModel(),
    atmViewModel:  AtmViewModel  = viewModel()
) {
    val cargando by homeViewModel.cargando.observeAsState(false)
    val error    by homeViewModel.error.observeAsState()

    // Bloqueo persistente
    val bloqueadoHastaMs by atmViewModel.bloqueadoHastaMs.observeAsState(0L)
    var segundosRestantes by remember { mutableStateOf(0) }

    LaunchedEffect(bloqueadoHastaMs) {
        while (System.currentTimeMillis() < bloqueadoHastaMs) {
            segundosRestantes = ((bloqueadoHastaMs - System.currentTimeMillis()) / 1000).toInt()
            delay(1000)
        }
        segundosRestantes = 0
    }

    val estaBloqueado = bloqueadoHastaMs > System.currentTimeMillis()

    var showError    by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    LaunchedEffect(error) {
        error?.let { mensajeError = it; showError = true }
    }

    HomeViewContent(
        cargando          = cargando,
        estaBloqueado     = estaBloqueado,
        segundosRestantes = segundosRestantes,
        onWithdrawCode    = { navController.navigate("atm/WITHDRAW_CODE") },
        onDeposit         = { navController.navigate("atm/DEPOSIT") },
        onPayment         = { navController.navigate("atm/PAYMENT") }
    )

    if (cargando) ShowLoadingAlertDialog()

    if (showError) {
        ShowMessageAlertDialog(
            onConfirmation = { homeViewModel.clearError(); showError = false },
            dialogTitle    = stringResource(R.string.dialog_error_title),
            dialogText     = mensajeError
        )
    }
}

@Composable
fun HomeViewContent(
    cargando:          Boolean,
    estaBloqueado:     Boolean = false,
    segundosRestantes: Int     = 0,
    onWithdrawCode:    () -> Unit = {},
    onDeposit:         () -> Unit = {},
    onPayment:         () -> Unit = {}
) {
    HadesBackground {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Bloque superior: marca ─────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
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
                
                if (estaBloqueado) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(HadesOrange.copy(alpha = 0.12f))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text       = "⛔ Retiro bloqueado — $segundosRestantes seg. restantes",
                            color      = HadesOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 13.sp,
                            textAlign  = TextAlign.Center,
                            modifier   = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Text(
                        text          = stringResource(R.string.home_select_operation),
                        fontSize      = 14.sp,
                        color         = HadesOnDark.copy(alpha = 0.6f),
                        letterSpacing = 1.sp,
                        textAlign     = TextAlign.Center,
                        fontWeight    = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ── Botones de operación ────────────────────────────────────────
            Column(
                modifier            = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AtmActionButton(
                    label    = "Retirar con Código",
                    sublabel = "Usa el código generado en tu app móvil",
                    icon     = Icons.Filled.ArrowUpward,
                    color    = HadesOrange,
                    onClick  = onWithdrawCode,
                    enabled  = !estaBloqueado
                )
                AtmActionButton(
                    label    = "Depositar Efectivo",
                    sublabel = "Ingresa billetes a tu cuenta",
                    icon     = Icons.Filled.ArrowDownward,
                    color    = HadesCyan,
                    onClick  = onDeposit
                )
                AtmActionButton(
                    label    = "Pagar Servicio",
                    sublabel = "Paga convenios con efectivo",
                    icon     = Icons.Filled.CreditCard,
                    color    = HadesPurple,
                    onClick  = onPayment
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun AtmActionButton(
    label:    String,
    sublabel: String,
    icon:     ImageVector,
    color:    Color,
    onClick:  () -> Unit,
    enabled:  Boolean = true
) {
    Surface(
        onClick        = onClick,
        enabled        = enabled,
        modifier       = Modifier.fillMaxWidth(),
        shape          = RoundedCornerShape(18.dp),
        color          = if (enabled) HadesNavyDark else HadesNavyDark.copy(alpha = 0.5f),
        border         = androidx.compose.foundation.BorderStroke(1.dp, if (enabled) color.copy(alpha = 0.25f) else color.copy(alpha = 0.1f)),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (enabled) color.copy(alpha = 0.12f) else color.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = label,
                    tint               = if (enabled) color else color.copy(alpha = 0.3f),
                    modifier           = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text          = label,
                    fontSize      = 16.sp,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color         = if (enabled) color else color.copy(alpha = 0.3f)
                )
                Text(
                    text     = sublabel,
                    fontSize = 12.sp,
                    color    = if (enabled) HadesOnDark.copy(alpha = 0.45f) else HadesOnDark.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Home Cajero")
@Composable
fun HomeViewPreview() {
    HadesCoinTheme {
        HomeViewContent(cargando = false)
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Home Cajero Bloqueado")
@Composable
fun HomeViewBlockedPreview() {
    HadesCoinTheme {
        HomeViewContent(cargando = false, estaBloqueado = true, segundosRestantes = 145)
    }
}
