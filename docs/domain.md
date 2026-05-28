# Paquete: domain

## Responsabilidad
Es la capa central (el núcleo) de la Clean Architecture. Aquí residen las reglas de negocio de la billetera, las entidades puras y las abstracciones (interfaces) de los repositorios. Este paquete es totalmente independiente de Android, de Firebase o de cualquier otro framework externo.

## Archivos

### AppUser.kt
- **Qué es:** Entidad de negocio (modelo de datos puro).
- **Qué hace:** Define la estructura fundamental de un usuario en el sistema (ID, documento, teléfono, nombre, PIN, saldo y fecha de creación).
- **Interactúa con:** Las interfaces de los repositorios y los Casos de Uso.

### WalletTransaction.kt
- **Qué es:** Entidad de negocio.
- **Qué hace:** Modela los datos de una transferencia o movimiento financiero (emisor, receptor, monto, tipo y dirección).
- **Interactúa con:** La interfaz `WalletRepository` y los Casos de Uso transaccionales.

### AuthRepository.kt
- **Qué es:** Interfaz que define el contrato del repositorio de autenticación.
- **Qué hace:** Establece los métodos obligatorios para iniciar sesión (`login`) y registrar usuarios (`register`), sin importar qué base de datos se use por debajo.
- **Interactúa con:** La entidad `AppUser`. Es consumida por los Casos de Uso de autenticación e implementada en la capa `data`.

### WalletRepository.kt
- **Qué es:** Interfaz que define el contrato del repositorio principal de la billetera.
- **Qué hace:** Establece los métodos necesarios para obtener el resumen de la cuenta (`getWalletData`), buscar usuarios por teléfono y realizar transferencias de dinero (`transferFunds`).
- **Interactúa con:** `AppUser` y `WalletTransaction`. Es consumida por los Casos de Uso transaccionales e implementada en la capa `data`.

### RegisterUseCase.kt
- **Qué es:** Caso de uso responsable del registro de usuarios.
- **Qué hace:** Recibe los datos de un usuario nuevo a través de la entidad `AppUser` y orquesta la operación de guardado en el sistema delegándola al repositorio.
- **Interactúa con:** La entidad `AppUser` y la interfaz `AuthRepository`.

### GetWalletDataUseCase.kt
- **Qué es:** Caso de uso específico de lectura de datos.
- **Qué hace:** Ejecuta la acción de recuperar la información principal de la billetera del usuario y su historial de transacciones utilizando su número de teléfono.
- **Interactúa con:** La interfaz `WalletRepository`.

### LoginUseCase.kt
- **Qué es:** Caso de uso para el inicio de sesión.
- **Qué hace:** Valida las credenciales del usuario (teléfono y PIN) delegando la verificación al repositorio de autenticación.
- **Interactúa con:** La interfaz `AuthRepository`.

### TransferUseCase.kt
- **Qué es:** Caso de uso que maneja las transferencias de fondos.
- **Qué hace:** Ejecuta la lógica para enviar dinero entre usuarios, validando el monto a transferir y el PIN de seguridad.
- **Interactúa con:** La interfaz `WalletRepository`.