# Instrucciones para GitHub Copilot — HadesCoin

## Contexto del proyecto
HadesCoin es una billetera digital Android (similar a Nequi/Daviplata) desarrollada en **Android Studio** con **Kotlin nativo** y **Jetpack Compose**. El proyecto sigue **Clean Architecture + MVVM** con separación estricta de capas.

---

## Reglas generales — SIEMPRE respetar

- Lenguaje: **Kotlin** únicamente
- UI: **Jetpack Compose** (NO usar XML layouts)
- Base de datos: **Firebase Realtime Database** únicamente
- **NO usar** Firebase Authentication
- **NO usar** Hilt ni inyección de dependencias
- **NO usar** ViewModelFactory
- **NO usar UiState de ninguna forma** — ni clases `data class XxxUiState`, ni `sealed class`, ni `StateFlow<UiState>`. Está completamente prohibido.
- El estado se expone únicamente con **`LiveData`** / **`MutableLiveData`** — una variable por concepto (ej: `_loginExitoso`, `_loginError`, `_cargando`)
- **NO usar** `StateFlow`, `MutableStateFlow` ni `uiState` como propiedad del ViewModel
- **NO encriptar** contraseñas — el PIN se guarda en **texto plano**
- Usar `viewModelScope.launch` + `kotlinx.coroutines.tasks.await` para operaciones async

---

## ❌ Patrón PROHIBIDO — NO hacer esto

```kotlin
// ❌ MAL — UiState prohibido
data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState
}
```

---

## ✅ Patrón CORRECTO — LiveData separadas por concepto

```kotlin
// ✅ BIEN — LiveData individuales
class LoginViewModel : ViewModel() {
    private val _loginExitoso = MutableLiveData<String>()
    val loginExitoso: LiveData<String> = _loginExitoso

    private val _loginError = MutableLiveData<String>()
    val loginError: LiveData<String> = _loginError

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando
}
```

---

## Estructura de paquetes — Clean Architecture

```
com.example.hadescoin
├── data/
│   ├── datasource/          ← Acceso directo a Firebase (FirebaseDatabase.getInstance())
│   └── repository/          ← Implementación de las interfaces definidas en domain
├── domain/
│   ├── model/               ← Modelos de datos puros (AppUser, Transaction, etc.)
│   ├── repository/          ← Interfaces/contratos del repositorio
│   └── usecase/             ← Casos de uso (lógica de negocio)
├── presentation/
│   ├── components/          ← Composables reutilizables (botones, inputs, etc.)
│   ├── home/                ← HomeScreen.kt + HomeViewModel.kt
│   ├── login/               ← LoginScreen.kt + LoginViewModel.kt
│   ├── navigation/          ← NavGraph.kt y rutas de navegación
│   └── register/            ← RegisterScreen.kt + RegisterViewModel.kt
└── ui/                      ← Theme, colores, tipografía (Material3)
```

---

## Responsabilidad de cada capa

### `data/datasource/`
- Contiene clases que interactúan **directamente** con Firebase Realtime Database.
- Ejemplo: `UserRemoteDataSource.kt`
- Instancia `FirebaseDatabase.getInstance()` aquí, **NO en el ViewModel**.

### `data/repository/`
- Implementa las interfaces de `domain/repository/`.
- Llama al datasource y mapea datos a modelos de `domain/model/`.
- Ejemplo: `UserRepositoryImpl.kt`

### `domain/repository/`
- Solo **interfaces** (contratos). No contiene lógica ni Firebase.
- Ejemplo:
```kotlin
interface UserRepository {
    suspend fun getUserByPhone(phoneNumber: String): AppUser?
    suspend fun registerUser(user: AppUser): Boolean
}
```

### `domain/usecase/`
- Un archivo por caso de uso. Contiene la lógica de negocio.
- Recibe el Repository por constructor.
- Ejemplo: `LoginUseCase.kt`, `RegisterUseCase.kt`
```kotlin
class LoginUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(phoneNumber: String, pin: String): AppUser? {
        return userRepository.getUserByPhone(phoneNumber)
            ?.takeIf { it.pin == pin }
    }
}
```

### `presentation/`
- ViewModels instancian el UseCase directamente (sin Hilt).
- Exponen estado con `LiveData` individuales — **NUNCA con UiState**.
- Las Screens observan el ViewModel con `observeAsState()`.

---

## Modelo de datos — AppUser.kt (`domain/model/`)

```kotlin
data class AppUser(
    val id: String = "",
    val documentNumber: String = "",
    val phoneNumber: String = "",
    val fullName: String = "",
    val pin: String = "",         // texto plano, sin encriptar
    val balance: Double = 0.0,
    val createdAt: String = ""
)
```

---

## Estructura de Firebase Realtime Database

