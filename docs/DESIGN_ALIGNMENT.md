# Alineación con el handoff de diseño v2 (Oficios Formosa)

Fuente: `design_handoff_oficios_formosa/` (README.md, design-tokens.css, prototipo HTML,
assets). Este documento registra qué se tocó en la primera tanda (rama
`feature/full-app/design-v2`) para acercar el proyecto ya avanzado al nuevo diseño, y
qué se dejó igual a propósito. Criterio aplicado en todo el documento (acordado con el
cliente): **coherencia de flujo por sobre fidelidad pixel-perfect** cuando las dos compiten.

## Tema / tokens (Fase 0)

| Elemento | Antes | Ahora | Nota |
|---|---|---|---|
| `core/designsystem/theme/Color.kt` | Paleta violeta/magenta oscura (`#231D38`, `#A32F56`, `#F4A426`) | Paleta navy/dorado/acción del handoff (`rgb(0,53,139)`, `rgb(0,109,239)`, `rgb(248,174,26)`, `rgb(0,77,168)`, `rgb(127,154,197)`) | Reescrito completo. Nombres viejos (`OficiosColorPrimary`, `OficiosColorTertiary`, etc.) quedan como `@Deprecated` alias hacia los nuevos, para no forzar un rename masivo de los ~30 archivos que los consumen en esta misma tanda — se migran de a poco cuando se toque cada archivo por otra razón. |
| `Theme.kt` (mapeo a `ColorScheme` M3) | — | `primary`=acción azul, `background`/`surface`=navy, `outline`=outline-fill, `tertiary`=dorado | Sin cambios de estructura, solo de valores — todos los componentes que ya consumían `MaterialTheme.colorScheme.*` heredaron la paleta nueva automáticamente (verificado con build completo). |
| `Type.kt` | `FontFamily.SansSerif`, pesos 700/400 ya correctos | Sin cambios | El handoff pide DIN Pro + Satoshi pero no vinieron archivos de fuente (solo PNGs en `assets/`). Se mantiene `FontFamily.Default` con la jerarquía de tamaños/pesos ya correcta. Si se consiguen los `.ttf/.otf` reales, van a `core/designsystem/src/main/res/font/` sin tocar la API de `OficiosTypography`. |
| `OficiosButton.kt` (variantes) | Sombra de color por variante, borde dorado inconsistente en Primary | Sombra `rgba(0,0,0,.15)` (negra, como pide el README) en todas las variantes con elevación; Primary sin borde de color | El resto de la lógica (Secondary = fondo blanco/texto navy, Outlined = borde `outline`) ya coincidía con el spec una vez cambiada la paleta — no requirió cambios estructurales. |
| Resto de componentes (`OficiosCard`, `OficiosCheckbox`, `OrderCard`, etc.) | — | Sin cambios de código | Ya consumían roles de `MaterialTheme.colorScheme` o alias deprecados en vez de hex hardcodeados — heredan la paleta nueva sin tocarlos. Confirmado con `./gradlew compileDebugKotlin` completo (0 errores, solo warnings de deprecación). |
| Modo claro (`LightColorScheme`) | Violeta clarito | Blanco/navy coherente con la nueva familia | El handoff define un solo modo ("Mode 1"); no hay spec de modo claro. Se mantiene la variante clara solo para no romper el toggle System/Light/Dark que ya existe en `feature:settings`. |

## Pantallas auditadas (Fase 1)

| Pantalla(s) | Resultado | Detalle |
|---|---|---|
| Splash | Sin cambios | Logo + wordmark centrado sobre fondo — ya coincide 1:1 con la spec, solo cambia de color por heredar el tema nuevo. |
| Login | Sin cambios de layout/copy | Email, contraseña, "Recordarme", "¿Olvidaste tu contraseña?", CTA "Ingresar", CTA secundario "Registrarse", bloque de ayuda con "Contactar" — todos los elementos del punto 2 del README ya estaban. Copy ya en español natural y coherente; no se forzó el texto literal del prototipo HTML porque el propio handoff aclara que ese HTML es referencia de estructura, no texto para copiar tal cual. |
| Registro de cuenta / Registro de oficio | Sin cambios | `RegisterTradeScreen` ya implementa selects encadenados categoría→rubro→especialización repetible ("+ Añadir otro" vía `auth_register_trade_other_specialization`), barrio con búsqueda, franjas horarias Desde/Hasta repetibles, métodos de pago repetibles (Efectivo/Transferencia/Mercado Pago) — coincide punto por punto con el punto 4 del README. |
| Onboarding (DNI, selfie, rubros, disponibilidad, medios de pago, barrio) | Sin cambios | Ya funcionalmente completo (`ProviderDniViewModel` con `onFrontImage`/`onBackImage`, `ProviderAvailabilityViewModel`, `ProviderPaymentMethodsViewModel`, etc.). No se tocó el flujo de guardado parcial existente — prioridad de coherencia de flujo sobre fidelidad visual, como pidió el cliente. |
| Home cliente / Categorías / Subcategoría | Sin cambios | Buscador, sección "Recientes", grid de categorías, listado de sub-rubros y bottom nav de 5 ítems (Inicio/Perfil/Favoritos/Ayuda/Ajustes) — coincide con los puntos 7-9 del README. |
| **Ficha completa de trabajador** (`WorkerProfileScreen`) | **Copy + layout ajustado** | Faltaban dos elementos explícitos del punto 11 del README: botón **"Calificar"** (ahora al lado de "Contratar" en la barra inferior) y link **"Denunciar Perfil"** (ahora debajo de las reseñas, en el color de alerta `rgb(186,99,128)` del handoff). Ambos quedan wireados a un mensaje explicativo ("vas a poder calificar cuando termines un pedido", "la denuncia va a estar disponible próximamente") en vez de navegar a una pantalla real, porque calificar de verdad requiere el módulo de Pedidos/Calificaciones (Fase 3, todavía no construido) — mismo patrón que ya usaba el stub de "Contratar" antes de esta tanda. |

