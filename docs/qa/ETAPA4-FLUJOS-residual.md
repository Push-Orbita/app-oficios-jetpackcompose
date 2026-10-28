# Etapa 4 — residual vs página Flujos

Fecha: 2026-10-21. File `hhKrcrusbr3h6qJxqYBiTd`. Capturas Figma en `docs/qa/etapa4-flujos/`.

Umbral pedido: &lt;2 % pixel-diff o residual documentado (D-DIN vs DIN Pro, usted vs voseo, nav 4 vs 5 tabs Figma, datos de fixture).

| Pantalla | Fuente Flujos | Antes (QA 21/10, Rediseño) | Qué se alineó | Residual |
|---|---|---|---|---|
| Detalle pedido | `613:572` Confirmado; `613:637` Pendiente; `613:982` Terminar | 36.02 % vs `198:697` | Filas icon-box gap 8; mapa 248 r16; título por estado; monto solo con `nuevoPrecio`; plazo de cancelación solo en PENDIENTE; CTAs Confirmado al final | D-DIN; usted; Cancelar si `puedeCancelar` (no está en `613:572`); sin chip Efectivo ([BK-21](../backend-requests/BK-21-metodo-pago-pedido.md)); sin timeline (Figma no la pinta) |
| Ficha trabajador | `613:460` / `453:343` | 33.89 % vs `40:559` | Estructura ya era title + card + galería 376 + Trabajos realizados | Chevron atrás (Figma no lo pinta; hace falta en el back stack); Reportar perfil extra; 4 tabs vs 5; no se tocó Contactar/Favorito |
| Home trabajador | `526:31` + agenda `526:667` | 33.57 % vs `259:534` | Agenda sigue **dentro de Inicio**; Completa Tu Perfil solo &lt;100 %; AHORA/HOY por fecha real | `526:667` no pinta Beneficios; `526:31` sí. La app une ambos. 4 tabs vs 5 |
| Agenda (config) | `526:494` | 28.72 % vs `239:475` | Días / división / grilla / por oficio | CTA Guardar (necesario, Figma no lo pinta); sin bottom nav en config (es push, no tab); 4 vs 5 tabs |
| Perfil | no está en Flujos; Rediseño `231:15` | 29.00 % | Oficios, especializado, Editar rubro, trabajos, certificados, Darme de baja | Vive en Ajustes, no tab Perfil Figma; fotos reales vs fixture; 4 vs 5 tabs |

No se alcanzó el 2 %: tipografía D-DIN vs DIN Pro, chrome de 4 tabs y fotos de fixture dominan el pixel-diff aunque el árbol visual coincida.
