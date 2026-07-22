# M05 Closure — Ficha del trabajador

## Qué se considera “cerrado” en M05

1. Desde `Home` y desde `SearchResults` se navega a la ficha completa del trabajador (ya no se muestra el stub).
2. Secciones visibles según datos:
   - Especialidades (chips por rubro).
   - Disponibilidad (chip con label resumido).
   - Galería (LazyRow con `WorkGalleryCard` con Coil).
   - Certificados (lista de `CertificateCard` cuando existan).
   - Reseñas (lista con `WorkerReviewCard` y paginación/load more).
3. Estados de carga/error/retry operativos en `WorkerProfileViewModel`/`WorkerProfileScreen`.
4. El CTA “Contratar” navega a un stub M06 (stub mínimo de “Próximamente” con el id del trabajador).
5. Back stack correcto: el botón de volver retorna al origen (popBackStack).
6. Tests unitarios y de integración para M05 pasan en verde.
7. No quedan referencias en código a `WorkerStub*`; los archivos stubs son eliminados.

