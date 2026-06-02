# 🏧 HadesCoin — Cajero ATM

> **Aplicación Android de punto de venta / cajero físico** para el ecosistema HadesCoin.
> Opera de forma complementaria a la app principal: el cajero **nunca almacena saldo propio**,
> se conecta directamente a Firebase para validar usuarios y ejecutar operaciones en tiempo real.

<p align="center">
  <img src="docs/ic_hadescoin_logo.png" width="120" alt="HadesCoin Logo"/>
</p>

---

## ¿Qué hace este cajero?

| Operación | Descripción | Autenticación requerida |
|---|---|---|
| **Depósito** | Acredita HadesCoin a una cuenta por número de teléfono | Teléfono |
| **Pago de servicio** | Descuenta saldo y registra el pago de un servicio (energía, agua, internet, etc.) | Teléfono + PIN |
| **Retiro con código** | Retira saldo usando un código temporal de 6 dígitos generado en la app principal | Teléfono + Código |

---

## Stack tecnológico

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Arquitectura:** MVVM + Clean Architecture (capas `data / domain / presentation`)
- **Backend:** Firebase Firestore + Firebase Authentication
- **Inyección de dependencias:** `ServiceLocator` manual (sin Hilt)
- **Navegación:** Jetpack Navigation Compose
- **Estado:** `LiveData` + `observeAsState`

---

## Estructura del proyecto

```
app/src/main/java/com/example/hadescoin/
├── data/
│   ├── datasource/
│   │   ├── local/          # BlockLocalDataSource (bloqueo por intentos fallidos)
│   │   └── remote/         # FirebaseWalletDataSource
│   └── repository/         # WalletRepositoryImpl
├── di/
│   └── ServiceLocator.kt   # Fábrica manual de dependencias
├── domain/
│   ├── model/
│   │   └── ServiceItem.kt  # Modelo de servicio (icono + nombre + id)
│   ├── repository/
│   │   └── WalletRepository.kt
│   └── usecase/
│       ├── AtmDepositUseCase.kt
│       ├── AtmPaymentUseCase.kt
│       └── ProcessWithdrawalUseCase.kt
└── presentation/
    ├── atm/
    │   ├── AtmView.kt          # UI: flujos de depósito, pago y retiro
    │   └── AtmViewModel.kt     # Lógica de negocio y manejo de estado
    └── components/             # Componentes reutilizables Compose
        ├── HadesBackground.kt
        ├── HadesScreen.kt      # Wrapper con safeDrawingPadding
        ├── HadesButton.kt
        ├── HadesCardBox.kt
        ├── HadesTextField.kt
        └── AlertDialogs.kt
```

---

## Flujo de Pago de Servicios

```
Paso 1 — Seleccionar categoría
        ↓
Paso 2 — Ingresar referencia y monto
        ↓
Paso 3 — Autenticación: Teléfono + PIN
        ↓
   Firebase valida el PIN
        ↓
  ✅ Pago registrado  /  ❌ Error
```

---

## Seguridad

- El **PIN** nunca se almacena en la app del cajero — se envía directamente a Firebase para validación.
- El retiro con código tiene un **límite de 3 intentos fallidos** antes de bloquear el terminal por 3 minutos.
- El código temporal de retiro **expira en 25 minutos** y es de un solo uso, generado desde la app principal.

---

## Requisitos para compilar

1. Android Studio Hedgehog o superior
2. JDK 17+
3. Archivo `google-services.json` del proyecto Firebase en `app/`
4. `minSdk 26` · `targetSdk 35` · `compileSdk 35`

```bash
# Clonar y abrir en Android Studio
git clone https://github.com/johnmesa-cell/HadesCoin-Cajero.git
```

> ⚠️ El archivo `google-services.json` **no está incluido** en el repositorio por seguridad.
> Solicítalo al administrador del proyecto Firebase.

---

## Documentación detallada

| Archivo | Contenido |
|---|---|
| [`docs/presentation.md`](docs/presentation.md) | Pantallas, ViewModels y flujos de navegación |
| [`docs/domain.md`](docs/domain.md) | Casos de uso, modelos y contratos del repositorio |
| [`docs/data.md`](docs/data.md) | Fuentes de datos, Firebase y datasource local |
| [`docs/di.md`](docs/di.md) | ServiceLocator y grafo de dependencias |
| [`docs/ui.md`](docs/ui.md) | Componentes Compose reutilizables y tema visual |
| [`docs/database-schema.json`](docs/database-schema.json) | Esquema de la colección Firestore `wallets` |

---

## Relación con HadesCoin (app principal)

Este cajero es un **cliente externo** del mismo backend Firebase.
No tiene pantallas de registro, login ni historial de transacciones propias.
Toda la lógica de saldo y usuarios vive en la app principal:
👉 [github.com/johnmesa-cell/HadesCoin](https://github.com/johnmesa-cell/HadesCoin)
