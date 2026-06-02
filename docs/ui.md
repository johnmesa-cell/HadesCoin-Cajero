# Componentes UI — HadesCoin Cajero

## Sistema de diseño

El cajero comparte la misma paleta y componentes base que la app principal HadesCoin.
Todos los componentes están en `presentation/components/`.

---

## Paleta de colores

| Token | Color | Uso |
|---|---|---|
| `HadesBlack` | `#0A0A0F` | Fondo base |
| `HadesNavyDark` | `#0F0F1A` | Tarjetas y contenedores |
| `HadesPurple` | `#7C3AED` | Color primario (pagos, títulos) |
| `HadesCyan` | `#06B6D4` | Éxito, depósito |
| `HadesOrange` | `#F97316` | Retiro con código, advertencias |
| `HadesOnDark` | `#E2E8F0` | Texto sobre fondos oscuros |

---

## Componentes reutilizables

### `HadesScreen`
Contenedor raíz de **todas las pantallas**. Combina el fondo con gradiente
(`HadesBackground`) con `safeDrawingPadding()` para respetar automáticamente
la barra de estado y la barra de navegación del dispositivo.

```kotlin
@Composable
fun HadesScreen(content: @Composable BoxScope.() -> Unit) {
    HadesBackground {
        Box(
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
            content  = content
        )
    }
}
```

### `HadesBackground`
Gradiente vertical `HadesBlack → HadesNavyDark → HadesBlack` sobre un `Box` de pantalla completa.
Usado internamente por `HadesScreen`.

### `HadesTextField`
Campo de texto con estilo oscuro. Parámetros relevantes:

| Parámetro | Tipo | Descripción |
|---|---|---|
| `value` | `String` | Valor actual |
| `onValueChange` | `(String) -> Unit` | Callback de cambio |
| `label` | `String` | Etiqueta flotante |
| `isPassword` | `Boolean` | Enmascara el texto con `PasswordVisualTransformation` |
| `keyboardType` | `KeyboardType` | Tipo de teclado |
| `enabled` | `Boolean` | Habilita/deshabilita el campo |

### `HadesButton`
Botón primario con soporte para estado de carga.

| Parámetro | Descripción |
|---|---|
| `text` | Texto normal |
| `textCargando` | Texto mientras `cargando == true` |
| `cargando` | Muestra spinner y deshabilita el botón |
| `enabled` | Control externo de habilitación |

### `HadesCardBox`
Contenedor tipo card con fondo `HadesNavyDark`, bordes redondeados y padding estándar.
Usado para agrupar formularios en las pantallas de depósito y retiro.

### `AlertDialogs`
- `ShowLoadingAlertDialog()` — spinner de carga no cancelable.
- `ShowMessageAlertDialog(title, text, onConfirmation)` — diálogo de error genérico.
