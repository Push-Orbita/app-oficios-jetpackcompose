# Fase 5 — Perfil y cuenta

Fecha: 27 de agosto de 2026

Rama: `codex/fase-5-perfil-cuenta`

## Resultado

La aplicación ya tiene un centro de cuenta real y diferenciado por rol. Cliente y Trabajador pueden editar sus datos sin volver a recorrer onboarding. El Trabajador además administra presentación profesional, múltiples servicios, modalidades, horarios, medios de cobro, galería y certificados.

La eliminación de cuenta queda visible y deshabilitada: el backend solo expone borrado genérico por ID y no existe aún un contrato seguro basado en la identidad autenticada.

## Implementación Android

- Nuevo `AccountRepository` con dominio, casos de uso, fuentes remotas y mappers.
- Hub de Ajustes según rol, con nombre, correo, foto, verificación y accesos relevantes.
- Edición de nombre, apellido, correo y foto de perfil.
- Persistencia cifrada actualizada para conservar rol, persona, foto y datos editados después de reiniciar la app.
- Cambio de contraseña autenticado con validaciones y errores comprensibles.
- Cliente:
  - listado de múltiples contactos y domicilios;
  - alta, edición, favorito y eliminación;
  - formato de WhatsApp explícito: Argentina `+54` + número local de 10/11 dígitos;
  - verificación por WhatsApp solo al crear o cambiar un número;
  - búsqueda real de barrios;
  - creación mediante `POST /domicilios/mi`, sin enviar un `personaId` manipulable.
- Trabajador:
  - edición de descripción, experiencia y medios de cobro;
  - listado de servicios publicados;
  - protección visual del servicio principal;
  - eliminación de servicios secundarios con confirmación;
  - constructor progresivo de servicios en cinco pasos: oficio, modalidad, horario, cobro y revisión;
  - múltiples especialidades, franjas horarias, días, modalidad a domicilio/local y dirección de local;
  - galería con carga y eliminación de fotos;
  - certificados con alta, edición, reemplazo y eliminación;
  - selector de certificados PDF/JPG/PNG/WEBP con límite local de 10 MB.

## Decisiones UX

- El perfil se administra desde Ajustes y no reutiliza pantallas de onboarding como si la cuenta siguiera incompleta.
- Formularios extensos se dividen en pasos breves con revisión final.
- El teléfono explica país, prefijo y ejemplo antes del error.
- Los contactos y domicilios permiten una opción principal para reducir decisiones durante contratación.
- Ningún certificado se presenta como validado: el backend aún no devuelve estado administrativo.
- El servicio principal no ofrece eliminación directa para evitar perfiles sin oferta activa y romper pedidos existentes.
- No se implementó “borrar y recrear” para editar un servicio: no es atómico y podría perder configuración. Quedó requerido al backend.
- La eliminación de cuenta no llama `DELETE /usuario/{id}` desde Android.

## Endpoints consumidos y verificados

- `GET /auth/me`
- `PATCH /auth/change-password`
- `PATCH /usuario/{id}`
- `GET /cliente/me`
- `GET /trabajador-oficio/me`
- `PATCH /trabajador-oficio/me`
- `GET /domicilios/mis`
- `POST /domicilios/mi`
- `PATCH /domicilios/{id}`
- `DELETE /domicilios/{id}`
- `PATCH /domicilios/{id}/favorito`
- `GET /contacto/mis`
- `POST /contacto`
- `PATCH /contacto/{id}`
- `DELETE /contacto/{id}`
- `PATCH /contacto/{id}/favorito`
- `POST /contacto/{id}/enviar-codigo-telefono`
- `POST /contacto/{id}/verificar-codigo-telefono`
- `POST /trabajador-oficio-rubro/full/secundario`
- `DELETE /trabajador-oficio-rubro/{id}`
- `POST /galeria-trabajo/subir-fotos`
- `DELETE /galeria-trabajo/{id}`
- `POST /certificado-trabajo`
- `PATCH /certificado-trabajo/{id}`
- `DELETE /certificado-trabajo/{id}`

## Diferencias respecto de Figma

- Se preservan identidad visual, jerarquía, tarjetas y tono del diseño, adaptados a Compose y tamaños dinámicos.
- El prototipo concentra alta profesional en un formulario largo; la app usa un constructor progresivo.
- Se agregaron estados de carga, error, vacío, guardado, confirmación y capacidad no disponible.
- “Certificado verificado” no se muestra porque Swagger no aporta esa evidencia.
- Eliminar cuenta se mantiene visible para que la arquitectura de información sea completa, pero deshabilitado con explicación.

## Pruebas ejecutadas

- `:core:datastore:testDebugUnitTest`: verde.
- `:core:data:testDebugUnitTest`: verde.
- `:feature:settings:compileDebugAndroidTestKotlin`: verde.
- `:app:assembleDebug`: verde.
- `lintDebug build`: verde en 1.135 tareas Gradle.
- 4 Compose instrumented tests en Pixel Tablet API 35:
  - opciones profesionales y eliminación segura deshabilitada;
  - formato argentino de WhatsApp;
  - protección del servicio principal;
  - certificado sin falsa validación administrativa.
- E2E automatizado en emulador API 35:
  - login real del trabajador validado;
  - Home del trabajador;
  - hub de cuenta;
  - datos personales;
  - presentación profesional;
  - servicios reales;
  - galería y certificados.
- Test de contrato contra OpenAPI: 18 operaciones requeridas presentes.
- Test API Trabajador:
  - lectura y actualización idempotente del perfil profesional;
  - carga real de foto de galería `id=10`;
  - carga real de certificado `id=3`;
  - limpieza y verificación posterior.
- Test API Cliente:
  - creación self-service de domicilio `id=8`;
  - edición y consulta;
  - eliminación y verificación posterior.

## Incidencias y requerimientos backend

- `BACK-ACCOUNT-001`: eliminación segura de la propia cuenta.
- `BACK-ACCOUNT-002`: actualización de perfil mediante `/me` y reglas explícitas al cambiar correo.
- `BACK-CONTACT-001`: creación idempotente. La cuenta cliente de prueba contiene siete contactos y dos grupos de números repetidos tras reintentos anteriores.
- `BACK-PROFILE-001`: edición atómica de servicios existentes. El PATCH actual solo modifica `autoAceptacion` y `principal`.
- `BACK-CERT-001`: estado administrativo, emisor, fechas y motivo de rechazo.
- `BACK-GALLERY-001`: descripción y orden estable de trabajos.

Todos están desarrollados con request/response propuestos, permisos, errores, idempotencia y criterios de aceptación en `docs/backend/BACKEND_IMPLEMENTATION_REQUIREMENTS.md`.

## Limpieza

Los recursos temporales creados por las pruebas (galería `10`, certificado `3` y domicilio `8`) fueron eliminados y una consulta posterior confirmó que no quedaron asociados a las cuentas de prueba.

## Limitación de validación

El recorrido completo de Cliente dentro de la app sigue condicionado por la verificación de correo y WhatsApp de la cuenta de prueba. La pantalla y sus estados se probaron en instrumented tests, y el CRUD cliente se validó contra la API real. La verificación E2E de Cliente → Home se retomará junto con contratación cuando exista una cuenta de prueba completamente verificada.
