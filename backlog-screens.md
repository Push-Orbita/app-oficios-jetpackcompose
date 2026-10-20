# Pantallas del backlog: detalle de `get_design_context`

Todas las llamadas se hicieron con `skillNames: figma-design-to-code`. La respuesta completa está en `ctx/<node>.txt` (React+Tailwind de referencia) y el esquema compacto en `ctx/<node>.outline`.
Los assets referenciados en `ctx/*.txt` expiran en 7 días: hay que descargarlos (curl) antes de implementar.
**Item 2d no se incluye:** queda descartado por ahora porque está resuelto en la API nueva.

## Item 5: buscador de trabajadores (rediseño)
El flujo se lee en el diagrama de Búsqueda 451:1145 y en la Reserva con Agenda 613:33. Es de 4 niveles:
`Home cliente 25:95` → `Oficios (categorías) 25:258` → `Construcción (rubros) 35:2` → `Albañiles de Oficios Formosa (resultados) 40:309` → `Ficha Completa 40:559`.

- **Home cliente 25:95 / 613:40 / 454:467**
  - Contenedor `pt16 px32 pb32 gap16`.
  - Header: avatar 48 r8, nombre DIN Bold 16 + `badge-check` 24, campana 48 `#004DA8` r8.
  - Buscador: borde 1 px `#004DA8`, r8, `pl16 pr24 py12`, placeholder 50 % blanco, iconos `mic` y `search` 24 con gap 16.
    - Placeholder en Rediseño: "¿En que pdoemos ayudarte?" (typo en Figma).
    - Placeholder en Flujos: "¿Qué oficio estás buscando?". **Usar el de Flujos.**
  - "Recientes" DIN Bold 36 con 3 cards cuadradas (r16, borde, p16, ilustración del rubro + nombre). Son **rubros recientes**, no búsquedas en texto.
  - Card de ayuda (r16, borde): texto + botón blanco "Contactar" + badge "?" en la esquina superior.
  - Bottom nav de 5 ítems.
  - **No hay** "Mis pedidos" ni "Destacados".
  - Variante 366:1030 / 356:104: la grilla de categorías se muestra directo en la home, sin el título "Oficios". Así se ve la home al completar el registro.
- **Oficios 25:258**
  - Título "‹ Oficios" 36 con `chevron-left` 24 y gap 10, campana a la derecha.
  - Buscador "¿Qué oficio estás buscando?".
  - Grilla de 2 columnas con cards cuadradas r16 (label arriba + ilustración ~100 px): Construcción, Mantenimiento y refacción, Seguridad en Casa, Serv. Personales, Bienestar, **Ver todos**.
- **Construcción 35:2**: misma plantilla. Grilla 2 col de rubros con ilustración (Plomero, Albañil, Cerrajero, Instalador, Pintor, Carpintero, Gasista, Electricista, Herrero, Instalador de energía renovable, Construcción en seco, Capataz, Arquitecto, Ingeniero).
- **Resultados 40:309**
  - Header: logotipo 61×101 + "Albañiles de / Oficios Formosa" DIN Bold 36 (plural del rubro + "de Oficios Formosa").
  - Aviso 50 % blanco: "No puedes contactar a más de 3 trabajadores en un lapso de 10 minutos*".
  - Buscador: "¿Buscas algún albañil en específico?" (filtra por nombre **dentro** del rubro).
  - Card del trabajador:
    - r16, borde, p16, gap16;
    - avatar 64 r8 + nombre Bold 16 + `shield-check` 24;
    - fila de rating con número + 5 `star` 24 (half o vacía), o "Sin Calificación";
    - especialidad en `#7F9AC5`;
    - botones "Contactar" (azul `#006DEF`) y "Ver ficha completa" (blanco), con justify-between.
  - **Sin chips ni filtros.**
