<div align="center">

<img src="docs/ic_hadescoin_logo.png" style="width:120px" alt="HadesCoin Logo"/>

# 🪙 HadesCoin

**Billetera digital móvil con estética cyberpunk**  
Desarrollada en Android con Jetpack Compose, Firebase y arquitectura Clean Architecture.

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)

</div>

---

## 📋 Descripción

HadesCoin es una aplicación de billetera digital para Android que permite a los usuarios registrarse, iniciar sesión y realizar transferencias de dinero entre cuentas. Cuenta con una interfaz de usuario con tema oscuro cyberpunk, construida completamente con Jetpack Compose, y usa Firebase Realtime Database como backend en tiempo real.

---

## ✨ Funcionalidades

- **Registro de usuario** — Creación de cuenta con nombre, documento, número de teléfono y PIN de 4 dígitos
- **Inicio de sesión** — Autenticación mediante teléfono + PIN
- **Saldo en tiempo real** — Visualización del balance disponible actualizado desde Firebase
- **Transferencias** — Envío de dinero a otros usuarios por número de teléfono con confirmación por PIN
- **Historial de movimientos** — Lista de transacciones con distinción entre ingresos y egresos
- **Resumen financiero** — Totales de ingresos y egresos calculados automáticamente
- **Perfil de usuario** — Visualización de datos personales (teléfono, documento, fecha de registro)

---

## 🏗️ Arquitectura

El proyecto sigue los principios de **Clean Architecture** dividido en tres capas principales:

```
com.example.hadescoin/
├── data/                   # Capa de datos
│   ├── datasource/         # Fuentes de datos (Firebase)
│   ├── model/              # Modelos de datos (WalletUser, WalletTransaction)
│   └── repository/         # Implementaciones de repositorios
├── domain/                 # Capa de dominio
│   ├── model/              # Entidades de negocio
│   ├── repository/         # Interfaces de repositorios
│   └── usecase/            # Casos de uso (Login, Register, Transfer, GetTransactions)
├── presentation/           # Capa de presentación
│   ├── auth/               # LoginViewModel, RegisterViewModel
│   ├── home/               # HomeViewModel
│   └── transfer/           # TransferViewModel
├── ui/                     # Capa de UI
│   ├── screens/            # Pantallas (LoginView, RegisterView, HomeView, TransferView)
│   └── theme/              # Tema de la app (Color.kt, Type.kt, Theme.kt)
├── di/                     # Inyección de dependencias (ServiceLocator)
└── MainActivity.kt         # Entry point + Navegación
```

### Principios aplicados

| Principio | Implementación |
|-----------|----------------|
| Clean Architecture | Separación en capas `data / domain / presentation / ui` |
| MVVM | `ViewModel` + `StateFlow` para manejo de estado |
| Inyección de dependencias | ServiceLocator manual |
| Repositorio | Interfaz en `domain`, implementación en `data` |
| Casos de uso | Un caso de uso por acción de negocio |

---

## 🛠️ Stack tecnológico

| Tecnología | Versión | Uso |
|------------|---------|-----|
| Kotlin | 2.0+ | Lenguaje principal |
| Jetpack Compose | BOM 2024 | UI declarativa |
| Firebase Realtime Database | Latest | Base de datos en tiempo real |
| ServiceLocator | — | Inyección de dependencias manual |
| Navigation Compose | 2.7+ | Navegación entre pantallas |
| Kotlin Coroutines | 1.8+ | Programación asíncrona |
| StateFlow | — | Manejo de estado reactivo |

---

## 🗄️ Estructura de la base de datos (Firebase)

La base de datos en Firebase Realtime Database está organizada en dos colecciones principales:

```json
{
  "users": {
    "{userId}": {
      "id": "string",
      "name": "string",
      "documentNumber": "string",
      "phone": "string",
      "pin": "string",
      "balance": 0.0,
      "createdAt": "timestamp"
    }
  },
  "transactions": {
    "{transactionId}": {
      "id": "string",
      "senderId": "string",
      "receiverId": "string",
      "amount": 0.0,
      "type": "TRANSFER | DEPOSIT | WITHDRAW | PAYMENT",
      "direction": "IN | OUT",
      "timestamp": "timestamp"
    }
  }
}
```

### Reglas de seguridad (Firebase Rules)

```json
{
  "rules": {
    ".read": true,
    ".write": true,
    "transactions": {
      ".indexOn": ["senderId", "receiverId", "timestamp"]
    },
    "users": {
      ".indexOn": ["phone"]
    }
  }
}
```

> ⚠️ Las reglas actuales son abiertas para propósitos de desarrollo. En producción se debe implementar autenticación con Firebase Auth.

---

## 🚀 Instalación y configuración

### Prerrequisitos

- Android Studio Hedgehog o superior
- JDK 17+
- Cuenta en [Firebase Console](https://console.firebase.google.com)

### Pasos

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/johnmesa-cell/HadesCoin.git
   cd HadesCoin
   ```

2. **Configurar Firebase**
   - Crear un proyecto en [Firebase Console](https://console.firebase.google.com)
   - Habilitar **Realtime Database**
   - Descargar el archivo `google-services.json`
   - Colocarlo en `app/google-services.json`

3. **Configurar reglas de Firebase**
   - En Firebase Console → Realtime Database → Reglas
   - Pegar las reglas de seguridad del apartado anterior

4. **Compilar y ejecutar**
   - Abrir el proyecto en Android Studio
   - Sincronizar con Gradle
   - Ejecutar en emulador o dispositivo físico (API 26+)

---

## 📁 Recursos

Los recursos de texto y color están centralizados en:

```
app/src/main/res/values/
├── strings.xml     # Todos los textos de la app organizados por pantalla
├── colors.xml      # Paleta de colores HadesCoin
└── themes.xml      # Tema base de la app (requisito del sistema Android)
```

---

## 📚 Documentación técnica

| Módulo | Descripción |
|--------|-------------|
| [data](docs/data.md) | Capa de datos y Firebase |
| [domain](docs/domain.md) | Entidades y casos de uso |
| [presentation](docs/presentation.md) | ViewModels y pantallas |
| [di](docs/di.md) | Inyección de dependencias |
| [ui](docs/ui.md) | Sistema de diseño y tema |

---

## 📱 Pantallas

| Pantalla | Descripción |
|----------|-------------|
| **Login** | Inicio de sesión con teléfono y PIN |
| **Register** | Registro de nuevo usuario |
| **Home** | Dashboard principal con saldo, resumen y movimientos |
| **Transfer** | Formulario de transferencia con confirmación |

---

## 👤 Autores

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/johnmesa-cell">
        <img src="https://github.com/johnmesa-cell.png" width="80" style="border-radius:50%"/><br/>
        <b>John Mesa</b>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/joserestrepo1-ctrl">
        <img src="https://github.com/joserestrepo1-ctrl.png" width="80" style="border-radius:50%"/><br/>
        <b>Juan José Restrepo</b>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/AndresYarce">
        <img src="https://github.com/AndresYarce.png" width="80" style="border-radius:50%"/><br/>
        <b>Andrés Felipe Yarce</b>
      </a>
    </td>
  </tr>
</table>

---

## 📄 Licencia

Este proyecto fue desarrollado con fines académicos.
