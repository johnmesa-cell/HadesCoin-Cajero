# Paquete: ui

## Responsabilidad
Contiene el sistema de diseño visual de la aplicación. Define la paleta de colores, la tipografía y el tema global que le dan la estética cyberpunk oscura a HadesCoin. Es consumido por todas las pantallas de la capa `presentation`.

## Archivos

### Color.kt
- **Qué es:** Archivo de definición cromática global de la interfaz de usuario.
- **Qué hace:** Centraliza las constantes de color hexadecimales que componen la estética cyberpunk oscura del proyecto, configurando tonos clave como `HadesNavy`, `HadesOrange`, `HadesPurple` y destellos de `HadesCyan`.
- **Interactúa con:** `Theme.kt` para poblar el mapa de colores del sistema de diseño.

### Theme.kt
- **Qué es:** Componente estructural de Compose que define la configuración del tema visual de la aplicación basado en Material Design 3.
- **Qué hace:** Configura un esquema de colores únicamente oscuro (`HadesDarkColorScheme`), asignando roles semánticos específicos a la paleta (colores para superficies de tarjetas, botones principales de acción, bordes y fondos) y expone la función composable `HadesCoinTheme`.
- **Interactúa con:** `Color.kt` (del cual extrae los colores), `Type.kt` (para inyectar los estilos tipográficos) y todas las pantallas de la interfaz que requieran envolverse bajo este estilo visual.

### Type.kt
- **Qué es:** Archivo de configuración y estilos tipográficos del proyecto.
- **Qué hace:** Inicializa y expone los atributos del objeto `Typography` de Material 3, estableciendo las dimensiones base, espaciados y configuraciones de lectura estándar para el cuerpo de texto principal de la aplicación.
- **Interactúa con:** `Theme.kt` para acoplar las configuraciones de texto dentro del tema global.