- **Ficha 40:559 / 453:343 / 613:460**
  - "Ficha Completa" 36, avatar 64, 5 estrellas, `heart` 24 a la derecha, bio, "Oficios:" y "Especializado en:" (Bold + `#7F9AC5`).
  - Botones Contactar/Calificar (Rediseño) o **"Solicitar Servicios"** (Flujos 453:343, la versión vigente).
  - "Trabajos realizados" 32 con galería horizontal; Certificados; reseñas con fecha.

**Impacto en la app:**
- `CategoriesScreen` y `RubrosScreen` pasan a grilla con ilustraciones.
- `SearchResultsScreen` lleva header + aviso + buscador en el rubro; los filtros quedan en un sheet opcional (app-only, a decidir).
- `WorkerCard` muestra 5 estrellas y Contactar en Primary.
- `HomeScreen` saca ActionCard y Destacados y suma Recientes (rubros) y la card de ayuda.
- API: la búsqueda por categoría no existe (BK-12 `categoriaId`). Hoy se navega categoría → rubros (`GET /rubro?categoriaId`, ya usado) → `search?rubroId`. El límite de contacto (3 en 10 min) es **regla de backend no expuesta**: sin endpoint ni código de error documentado → **gap nuevo BK-14**.

## Item 1a/1b/1c: servicios que ofrecés y agenda/horarios por día
- **Calendario general 239:475 / 526:494 / 366:1553 / 376:2044 / 526:1169**
  - Contenedores r16 con borde y p10.
  - "Días de Atención": 7 celdas de 32, seleccionada `#004DA8` r7 Bold.
  - "División de Horarios": segmento 30 minutos / **1 hora** / 2 horas + botón `clock` (personalizado).
  - "Horario de Atención": grilla de 4 columnas con las 24 horas (00:00…23:00), h32 r7, seleccionada `#004DA8` Bold.
  - Nota: "Podés marcar hasta 12 horas en un mismo día".
  - Texto: "El calendario se aplica para todos los oficios que agregues…".
  - "Calendario Por Oficio" 36: filas con checkbox + oficio + subtítulo ("Calendario General" o "Lu, Ma – 09:00 a 13:00") + `chevron-right`.
  - CTA "Guardar"/"Omitir por ahora" según el contexto.
  - **Implica un horario por día** (1c). El chip "Días de Atención" funciona como selector del día que se edita, más un override por oficio.
- **Diagrama Configuración de Agenda 526:13**: Abrir config → Seleccionar oficios (si ninguno: error) → Definir disponibilidad por oficio (días/horarios, zona, modalidad) → Configurar intervalo de turnos (si no es válido: error) → ¿Tiene local de atención? → ¿Activar auto-agenda? (sí: guardar auto-agenda, asignar el próximo turno disponible) → Guardar disponibilidad e intervalo por oficio → ¿Disponibilidad válida y completa? (no: errores de solapes/datos) → Publicar agenda por oficios.
- **Editar Perfil 231:15 / 224:1077 / 230:20**
  - Foto (Eliminar/Inspeccionar), Descripción, Oficios, "Especializado en", nota "Solo puede…", botón **"Editar Rubro o Especialidad"** (`pencil`).
  - "Trabajos realizados" con "+ Añadir".
  - "Certificado(s)" con estado Validado (`circle-check`) / Pendiente (`circle-dashed`).
  - "Darme de baja".
- **Elegí Rubro 526:903/986** (alta de servicio): grilla de categorías multiselección + "Continuar".
- **Agenda trabajador 259:534 / 526:667 y Agenda completa 210:106**
  - Calendario mensual: hoy en círculo blanco, días con turnos en `#004DA8`, seleccionado `#006DEF`.
  - Lista agrupada por fecha ("4 de marzo"…), cards con barra lateral (`#006DEF` = hoy) y "ver agenda completa".
  - En Agenda completa, checkboxes por servicio y "ajustar calendario".
