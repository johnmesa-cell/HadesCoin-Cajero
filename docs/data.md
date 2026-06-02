# Capa de Datos — HadesCoin Cajero

## Descripción general

La capa de datos tiene dos fuentes:

| Fuente | Clase | Responsabilidad |
|---|---|---|
| **Remote** | `FirebaseWalletDataSource` | Operaciones contra Firestore |
| **Local** | `BlockLocalDataSource` | Persistencia del bloqueo por intentos fallidos |

---

## FirebaseWalletDataSource

Conecta con la colección `wallets` de Firestore.
Cada documento representa la billetera de un usuario, identificado por su número de teléfono.

### Operaciones implementadas

| Método | Descripción |
|---|---|
| `deposit(phone, amount)` | Incrementa el campo `balance` del documento |
| `payment(phone, amount, reference, pin)` | Valida el PIN del documento, descuenta el balance y registra la transacción |
| `processWithdrawal(phone, code, amount)` | Valida el código temporal y descuenta el balance |
| `markWithdrawalFailed(phone)` | Incrementa el contador de intentos fallidos en Firestore |

### Esquema del documento `wallets/{phone}`

Ver [`database-schema.json`](database-schema.json) para el esquema completo.

Campos principales:

```
balance        : Number   — Saldo actual en HadesCoin
pin            : String   — PIN hasheado del usuario
withdrawCode   : String   — Código temporal activo (nullable)
codeExpiresAt  : Timestamp — Expiración del código temporal
failedAttempts : Number   — Contador de intentos fallidos de retiro
```

---

## BlockLocalDataSource

Persistencia local con `SharedPreferences` para mantener el estado de bloqueo
del terminal **entre reinicios de la app**.

```kotlin
fun getBlockedUntilMs(): Long          // timestamp de fin de bloqueo
fun saveBlockedUntilMs(until: Long)    // guarda el timestamp
fun getFailedAttempts(): Int           // intentos acumulados
fun saveFailedAttempts(count: Int)     // actualiza el contador
fun clear()                            // limpia bloqueo y contador
```

### Lógica de bloqueo

```
intento fallido → getFailedAttempts() + 1
  si intentos >= 3:
    → saveBlockedUntilMs(ahora + 3 minutos)
    → markWithdrawalFailed(phone) en Firebase
    → UI muestra banner de bloqueo con countdown
  else:
    → muestra "intento X/3"
```

---

## WalletRepositoryImpl

Implementa `WalletRepository` delegando en `FirebaseWalletDataSource`.
Actúa como capa de abstracción entre el dominio y Firebase,
permitiendo reemplazar la fuente de datos sin modificar los casos de uso.
