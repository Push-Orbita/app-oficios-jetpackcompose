# Fase 1 — Auditoría de pantallas existentes vs. diseño v2

## Criterio de cierre

- Pantallas ya construidas (Auth, Onboarding, Home cliente, Ficha de trabajador — módulos M02/M03/M04/M05) comparadas pantalla por pantalla contra el README del handoff y el prototipo HTML.
- Documentado qué se dejó igual, qué se ajustó (copy/layout) y por qué, priorizando coherencia de flujo por sobre fidelidad pixel-perfect cuando compitieron (instrucción explícita del usuario).

## Detalle

Ver [DESIGN_ALIGNMENT.md](DESIGN_ALIGNMENT.md) — tabla completa pantalla por pantalla con el veredicto {alineada sin cambios | copy ajustado | layout ajustado} y la razón de cada decisión, cubriendo:

- **Auth**: Splash, Login, Registro de cuenta, Registro de oficio.
- **Onboarding**: DNI/selfie, rubros/sub-rubros, disponibilidad, medios de pago, barrio.
- **Home cliente**: header + buscador + recientes, categorías, subcategoría, tarjeta de resultado.
- **Ficha completa de trabajador**: especialidades, disponibilidad, galería, certificados, reseñas.

## Fuera de alcance

- No se tocó lógica de negocio ni flujos de guardado parcial ya funcionales en Onboarding — solo copy/layout donde el handoff difería.
- Home trabajador y Pedidos quedaron fuera (no existían aún) — cubiertos por Módulo 5.1 y Módulo 6 respectivamente.
