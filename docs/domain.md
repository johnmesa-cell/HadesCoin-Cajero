# Capa de Dominio — HadesCoin Cajero

## Descripción general

La capa de dominio define los **contratos** (interfaces de repositorio) y
los **casos de uso** que ejecuta el cajero. No contiene ninguna referencia
a Firebase, Android o Compose — es puro Kotlin.

---

## Modelo: `ServiceItem`

Representa una categoría de servicio pagable.

```kotlin
data class ServiceItem(
    val id:       String,
    val icono:    ImageVector,
    val nombreRes: Int           // @StringRes
)
```

### Servicios disponibles

| ID | Ícono Material | String resource |
|---|---|---|
| `energia` | `ElectricBolt` | `R.string.payment_servicio_energia` |
| `agua` | `WaterDrop` | `R.string.payment_servicio_agua` |
| `gas` | `LocalFireDepartment` | `R.string.payment_servicio_gas` |
| `internet` | `Wifi` | `R.string.payment_servicio_internet` |
| `telefono` | `PhoneAndroid` | `R.string.payment_servicio_telefono` |
| `tv` | `Tv` | `R.string.payment_servicio_tv` |
| `gimnasio` | `FitnessCenter` | `R.string.payment_servicio_gimnasio` |
| `streaming` | `PlayCircle` | `R.string.payment_servicio_streaming` |
| `seguro` | `Shield` | `R.string.payment_servicio_seguro` |
| `matricula` | `School` | `R.string.payment_servicio_matricula` |

---

## Contrato del repositorio: `WalletRepository`

```kotlin
interface WalletRepository {
    suspend fun deposit(phoneNumber: String, amount: Double): Result<Unit>
    suspend fun payment(phoneNumber: String, amount: Double, reference: String, pin: String): Result<Unit>
    suspend fun processWithdrawal(phoneNumber: String, code: String, amount: Double): Result<Unit>
    suspend fun markWithdrawalFailed(phoneNumber: String)
}
```

Todas las operaciones retornan `Result<Unit>` para que el ViewModel
maneje éxito y fallo de forma uniforme con `.fold(onSuccess, onFailure)`.

---

## Casos de uso

### `AtmDepositUseCase`

```kotlin
class AtmDepositUseCase(private val repo: WalletRepository) {
    suspend operator fun invoke(phone: String, amount: Double) =
        repo.deposit(phone, amount)
}
```

Validaciones previas en el ViewModel: `amount > 0`.

---

### `AtmPaymentUseCase`

> ⚠️ Este caso de uso existe pero la operación de pago con PIN
> se ejecuta directamente via `WalletRepository.payment()` desde el ViewModel
> para poder incluir el PIN en la llamada.

---

### `ProcessWithdrawalUseCase`

```kotlin
class ProcessWithdrawalUseCase(private val repo: WalletRepository) {
    suspend operator fun invoke(phone: String, code: String, amount: Double) =
        repo.processWithdrawal(phone, code, amount)
}
```

Validaciones previas en el ViewModel:
- `phone` no vacío
- `code.length == 6`
- `amount > 0`
- Terminal no bloqueado por intentos fallidos
