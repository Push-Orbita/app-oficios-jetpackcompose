# Figma Node Mapping — Oficios Formosa

> Mapeado de nodos Figma → pantallas → componentes Compose implementados

---

## Nodos analizados

| Nodo Figma | Pantalla / Sección | Hallazgos clave | Componentes Compose |
|---|---|---|---|
| `16:2` | Pantalla Login | Fondo `#00358B`. Casco de marca en Thumbnail `666:2` = `#006AF0` | `OficiosTextField`, `OficiosPasswordField`, `OficiosButton` |
| `16:245` | Pantalla Registro | Mismo sistema visual que Login + Checkbox términos | `OficiosTextField`, `OficiosCheckbox` |
| `25:95` | Home / Dashboard | BottomNav con border-top `#413764`, selected container `#413764` | `OficiosBottomNavigationBar`, `OficiosTopAppBar` |
| `16:572` | Search / Explorar | Campo de búsqueda con ícono mic + lupa | `OficiosSearchField` |
| `191:11` | Categorías de servicio | Grid de `CategoryCard` con ícono centrado | `CategoryCard` |
| `25:258` | Lista de trabajadores | Cards con avatar 64dp, nombre, rating estrellas, botones | `WorkerCard` |
| `198:697` | Detalle de pedido | Header status, dirección, cliente, horario, monto, acciones | `OrderCard` |
| `40:309` | Worker Card expandida | Avatar 64x64 radius 8dp, shield-check, estrellas ámbar | `WorkerCard` |
| `210:29` | Pedido compacto | Versión condensada de OrderCard | `OrderCard` |
| `255:101` | Agenda / Calendario | Días de semana + slots horarios con estados | `OficiosCalendarDay`, `OficiosScheduleSlot` |

---

## Tokens extraídos de Figma

### Colores identificados
```
Background:              #00358B  → OficiosColorBackground
Outline / card:          #004DA8  → OficiosColorOutlineFill
Texto secundario:        #7F9AC5  → OficiosColorSecondaryText
Acción (CTA medio):      #006DEF  → OficiosColorAction
CTA dominante:           #FFFFFF  → OficiosColorWhite, texto #00358B
Casco de marca:          #006AF0  → OficiosColorBrandMark (nodo 666:2)
Acento UI (no logo):     #F8AE1A  → OficiosColorAccent
Alerta:                  #BA6380 / #D197AB → OficiosColorAlert / OficiosColorAlertText
```

### Tipografías identificadas en Figma
```
DIN Pro Bold/Medium/Regular → UI principal (fallback: SansSerif)
Roboto Medium              → Status bar del sistema
Satoshi Medium             → Labels de navegación inferior (fallback: SansSerif)
```

### Dimensiones confirmadas
```
Input height:     48dp
Input radius:     8dp
Button radius:    8dp
Card radius:      16dp
Card padding:     16dp
Avatar (worker):  64×64dp radius 8dp
BottomNav height: 64dp
TopBar height:    56dp
```

---

## Estado de implementación

| Componente | Nodo Figma | Estado |
|---|---|---|
| `OficiosTheme` | General | ✅ Implementado |
| `OficiosButton (Primary)` | `16:2` | ✅ Implementado |
| `OficiosButton (Secondary/Ghost/etc.)` | `16:2`, `40:309` | ✅ Implementado |
| `OficiosTextField` | `16:2`, `16:245` | ✅ Implementado |
| `OficiosPasswordField` | `16:2`, `16:245` | ✅ Implementado |
| `OficiosSearchField` | `16:572` | ✅ Implementado |
| `OficiosDropdownField` | `16:245` | ✅ Implementado |
| `OficiosCheckbox` | `16:245` | ✅ Implementado |
| `OficiosCard` | General | ✅ Implementado |
| `WorkerCard` | `25:258`, `40:309` | ✅ Implementado |
| `OrderCard` | `198:697`, `210:29` | ✅ Implementado |
| `CategoryCard` | `191:11` | ✅ Implementado |
| `ProfileCompletionCard` | General | ✅ Implementado |
| `CertificateCard` | General | ✅ Implementado |
| `WorkGalleryCard` | General | ✅ Implementado |
| `BenefitCard` | General | ✅ Implementado |
| `OficiosStatusChip` | `198:697` | ✅ Implementado |
| `OficiosFilterChip` | `191:11` | ✅ Implementado |
| `OficiosBadge/NotificationBadge` | `25:95` | ✅ Implementado |
| `OficiosBottomNavigationBar` | `25:95` | ✅ Implementado |
| `OficiosTopAppBar/BackTopBar` | `25:95` | ✅ Implementado |
| `OficiosScaffold/Screen` | General | ✅ Implementado |
| `OficiosLoadingState` | General | ✅ Implementado |
| `OficiosEmptyState` | General | ✅ Implementado |
| `OficiosErrorState` | General | ✅ Implementado |
| `OficiosSuccessState` | General | ✅ Implementado |
| `OficiosCalendarDay` | `255:101` | ✅ Implementado |
| `OficiosScheduleSlot` | `255:101` | ✅ Implementado |

---

## Notas de divergencia

- **Avatar `WorkerCard`**: Figma muestra imagen real → implementado como `Box` placeholder. Integrar con Coil `AsyncImage` en el feature module.
- **WorkGalleryCard**: Figma muestra imagen de portafolio → implementado como placeholder. Integrar Coil.
- **Fuentes**: DIN Pro y Satoshi no están licenciadas en el proyecto → fallback `SansSerif` documentado en `DESIGN_SYSTEM_SPEC.md`.
- **OrderCard amount format**: Figma usa ARS → UI recibe `String` para máxima flexibilidad.