- **API (prod actual):**
  - Editar días por servicio (BK-02), editar sesión (BK-03) y bloqueos (BK-04) son gaps.
  - "División de horarios" se mapea a `sesion.duracionMinutos` (POST/DELETE, sin PATCH).
  - El horario por día se puede mandar en el alta (`diasLaborales[]` con `diaInicio = diaFin`), pero no se puede editar después (BK-02).
  - Auto-agenda = `autoAceptacion` en `PATCH /trabajador-oficio-rubro/{id}` ✅.

## Item 2a: reserva según disponibilidad
- **Agendar Cita 613:1373**: card del trabajador (avatar + 5 estrellas), "Fecha" con calendario (días no disponibles en `#918E9B`), "Horario" con grilla de 4 col de horas del día (h32 r7; las ocupadas apagadas) y CTA.
- **Completá tu Solicitud 455:27 / 460:1017**
  - "Modalidad": segmentado **Presupuesto | Trabajo** (`receipt-text` / `hammer`).
  - Descripción.
  - "Ubicación": lista de domicilios con radio y `star` de favorita + "Añadir nueva ubicación" `circle-plus`.
  - "Fecha y hora": calendario + grilla de horas.
  - "Continuar".
- **Solicitar Servicio 468:1657**: variante "Este trabajador requiere un presupuesto antes de realizar un trabajo." con `circle-alert` y `x`. Las horas deshabilitadas en `#231D38`.
- **Solicitud 455:423**: resumen con mapa 248 + Detalle/Dirección/Tiempo de visita + "Realizar cambios" / "Enviar Solicitud" → Home con el toast "Solicitud enviada".
- **Diagrama Reserva 613:33**: Contratar (desde búsqueda/perfil) → Seleccionar oficio → ¿Auto-agenda activa (local + slots)?
  - **Sí:** asignar el siguiente turno según agenda e intervalo → ¿Hay turno? → Mostrar turno asignado → Confirmar reserva automática (o "Elegir otra fecha") → Crear turno (estado Confirmado) → Programar recordatorios.
  - **No:** ver disponibilidad y elegir fecha/hora → ¿Slot disponible en la agenda? (sí: confirmar el turno propuesto; no: ver alternativas) → Proponer fecha/hora (oferta del cliente) → Crear turno (estado Solicitado) → Notificar al trabajador.
  - Después, el trabajador revisa: Aceptar / Rechazar con motivo / Contraoferta (proponer nueva fecha/hora). El cliente acepta la contraoferta (→ Confirmado + confirmación) o elige otra fecha/cancela (→ Cancelado). Si recibe un rechazo: ¿Cliente desea reintentar?
- **API:** `GET /agenda/slot/disponibles` cubre la reserva con turnos. Para servicios sin turnos falta la disponibilidad del día (BK-05). "Modalidad Presupuesto/Trabajo" **no existe** en prod: `CreateSolicitudServicioRequestDto` = {trabajadorOficioSubRubroId, domicilioId, contactoId, slotTurnoId, fechaPropuestaInicio/Fin, descripcion}, y no hay ningún flag "requiere presupuesto" → **gap BK-15**. Domicilio favorito ✅ (`PATCH /domicilios/{id}/favorito`). Los "recordatorios" no aparecen en la spec de prod (**gap BK-16**, baja prioridad).

## Item 2b/2c/4: detalle del pedido, cancelación y plazo
- **Pedido Confirmado 198:697 / 613:572**
  - "‹ Pedido Confirmado" 36.
  - Mapa 376×248 r16 con pin.
  - 5 filas con icon-box 40 (`receipt-text`, `map-pin-house`, `user`, `clock`, `dollar-sign`): label Bold 16 + valor `#7F9AC5`.
  - Chip "Efectivo" (`banknote`).
  - CTAs a la derecha: "Contacto" (azul) y "Estoy en camino" (blanco).
  - Sin bottom nav.