## Bugs encontrados probando el flujo real en emulador (no solo el diseño)

Instalando el APK en un emulador y navegando el flujo real (con datos reales del backend,
no mocks) aparecieron dos bugs preexistentes, ninguno introducido por el cambio de tema:

1. **Crash real al abrir la ficha de un trabajador con dos especialidades del mismo rubro**:
   `WorkerSpecialtiesSection.kt` usaba `key = { it.rubroName }` en un `LazyRow` — si el backend
   devuelve dos especialidades con el mismo nombre de rubro (pasó con un trabajador real de
   prueba, "Peluquería canina" x2), Compose tira `IllegalArgumentException: Key ... was already
   used` y la app crashea. Arreglado usando `itemsIndexed` con `"$index-${rubroName}"` como key.
2. **Contenido inalcanzable en la ficha completa**: el `Column` de la pantalla de éxito no tenía
   `verticalScroll`. Para un trabajador con galería + certificados + reseñas, el contenido excedía
   el alto de pantalla y el link "Denunciar Perfil" (agregado en esta misma tanda) quedaba
   completamente fuera de vista y sin forma de scrollear hasta él. Arreglado agregando
   `.verticalScroll(rememberScrollState())`.

Ambos se verificaron en el emulador después del fix: la ficha de un trabajador con especialidades
duplicadas ya no crashea, y la de un trabajador con galería+certificados+reseñas ahora scrollea
hasta el final. Se re-corrieron los tests de `:feature:home` (verdes) y se recompiló todo el
proyecto sin errores.

## Fuera de alcance de esta tanda (ver roadmap completo en el plan)

No se tocó nada de: Home trabajador (no existe todavía), módulo de Pedidos (solo hay stub),
Agenda/Calendario de gestión, Mi perfil/Editar perfil, ni Maps — quedan para las fases 2-6,
cada una con su propio checkpoint de build + corrida en emulador antes de seguir.

---

## Fase 2 — Home trabajador + separación real de nav graph Cliente/Trabajador

**El bug de fondo**: `ClientHome` y `ProviderHome` (`domain/model/InitialDestination.kt`)
enrutaban al mismo `HomeDestination.graph`, así que cualquier trabajador que iniciaba sesión
veía la Home de **cliente** (buscador, "Trabajadores destacados") en vez de la suya. Se
confirmó en vivo en el emulador con una cuenta de trabajador real (Matías Federico Britez,
especialidad "Mascotas · Peluquería canina") antes y después del fix.

**Qué se construyó** (real, no mockeado, contra el backend real):
- `domain/model/ProviderHomeContent.kt` + `HomeRepository.getProviderHome()` +
  `GetProviderHomeUseCase` — nombre y especialidad principal desde `trabajador-oficio/me`,
  verificación e ítems faltantes desde `trabajador-oficio/me/estado-perfil` (reutilizando
  `OnboardingRepository.getProviderProfileStatus()`/`getProviderRequiredSteps()`, ya existentes).
  El % de completitud se deriva en incrementos de 25% a partir de 4 hitos booleanos reales
  (identidad verificada, perfil completo, configuración completa, operativo) porque el backend
  no expone un porcentaje numérico.
- `feature/home/providerhome/` — `ProviderHomeScreen`/`ViewModel`/`Route`, header con
  nombre+especialidad+badge verificado, sección "Beneficios" (Fase 2 nota abajo), tarjeta
  "Completá tu perfil" (real) y "Agenda" con mini-calendario navegable (real, mes actual +
  navegación) y "próximos turnos" vacío.
- Nav graph separado de verdad: `ProviderHomeDestination`/`providerHomeScreen()` nuevo,
  `NavigationExtensions.kt` ahora rutea `ProviderHome` → `ProviderHomeDestination.graph` en vez
  de compartir el graph de cliente.

**Decisiones/mismatches con el backend, documentados según lo pedido**:
- **Banner "AHORA"**: el componente (`ProviderActiveBookingBanner`) está construido pero no se
  renderiza — no hay fuente de datos real de "pedido en curso" hasta que el módulo de Pedidos
  (Fase 3, `solicitud-servicio`/`booking`) esté wireado. `ProviderHomeContent.hasActiveBooking`
  queda hardcodeado en `false`.
- **"Beneficios"**: el swagger real (`apioficios.pushsoftware.com.ar/api-json`) no tiene ningún
  tag de beneficios/descuentos. Se dejó una lista estática local de 2 ítems en
  `HomeRepositoryImpl.staticProviderBenefits()` hasta que el backend lo exponga.
- **"Próximos turnos"**: la agenda de gestión real (`agenda/slot`, `sesion`) es Fase 4 — el
  mini-calendario solo navega meses y marca "hoy"; la lista de turnos muestra el estado vacío.
  El link "Ver agenda completa" muestra "Próximamente" (mismo patrón que Favoritos/Ayuda).

**Verificado en emulador** (cuenta de trabajador real, no mocks): Home trabajador con datos
reales → "Completar perfil" navega al checklist real de onboarding y muestra el mismo ítem
faltante ("Foto de rostro") que la tarjeta de Home → navegación de mes del calendario (Julio→
Agosto 2026, grillas correctas) → tabs Perfil/Favoritos/Ayuda muestran "Próximamente" → Ajustes
navega y funciona igual que para cliente. Build completo + `./gradlew test` en verde.
