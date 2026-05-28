package com.example.hadescoin.presentation.auth.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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

@Composable
fun RegisterView(
    navController: NavController,
    viewModel: RegisterViewModel = viewModel()
) {
    var fullName       by remember { mutableStateOf("") }
    var documentNumber by remember { mutableStateOf("") }
    var phoneNumber    by remember { mutableStateOf("") }
    var pin            by remember { mutableStateOf("") }
    var confirmPin     by remember { mutableStateOf("") }

    val cargando        by viewModel.cargando.observeAsState(false)
    val registroExitoso by viewModel.registroExitoso.observeAsState()
    val registroError   by viewModel.registroError.observeAsState()

    var mensajeError by remember { mutableStateOf("") }
    var showError    by remember { mutableStateOf(false) }

    LaunchedEffect(registroExitoso) {
        registroExitoso?.let { navController.popBackStack() }
    }

    LaunchedEffect(registroError) {
        registroError?.let {
            mensajeError = it
            showError = true
        }
    }

    RegisterViewContent(
        fullName               = fullName,
        documentNumber         = documentNumber,
        phoneNumber            = phoneNumber,
        pin                    = pin,
        confirmPin             = confirmPin,
        cargando               = cargando,
        onFullNameChange       = { if (it.all { c -> c.isLetter() || c.isWhitespace() }) { fullName = it; viewModel.clearError() } },
        onDocumentNumberChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) { documentNumber = it; viewModel.clearError() } },
        onPhoneChange          = { if (it.length <= 10 && it.all { c -> c.isDigit() } && (it.isEmpty() || it[0] == '3')) { phoneNumber = it; viewModel.clearError() } },
        onPinChange            = { if (it.length <= 4 && it.all { c -> c.isDigit() }) { pin = it; viewModel.clearError() } },
        onConfirmPinChange     = { if (it.length <= 4 && it.all { c -> c.isDigit() }) { confirmPin = it; viewModel.clearError() } },
        onRegisterClick        = { viewModel.register(fullName, documentNumber, phoneNumber, pin, confirmPin) },
        onBackToLoginClick     = { navController.popBackStack() }
    )

    if (cargando) ShowLoadingAlertDialog()

    if (showError) {
        ShowMessageAlertDialog(
            onConfirmation = { showError = false },
            dialogTitle    = stringResource(R.string.dialog_error_title),
            dialogText     = mensajeError
        )
    }
}

@Composable
fun RegisterViewContent(
    fullName: String,
    documentNumber: String,
    phoneNumber: String,
    pin: String,
    confirmPin: String,
    cargando: Boolean,
    onFullNameChange: (String) -> Unit,
    onDocumentNumberChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPinChange: (String) -> Unit,
    onConfirmPinChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onBackToLoginClick: () -> Unit
) {
    HadesBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter            = painterResource(id = R.drawable.ic_hadescoin_logo),
                contentDescription = stringResource(R.string.cd_logo),
                modifier           = Modifier.size(110.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text          = stringResource(R.string.login_title),
                fontSize      = 34.sp,
                fontWeight    = FontWeight.Black,
                letterSpacing = 6.sp,
                color         = HadesPurple,
                textAlign     = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, HadesCyan, HadesOrange, Color.Transparent)
                        )
                    )
            )
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text          = stringResource(R.string.register_subtitle),
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Medium,
                letterSpacing = 2.sp,
                color         = HadesCyan.copy(alpha = 0.7f),
                textAlign     = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            HadesCardBox {
                Text(
                    text          = stringResource(R.string.register_section_header),
                    fontSize      = 12.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color         = HadesCyan
                )

                HadesTextField(
                    value         = fullName,
                    onValueChange = onFullNameChange,
                    label         = stringResource(R.string.label_full_name)
                )

                HadesTextField(
                    value         = documentNumber,
                    onValueChange = onDocumentNumberChange,
                    label         = stringResource(R.string.label_document_number),
                    keyboardType  = KeyboardType.Number
                )

                HadesTextField(
                    value         = phoneNumber,
                    onValueChange = onPhoneChange,
                    label         = stringResource(R.string.label_phone_number_register),
                    keyboardType  = KeyboardType.Phone
                )

                HadesTextField(
                    value         = pin,
                    onValueChange = onPinChange,
                    label         = stringResource(R.string.label_pin),
                    isPassword    = true,
                    keyboardType  = KeyboardType.NumberPassword
                )

                HadesTextField(
                    value         = confirmPin,
                    onValueChange = onConfirmPinChange,
                    label         = stringResource(R.string.label_confirm_pin),
                    isPassword    = true,
                    keyboardType  = KeyboardType.NumberPassword,
                    isError       = confirmPin.isNotEmpty() && pin != confirmPin,
                    supportingText = {
                        if (confirmPin.isNotEmpty() && pin != confirmPin) {
                            Text(
                                text     = stringResource(R.string.error_pins_no_match),
                                color    = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                HadesButton(
                    text         = stringResource(R.string.btn_register),
                    textCargando = stringResource(R.string.btn_register_loading),
                    onClick      = onRegisterClick,
                    enabled      = pin.length == 4 && confirmPin.length == 4 && pin == confirmPin,
                    cargando     = cargando
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text     = stringResource(R.string.text_has_account),
                    fontSize = 13.sp,
                    color    = HadesOnDark.copy(alpha = 0.5f)
                )
                TextButton(
                    onClick        = onBackToLoginClick,
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        text          = stringResource(R.string.btn_login_link),
                        fontSize      = 13.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color         = HadesOrange
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Register — vacío")
@Composable
fun RegisterViewPreview() {
    HadesCoinTheme {
        RegisterViewContent(
            fullName = "", documentNumber = "", phoneNumber = "", pin = "", confirmPin = "",
            cargando = false,
            onFullNameChange = {}, onDocumentNumberChange = {}, onPhoneChange = {},
            onPinChange = {}, onConfirmPinChange = {}, onRegisterClick = {}, onBackToLoginClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Register — con datos")
@Composable
fun RegisterViewFilledPreview() {
    HadesCoinTheme {
        RegisterViewContent(
            fullName = "Juan Pérez", documentNumber = "1010101010",
            phoneNumber = "3001234567", pin = "1234", confirmPin = "1234",
            cargando = false,
            onFullNameChange = {}, onDocumentNumberChange = {}, onPhoneChange = {},
            onPinChange = {}, onConfirmPinChange = {}, onRegisterClick = {}, onBackToLoginClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Register — cargando")
@Composable
fun RegisterViewLoadingPreview() {
    HadesCoinTheme {
        RegisterViewContent(
            fullName = "Juan Pérez", documentNumber = "1010101010",
            phoneNumber = "3001234567", pin = "1234", confirmPin = "1234",
            cargando = true,
            onFullNameChange = {}, onDocumentNumberChange = {}, onPhoneChange = {},
            onPinChange = {}, onConfirmPinChange = {}, onRegisterClick = {}, onBackToLoginClick = {}
        )
    }
}
