# Paquete: data

## Responsabilidad
Este paquete conforma la capa de datos de la aplicación. Es responsable de implementar las interfaces definidas en el dominio, manejar las fuentes de datos (Firebase Realtime Database) y mapear los modelos de datos de la red a las entidades del dominio.

## Archivos

### FirebaseUserDataSource.kt
- **Qué es:** La fuente de datos remota para usuarios.
- **Qué hace:** Ejecuta las consultas y operaciones directas contra Firebase para registro, login y búsqueda de usuarios.
- **Interactúa con:** La base de datos de Firebase y los repositorios.

### FirebaseTransactionDataSource.kt
- **Qué es:** La fuente de datos remota para transacciones.
- **Qué hace:** Ejecuta las consultas contra Firebase para registrar y consultar transferencias de dinero.
- **Interactúa con:** La base de datos de Firebase y `WalletRepositoryImpl`.

### FirebaseAuthRepositoryImpl.kt
- **Qué es:** Implementación concreta del repositorio de autenticación.
- **Qué hace:** Orquesta las operaciones de login y registro delegando a `FirebaseUserDataSource`.
- **Interactúa con:** `FirebaseUserDataSource` y la interfaz `AuthRepository` del dominio.

### WalletRepositoryImpl.kt
- **Qué es:** Implementación concreta del repositorio principal de la billetera.
- **Qué hace:** Orquesta la obtención del saldo, historial de transacciones y transferencias de fondos.
- **Interactúa con:** `FirebaseUserDataSource`, `FirebaseTransactionDataSource` y la interfaz `WalletRepository` del dominio.

## Diagrama de dependencias
Firebase (BBDD) → data (DataSource → RepositoryImpl) → domain
