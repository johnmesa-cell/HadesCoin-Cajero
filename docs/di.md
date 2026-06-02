# Inyección de Dependencias — HadesCoin Cajero

## Patrón utilizado: ServiceLocator manual

El cajero usa un `ServiceLocator` singleton en lugar de Hilt o Koin,
manteniendo el proyecto liviano y sin procesadores de anotaciones.

---

## ServiceLocator.kt

```kotlin
object ServiceLocator {
    fun provideWalletRepository(): WalletRepository
    fun provideAtmDepositUseCase(): AtmDepositUseCase
    fun provideAtmPaymentUseCase(): AtmPaymentUseCase
    fun provideProcessWithdrawalUseCase(): ProcessWithdrawalUseCase
    fun provideBlockLocalDataSource(): BlockLocalDataSource
}
```

## Grafo de dependencias

```
AtmViewModel
  ├── AtmDepositUseCase
  │     └── WalletRepository
  │           └── FirebaseWalletDataSource
  ├── AtmPaymentUseCase
  │     └── WalletRepository
  ├── ProcessWithdrawalUseCase
  │     └── WalletRepository
  ├── BlockLocalDataSource   (SharedPreferences)
  └── WalletRepository      (acceso directo para payment con PIN)
```

---

## Instanciación en el ViewModel

```kotlin
class AtmViewModel(
    private val depositUseCase:    AtmDepositUseCase        = ServiceLocator.provideAtmDepositUseCase(),
    private val processWithdrawal: ProcessWithdrawalUseCase = ServiceLocator.provideProcessWithdrawalUseCase(),
    private val blockDataSource:   BlockLocalDataSource     = ServiceLocator.provideBlockLocalDataSource(),
    private val repository:        WalletRepository         = ServiceLocator.provideWalletRepository()
) : ViewModel()
```

Los parámetros tienen valores por defecto, lo que permite inyectar mocks
en tests sin modificar la clase.
