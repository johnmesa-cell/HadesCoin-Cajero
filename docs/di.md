# Paquete: di

## Responsabilidad
Contiene el mecanismo de Inyección de Dependencias manual del proyecto mediante un **ServiceLocator**. Su rol es proveer las instancias necesarias (repositorios, casos de uso, fuentes de datos de Firebase) de forma centralizada, sin necesidad de librerías externas como Hilt o Dagger.

## Archivos

### ServiceLocator.kt
- **Qué es:** Objeto singleton que actúa como contenedor de dependencias.
- **Qué hace:** Instancia de forma lazy (perezosa) todas las dependencias de la aplicación: `FirebaseUserDataSource`, `FirebaseTransactionDataSource`, los repositorios y los casos de uso. Expone métodos `provide*` que son consumidos por los ViewModels.
- **Interactúa con:** Todas las capas: `data` (DataSources y Repositorios), `domain` (Casos de uso) y `presentation` (ViewModels).

## Diagrama de dependencias
ServiceLocator → Instancia y provee dependencias a presentation (ViewModels) desde data y domain
