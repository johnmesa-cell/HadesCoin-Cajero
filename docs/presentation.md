# Capa de Presentación — HadesCoin Cajero

## Descripción general

La capa de presentación sigue el patrón **MVVM** (Model-View-ViewModel).
Toda la UI está construida con **Jetpack Compose** y el estado se expone mediante `LiveData`
observado con `observeAsState()`.

---

## Pantalla principal: `AtmView.kt`

Punto de entrada único para todas las operaciones del cajero.
Recibe un parámetro `AtmOperation` y delega el renderizado al composable correspondiente.

```kotlin
enum class AtmOperation { DEPOSIT, PAYMENT, WITHDRAW_CODE }
```

### Composables por operación

| Composable | Operación | Pasos |
|---|---|---|
| `AtmDepositContent` | Depósito | 1 — Teléfono + monto |
| `PaymentAtmContent` | Pago de servicio | 3 — Categoría → Datos → Autenticación |
| `WithdrawCodeAtmContent` | Retiro con código | 1 — Teléfono + código + monto |

Todos los composables usan `HadesScreen { }` como contenedor raíz, que aplica
`safeDrawingPadding()` automáticamente para respetar la barra de estado y la barra de navegación del dispositivo.

---

## Flujo de Pago de Servicios (`PaymentAtmContent`)

El composable maneja 3 estados internos con una variable `paso: Int`:

### Paso 1 — Selección de categoría
- `LazyVerticalGrid` de 2 columnas con los 10 servicios disponibles.
- Al tocar una tarjeta se guarda `servicioSeleccionado` y avanza a `paso = 2`.

### Paso 2 — Datos del pago
- Campo **referencia** (número de contrato, hasta 40 caracteres).
- Campo **monto** (decimal, hasta 12 caracteres).
- Botón «Continuar» deshabilitado si algún campo está vacío o el monto es ≤ 0.

### Paso 3 — Autenticación del usuario
- Muestra un resumen del pago (servicio, referencia, monto).
- Campo **teléfono** (10 dígitos).
- Campo **PIN** (4 dígitos, enmascarado con `isPassword = true`).
- Botón «Confirmar Pago» habilitado solo cuando `phone.length == 10 && pin.length == 4`.

---

## ViewModel: `AtmViewModel.kt`

### Estado expuesto

| LiveData | Tipo | Descripción |
|---|---|---|
| `cargando` | `Boolean` | Muestra el diálogo de carga |
| `exito` | `String?` | Mensaje de operación exitosa |
| `error` | `String?` | Mensaje de error |
| `bloqueadoHastaMs` | `Long` | Timestamp hasta el que el terminal está bloqueado |
| `servicios` | `List<ServiceItem>` | Lista inmutable de los 10 servicios disponibles |

### Funciones principales

```kotlin
fun executeDeposit(phoneNumber: String, amount: Double)
fun executePayment(phoneNumber: String, amount: Double, reference: String, pin: String)
fun executeWithdrawalCode(phoneNumber: String, code: String, amount: Double)
```

### Formateo de montos

Los mensajes de éxito usan `NumberFormat` con `Locale("es", "CO")` para mostrar
valores legibles: `$150.000`, `$1.200.000`, etc.

---

## Diálogos globales

Definidos en `AtmView` y comunes a todas las operaciones:

- **`ShowLoadingAlertDialog()`** — mientras `cargando == true`.
- **`AlertDialog` de éxito** — con ícono `CheckCircle` en cyan, cierra y navega atrás.
- **`ShowMessageAlertDialog` de error** — muestra el mensaje de Firebase o validación.