- **Pedido Pendiente 210:29 / 613:637**: mismo layout. CTAs: "Rechazar" (texto), "Reagendar" (azul), "Aceptar" (blanco). Monto "Presupuesto pendiente".
- **Reagendar 210:531 / 455:488 / 613:704**
  - Bottom sheet sobre un overlay oscuro: "Reagendar pedido" (`redo`), "Envía una nueva fecha y hora al cliente.", calendario, hora con `‹ 12 : 30 ›`, "Tiempo de visita actual" vs "Nuevo tiempo de visita propuesto" (`clock` / `clock-check`), "Cancelar" (blanco) y "Enviar Reagenda" (azul).
  - Estado enviado 215:769 / 613:906: banner "Nueva propuesta enviada: 26 de marzo a las 11:30" con "Aceptar", y los botones Rechazar/Aceptar Pedido deshabilitados.
- **Terminar Pedido 255:17…452 / 613:982…1249**
  - Trabajador: input "Ingresar código del cliente" (`asterisk`) + "Validar".
  - Cliente: fila "Código: Este código al trabajador de oficio" + `3456` + diálogo "El ingreso del código por parte del proveedor de servicio implica la terminación del trabajo contratado" con checkbox "No volver a mostrar" y "Aceptar".
  - Calificación: sheet "Puntuar al trabajador de oficio" con avatar + nombre, número + 5 estrellas grandes, "Enviar"/"Enviado" y comentario opcional.
- **Cancelar:** **Figma no dibuja ningún botón "Cancelar pedido"** ni la vista del plazo de cancelación. Para el item 4 ("cancelar como primario") se propone:
  - usar `OficiosButton` Danger (fill `#BA6380`) del mismo tamaño que los CTAs de Figma (px12 py8, r8), alineado con Contacto/Estoy en camino;
  - mostrar `fechaLimiteCancelacion` como una fila más con icon-box `clock`.
  - **Pedirle a diseño** el frame de cancelación (motivo + plazo).
- **API:** `PATCH /solicitud-servicio/{id}/cancelar` exige `motivoCancelacion`: hoy hay un bug porque el trabajador manda null (2b). El plazo es 12–168 en prod y el pedido es 1–24 (BK-06). Reagendar → contraoferta `PATCH …/contraofertar` ✅. "Estoy en camino" ✅ `POST /tracking-booking/booking/{bookingId}/marcar` (en_camino → EN_CAMINO, llegue → EN_CURSO). Código de finalización ✅ (ya implementado). Calificación: prod exige `calidad` + `puntualidad`, así que hacen falta 2 filas de estrellas **[adaptación]** (o BK-17).

## Item 2e: toasts y estados de éxito/error
- **Toast de éxito 475:65**: banda fija sobre la bottom nav (border-top 1 px `#004DA8`, p16): icon-box 40 `#004DA8` con `circle-check` + "Solicitud enviada:" Regular 16 + `x` para cerrar. **No flota** como un Snackbar M3.
- **Error de login 356:343 / 366:1865**: mensaje arriba del formulario con `circle-alert` ("Los datos de sesión no coinciden" / "Complete los datos para ingresar") y bordes de input `#A32F56`.
- **Validación de código 363:911 / 366:964**: "Código validado" con `circle-check` o "Código Inválido" con `circle-alert`, inline bajo las casillas.
- **App:** `OficiosSnackbar` (Success/Error/Info) existe, pero `showSuccess` no se usa. Hay que rediseñarlo como esta banda: icon-box + texto + cerrar, anclado arriba de la bottom bar.

## Item 2f: tiempo real
Figma no lo dibuja. Lo que sí muestra son los estados (Pendiente → "Nueva propuesta enviada" → Confirmado → Terminar). Se mantiene la propuesta de polling (PR-D) + push (PR-J).

## Item 3: imágenes y certificados
- Editar Perfil: foto real con nombre de archivo, Eliminar/Inspeccionar; galería; certificados con estado.
- Ficha: "Trabajos realizados" (Frame 954 40:783 = fotos de tableros eléctricos de ejemplo).
- Iconos de categoría: **ilustraciones propias** → assets locales por ahora (BK-10 para que vengan de la API).
