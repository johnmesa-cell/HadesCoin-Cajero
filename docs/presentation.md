# Paquete: presentation

## Responsabilidad
Este paquete contiene toda la capa de presentación de la aplicación. Combina la gestión del estado y las validaciones de la interfaz (ViewModels), la renderización declarativa de las pantallas (Views en Jetpack Compose), los componentes visuales reutilizables y el enrutamiento de la aplicación (Navegación).

## Archivos por funcionalidad

### 1. Autenticación (auth)

#### LoginViewModel.kt
- **Qué es:** ViewModel que gestiona el estado y la lógica del flujo de inicio de sesión.
- **Qué hace:** Valida que el teléfono tenga 10 dígitos y empiece por 3, y que el PIN posea exactamente 4 dígitos. Ejecuta el inicio de sesión y expone estados reactivos de carga, éxito o error mediante `LiveData`.
- **Interactúa con:** `LoginUseCase`, `ServiceLocator` y alimenta visualmente a `LoginView.kt`.

#### LoginView.kt
- **Qué es:** Pantalla de inicio de sesión construida en Jetpack Compose.
- **Qué hace:** Renderiza el formulario de acceso separando el contenido puro en `LoginContent` para facilitar la previsualización. Reacciona a los estados de carga y muestra diálogos emergentes si fallan las credenciales.
- **Interactúa con:** `LoginViewModel`, `AppNavigation` y el paquete de componentes.

#### RegisterViewModel.kt
- **Qué es:** ViewModel para el flujo de registro de nuevos usuarios.
- **Qué hace:** Valida todos los campos del formulario (nombres, longitud de documento, consistencia telefónica y PIN). Coordina la creación de la entidad `AppUser` y gestiona las respuestas de éxito o cuenta duplicada.
- **Interactúa con:** `RegisterUseCase`, `ServiceLocator` y alimenta a `RegisterView.kt`.

#### RegisterView.kt
- **Qué es:** Pantalla de registro de usuarios.
- **Qué hace:** Dibuja el formulario de alta y observa de forma reactiva el estado del ViewModel para desplegar animaciones de carga, bloqueos de pantalla o diálogos de éxito/error.
- **Interactúa con:** `RegisterViewModel`, `AppNavigation` y el paquete de componentes.

### 2. Tablero Principal (home)

#### HomeViewModel.kt
- **Qué es:** ViewModel del Dashboard principal de la billetera.
- **Qué hace:** Carga y almacena el balance del usuario y su historial de movimientos. Ordena las transacciones cronológicamente de forma descendente y maneja funciones de refresco y posibles fallos de red.
- **Interactúa con:** `GetWalletDataUseCase`, `ServiceLocator` y alimenta a `HomeView.kt`.

#### HomeView.kt
- **Qué es:** Vista principal del cuadro de mando.
- **Qué hace:** Ensambla las tarjetas de saldo interactivo, el resumen de ingresos/egresos y la lista filtrable de transacciones (`LazyColumn`). Implementa un menú flotante (`HadesSpeedDial`) y una hoja inferior (`UserPanelSheet`) para gestionar la sesión.
- **Interactúa con:** `HomeViewModel`, `AppNavigation`, `TextUtils` y componentes UI.

### 3. Transferencias (transfer)

#### TransferViewModel.kt
- **Qué es:** ViewModel encargado de gobernar las transferencias de dinero.
- **Qué hace:** Pre-carga el balance del emisor y valida estrictamente las reglas antes de enviar fondos (monto positivo, teléfono destino válido de 10 dígitos, PIN de 4 dígitos, prohibición de auto-transferencia). Actualiza el balance local tras un envío exitoso.
- **Interactúa con:** `TransferUseCase`, `GetWalletDataUseCase`, `ServiceLocator` y alimenta a `TransferView.kt`.

#### TransferView.kt
- **Qué es:** Pantalla del formulario de envíos de dinero.
- **Qué hace:** Permite ingresar los datos del destinatario y el monto a transferir. Muestra advertencias visuales en color naranja si el monto supera el saldo disponible y despliega una hoja de confirmación para validar el PIN antes de ejecutar la transacción.
- **Interactúa con:** `TransferViewModel`, `AppNavigation` y el paquete de componentes.

### 4. Navegación (navigation)

#### AppNavigation.kt
- **Qué es:** Enrutador central estático de la aplicación.
- **Qué hace:** Define el grafo de navegación utilizando `NavHost`. Establece `login` como el destino inicial y gestiona de forma segura la extracción y paso de parámetros (como el número de teléfono) hacia las rutas dinámicas de `home` y `transfer`.
- **Interactúa con:** `LoginView`, `RegisterView`, `HomeView` y `TransferView`.

### 5. Componentes UI (components)

#### Componentes atómicos y contenedores (HadesButton, HadesTextField, HadesBackground, HadesCardBox, etc.)
- **Qué es:** Biblioteca de elementos visuales reutilizables con estilo propio.
- **Qué hace:** Encapsulan elementos base de Jetpack Compose para inyectarles de forma centralizada el sistema de diseño del proyecto (fondos con gradientes, bordes iluminados, botones con estado de carga integrado y campos de texto personalizados).
- **Interactúa con:** Todas las pantallas del paquete `presentation`.

#### Componentes de datos y estados (HadesBalanceText, HadesSummaryRow, HadesFilterChipRow, HadesSpeedDial, AlertDialogs)
- **Qué es:** Componentes especializados en la interacción y retroalimentación visual de la billetera.
- **Qué hace:** Proveen diálogos de carga síncronos y asíncronos (`ShowLoadingAlertDialog`), animaciones de rotación en menús flotantes, ocultamiento dinámico de saldo e interfaces tabulares para la información financiera.
- **Interactúa con:** Principalmente con `HomeView` y `TransferView`.

### 6. Utilidades (utils)

#### TextUtils.kt
- **Qué es:** Archivo de utilidades para el formateo de datos.
- **Qué hace:** Provee funciones puras de apoyo visual: extrae iniciales de nombres para los avatares (`getInitials`), traduce identificadores de transacciones del servidor al español (`translateTransactionType`) y formatea fechas ISO a texto amigable (`formatTimestamp`).
- **Interactúa con:** `HomeView.kt` y `TransferView.kt`.