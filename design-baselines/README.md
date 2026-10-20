# Baselines de diseño de Figma

Este directorio contiene las imágenes de referencia exportadas de Figma para verificación de fidelidad 1:1.

## Estructura

```
design-baselines/
├── screens/          # Screenshots de referencia de Figma por pantalla
│   ├── redisenio/   # Exportados de la página "Rediseño 3:3"
│   └── flujos/      # Exportados de la página "Flujos 3:2"
└── README.md
```

## Cómo obtener las imágenes de Figma

Las imágenes de referencia deben exportarse desde el archivo de Figma:

- **Archivo Figma**: `hhKrcrusbr3h6qJxqYBiTd`
- **Páginas**: "Rediseño 3:3" y "Flujos 3:2"

### Exportar desde Figma

1. Abrir el archivo en Figma
2. Seleccionar el frame de la pantalla deseada
3. Clic derecho → Export → PNG @ 1x
4. Guardar en el subdirectorio correspondiente con nombre descriptivo

### Nomenclatura recomendada

Usar el mismo formato que `uploads/MAPA_FIGMA_APP.md`:

```
<nn>-<slug>.png
```

Ejemplos:
- `02-ingrese-sus-datos-para-iniciar-sesion.png` (Login)
- `05-nicolas-ayala.png` (Home cliente)
- `14-pedido-confirmado.png` (Pedido confirmado)

## Actualización

Estas imágenes deben actualizarse cuando:

- El diseño en Figma cambie
- Se agreguen nuevas pantallas
- Se requiera mayor precisión en la comparación

> **Nota**: Las imágenes no se incluyen en el repositorio por su tamaño. Deben exportarse localmente o descargarse desde el archivo de Figma para ejecutar las comparaciones.