```json
{
  "users": {
    "user_prueba_001": {
      "documentNumber": "1010101010",
      "phoneNumber": "3001234567",
      "fullName": "Juan Pérez",
      "pin": "1234",
      "balance": 150000.0,
      "createdAt": "2026-04-24T00:00:00Z"
    },
    "user_prueba_002": {
      "documentNumber": "2020202020",
      "phoneNumber": "3119876543",
      "fullName": "María López",
      "pin": "5678",
      "balance": 75000.0,
      "createdAt": "2026-04-24T00:00:00Z"
    }
  },
  "transactions": {
    "tx_001": {
      "senderId": "user_prueba_001",
      "receiverId": "user_prueba_002",
      "amount": 50000.0,
      "type": "TRANSFER",
      "timestamp": "2026-04-24T10:00:00Z"
    }
  }
}
```

---

## Ejemplo completo de flujo Login

### `data/datasource/UserRemoteDataSource.kt`
```kotlin
class UserRemoteDataSource {
    private val database = FirebaseDatabase.getInstance()

    suspend fun getAllUsers(): List<AppUser> {
        val snapshot = database.getReference("users").get().await()
        return snapshot.children.mapNotNull { userSnapshot ->
            AppUser(
                id = userSnapshot.key ?: "",
                documentNumber = userSnapshot.child("documentNumber").getValue(String::class.java) ?: "",
                phoneNumber = userSnapshot.child("phoneNumber").getValue(String::class.java) ?: "",
                fullName = userSnapshot.child("fullName").getValue(String::class.java) ?: "",
                pin = userSnapshot.child("pin").getValue(String::class.java) ?: "",
                balance = userSnapshot.child("balance").getValue(Double::class.java) ?: 0.0,
                createdAt = userSnapshot.child("createdAt").getValue(String::class.java) ?: ""
            )
        }
    }
}
```

### `data/repository/UserRepositoryImpl.kt`
```kotlin
class UserRepositoryImpl(
    private val dataSource: UserRemoteDataSource
) : UserRepository {
    override suspend fun getUserByPhone(phoneNumber: String): AppUser? {
        return dataSource.getAllUsers().find { it.phoneNumber == phoneNumber }
    }
    override suspend fun registerUser(user: AppUser): Boolean {
        return true
    }
}
```

### `domain/usecase/LoginUseCase.kt`
```kotlin
class LoginUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(phoneNumber: String, pin: String): AppUser? {
        return userRepository.getUserByPhone(phoneNumber)
            ?.takeIf { it.pin == pin }
    }
}
```

### `presentation/login/LoginViewModel.kt`
```kotlin
class LoginViewModel : ViewModel() {

    private val dataSource = UserRemoteDataSource()
    private val repository = UserRepositoryImpl(dataSource)
    private val loginUseCase = LoginUseCase(repository)

    private val _loginExitoso = MutableLiveData<String>()
    val loginExitoso: LiveData<String> = _loginExitoso

    private val _loginError = MutableLiveData<String>()
    val loginError: LiveData<String> = _loginError

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    fun login(phoneNumber: String, pin: String) {
        if (phoneNumber.isBlank() || pin.isBlank()) {
            _loginError.value = "Completa todos los campos"
            return
        }
        viewModelScope.launch {
            _cargando.value = true
            try {
                val user = loginUseCase(phoneNumber, pin)
                if (user != null) {
                    _loginExitoso.value = "¡Bienvenido, ${user.fullName}!"
                } else {
                    _loginError.value = "Teléfono o PIN incorrectos"
                }
            } catch (e: Exception) {
                _loginError.value = "Error de conexión: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }
}
```

---

## Cómo instanciar ViewModels en las Screens

```kotlin
// SIN factory, SIN Hilt — así de simple
val viewModel: LoginViewModel = viewModel()
val viewModel: RegisterViewModel = viewModel()
```

---

## Snackbar — patrón para mensajes de feedback

```kotlin
val snackbarHostState = remember { SnackbarHostState() }
val loginExitoso by viewModel.loginExitoso.observeAsState()
val loginError by viewModel.loginError.observeAsState()

LaunchedEffect(loginExitoso) {
    loginExitoso?.let { snackbarHostState.showSnackbar(it) }
}
LaunchedEffect(loginError) {
    loginError?.let { snackbarHostState.showSnackbar(it) }
}

Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
    // contenido de la pantalla
}
```

---

## Archivos que NO deben existir — eliminar si están presentes

- `LoginUiState.kt`
- `RegisterUiState.kt`
- `LoginViewModelFactory.kt`
- `RegisterViewModelFactory.kt`
- Cualquier archivo con el sufijo `UiState.kt`
- Cualquier `sealed class` o `data class` que modele estado de UI

---

## Dependencias necesarias en app/build.gradle.kts

```kotlin
implementation(libs.firebase.database)                       // Realtime Database
implementation(libs.androidx.lifecycle.viewmodel.compose)    // viewModel()
implementation(libs.androidx.lifecycle.livedata.ktx)         // LiveData
implementation(libs.androidx.runtime.livedata)               // observeAsState()
implementation(libs.kotlinx.coroutines.play.services)        // .await() en coroutines
```
