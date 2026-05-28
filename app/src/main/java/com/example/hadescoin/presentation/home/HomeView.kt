package com.example.hadescoin.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.hadescoin.domain.model.WalletTransaction
import com.example.hadescoin.presentation.components.*
import com.example.hadescoin.presentation.utils.formatTimestamp
import com.example.hadescoin.presentation.utils.getInitials
import com.example.hadescoin.presentation.utils.translateTransactionType
import com.example.hadescoin.ui.theme.*
import java.util.Locale

@Composable
fun HomeView(
    phoneNumber: String,
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val cargando     by viewModel.cargando.observeAsState(false)
    val appUser      by viewModel.appUser.observeAsState()
    val transactions by viewModel.transactions.observeAsState(emptyList())
    val error        by viewModel.error.observeAsState()

    var showError    by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            if (destination.route?.startsWith("home") == true) menuExpanded = false
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

    LaunchedEffect(phoneNumber) {
        viewModel.loadWalletData(phoneNumber)
    }

    LaunchedEffect(error) {
        error?.let {
            mensajeError = it
            showError = true
        }
    }

    HomeViewContent(
        appUser        = appUser,
        transactions   = transactions,
        cargando       = cargando,
        menuExpanded   = menuExpanded,
        onMenuToggle   = { menuExpanded = !menuExpanded },
        onMenuCollapse = { menuExpanded = false },
        onRefresh      = { viewModel.refresh() },
        onLogout       = {
            navController.navigate("login") { popUpTo(0) { inclusive = true } }
        },
        onTransfer     = {
            menuExpanded = false
            navController.navigate("transfer/$phoneNumber")
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

@Composable
fun HomeViewContent(
    appUser: AppUser?,
    transactions: List<WalletTransaction>,
    cargando: Boolean,
    menuExpanded: Boolean = false,
    onMenuToggle: () -> Unit = {},
    onMenuCollapse: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onLogout: () -> Unit = {},
    onTransfer: () -> Unit = {}
) {
    var showUserPanel by remember { mutableStateOf(false) }
    var saldoVisible  by remember { mutableStateOf(true) }
    var filtroActivo  by remember { mutableStateOf("TODOS") }

    val transaccionesFiltradas = if (filtroActivo == "TODOS") transactions
        else transactions.filter { it.type.uppercase() == filtroActivo }

    val speedDialItems = listOf(
        SpeedDialItem(
            label   = stringResource(R.string.action_transfer),
            icon    = Icons.Filled.SwapHoriz,
            color   = HadesCyan,
            onClick = { onTransfer() }
        ),
        SpeedDialItem(
            label   = stringResource(R.string.action_deposit),
            icon    = Icons.Filled.ArrowDownward,
            color   = HadesCyan,
            onClick = {},
            enabled = false
        ),
        SpeedDialItem(
            label   = stringResource(R.string.action_withdraw),
            icon    = Icons.Filled.ArrowUpward,
            color   = HadesOrange,
            onClick = {},
            enabled = false
        ),
        SpeedDialItem(
            label   = stringResource(R.string.action_pay),
            icon    = Icons.Filled.CreditCard,
            color   = HadesPurple,
            onClick = {},
            enabled = false
        )
    )

    HadesBackground {
        Box(modifier = Modifier.fillMaxSize()) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                    HomeHeader(
                        appUser       = appUser,
                        onRefresh     = onRefresh,
                        onAvatarClick = { showUserPanel = true }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    BalanceCard(
                        appUser       = appUser,
                        saldoVisible  = saldoVisible,
                        onToggleSaldo = { saldoVisible = !saldoVisible }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item { Spacer(modifier = Modifier.height(28.dp)) }

                item {
                    HadesFilterChipRow(
                        opciones       = listOf("TODOS", "TRANSFER", "DEPOSIT", "WITHDRAW"),
                        seleccionado   = filtroActivo,
                        onSeleccion    = { filtroActivo = it },
                        labelTransform = { translateTransactionType(it) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    val totalIngresos = transactions.filter { it.direction == "IN" }.sumOf { it.amount }
                    val totalEgresos  = transactions.filter { it.direction == "OUT" }.sumOf { it.amount }
                    HadesSummaryRow(
                        items = listOf(
                            HadesSummaryItem(stringResource(R.string.label_incomes), totalIngresos, HadesCyan,   "+ "),
                            HadesSummaryItem(stringResource(R.string.label_expenses), totalEgresos,  HadesOrange, "- ")
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text          = stringResource(R.string.home_movements_header),
                            fontSize      = 12.sp,
                            fontWeight    = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color         = HadesCyan
                        )
                        Text(
                            text     = stringResource(R.string.label_records, transactions.size),
                            fontSize = 11.sp,
                            color    = HadesOnDark.copy(alpha = 0.4f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (transaccionesFiltradas.isEmpty() && !cargando) {
                    item {
                        Box(
                            modifier        = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text          = stringResource(R.string.home_empty_title),
                                    color         = HadesOnDark.copy(alpha = 0.3f),
                                    fontSize      = 13.sp,
                                    fontWeight    = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text      = stringResource(R.string.home_empty_subtitle),
                                    color     = HadesOnDark.copy(alpha = 0.25f),
                                    fontSize  = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                items(transaccionesFiltradas) { tx ->
                    TransactionRow(tx = tx)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item { Spacer(modifier = Modifier.height(120.dp)) }
            }

            if (menuExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(HadesNavyDark.copy(alpha = 0.6f))
                        .clickable { onMenuCollapse() }
                )
            }

            HadesSpeedDial(
                expanded = menuExpanded,
                onToggle = onMenuToggle,
                items    = speedDialItems,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }

        if (showUserPanel) {
            UserPanelSheet(
                appUser   = appUser,
                onDismiss = { showUserPanel = false },
                onLogout  = onLogout
            )
        }
    }
}

@Composable
private fun HomeHeader(
    appUser: AppUser?,
    onRefresh: () -> Unit,
    onAvatarClick: () -> Unit
) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(colors = listOf(HadesPurple, HadesNavyDark)))
                    .clickable { onAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = getInitials(appUser?.fullName),
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Black,
                    color      = HadesOnDark
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text          = stringResource(R.string.home_app_label),
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color         = HadesPurple
                )
                Text(
                    text       = stringResource(R.string.home_greeting, appUser?.fullName ?: stringResource(R.string.home_no_data)),
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color      = HadesOnDark.copy(alpha = 0.8f)
                )
            }
        }
        IconButton(onClick = onRefresh) {
            Icon(
                imageVector        = Icons.Filled.Refresh,
                contentDescription = stringResource(R.string.cd_refresh),
                tint               = HadesCyan,
                modifier           = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun BalanceCard(
    appUser: AppUser?,
    saldoVisible: Boolean,
    onToggleSaldo: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(colors = listOf(HadesPurple, HadesPurpleGlow, HadesNavyDark)))
            .padding(24.dp)
    ) {
        Column {
            Text(
                text          = stringResource(R.string.label_available_balance),
                fontSize      = 12.sp,
                letterSpacing = 1.sp,
                color         = HadesOnDark.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            HadesBalanceText(balance = appUser?.balance ?: 0.0, visible = saldoVisible, onToggle = onToggleSaldo)
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(HadesOnDark.copy(alpha = 0.15f)))
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = stringResource(R.string.label_phone),    fontSize = 9.sp,  letterSpacing = 1.sp, color = HadesOnDark.copy(alpha = 0.5f))
                    Text(text = appUser?.phoneNumber    ?: "—",           fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HadesOnDark)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = stringResource(R.string.label_document), fontSize = 9.sp,  letterSpacing = 1.sp, color = HadesOnDark.copy(alpha = 0.5f))
                    Text(text = appUser?.documentNumber ?: "—",           fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HadesOnDark)
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: WalletTransaction) {
    val isIncome    = tx.direction == "IN"
    val amountColor = if (isIncome) HadesCyan else HadesOrange
    val prefix      = if (isIncome) "+" else "-"
    val icon        = if (isIncome) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward
    val typeLabel   = translateTransactionType(tx.type)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(HadesNavyDark)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier         = Modifier.size(36.dp).clip(CircleShape).background(amountColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = typeLabel, tint = amountColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = typeLabel,                fontSize = 14.sp, fontWeight = FontWeight.Bold,  color = HadesOnDark)
                Text(text = formatTimestamp(tx.timestamp), fontSize = 11.sp, color = HadesOnDark.copy(alpha = 0.45f))
            }
        }
        Text(
            text       = "$prefix$ ${String.format(Locale.US, "%,.2f", tx.amount)}",
            fontWeight = FontWeight.Black,
            color      = amountColor,
            fontSize   = 15.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPanelSheet(appUser: AppUser?, onDismiss: () -> Unit, onLogout: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = HadesNavyDark) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier         = Modifier.size(72.dp).clip(CircleShape)
                    .background(Brush.radialGradient(colors = listOf(HadesPurple, HadesNavyDark))),
                contentAlignment = Alignment.Center
            ) {
                Text(text = getInitials(appUser?.fullName), fontSize = 26.sp, fontWeight = FontWeight.Black, color = HadesOnDark)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = appUser?.fullName   ?: "...", fontSize = 20.sp, fontWeight = FontWeight.Bold,  color = HadesOnDark)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = appUser?.phoneNumber ?: "—",  fontSize = 14.sp, color = HadesOnDark.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(HadesOnDark.copy(alpha = 0.1f)))
            Spacer(modifier = Modifier.height(20.dp))
            UserInfoRow(label = stringResource(R.string.label_document),     value = appUser?.documentNumber ?: "—")
            Spacer(modifier = Modifier.height(12.dp))
            UserInfoRow(label = stringResource(R.string.label_member_since), value = appUser?.createdAt?.take(10) ?: "—")
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick  = { onDismiss(); onLogout() },
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(containerColor = HadesOrange.copy(alpha = 0.15f), contentColor = HadesOrange),
                shape    = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.btn_logout), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun UserInfoRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 10.sp, letterSpacing = 1.sp, color = HadesOnDark.copy(alpha = 0.45f))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HadesOnDark)
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Home — vacío")
@Composable
fun HomeViewEmptyPreview() {
    HadesCoinTheme {
        HomeViewContent(
            appUser      = AppUser(fullName = "Juan Pérez", balance = 0.0, phoneNumber = "3001234567", documentNumber = "1010101010"),
            transactions = emptyList(),
            cargando     = false
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Home — con datos")
@Composable
fun HomeViewFilledPreview() {
    HadesCoinTheme {
        HomeViewContent(
            appUser = AppUser(fullName = "Juan Pérez", balance = 1250.50, phoneNumber = "3001234567", documentNumber = "1010101010"),
            transactions = listOf(
                WalletTransaction(type = "DEPOSIT",  amount = 500.0,  direction = "IN",  timestamp = "2026-05-21T10:00:00Z"),
                WalletTransaction(type = "WITHDRAW", amount = 50.25,  direction = "OUT", timestamp = "2026-05-20T08:00:00Z"),
                WalletTransaction(type = "TRANSFER", amount = 200.0,  direction = "OUT", timestamp = "2026-05-19T15:00:00Z"),
                WalletTransaction(type = "TRANSFER", amount = 150.0,  direction = "IN",  timestamp = "2026-05-18T11:00:00Z")
            ),
            cargando = false
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Home — cargando")
@Composable
fun HomeViewLoadingPreview() {
    HadesCoinTheme { HomeViewContent(appUser = null, transactions = emptyList(), cargando = true) }
}
