const { buildCierreModulo } = require('./gen_cierre_modulo');

buildCierreModulo({
  num:         "02",
  nombre:      "Design System Base",
  complejidad: "Fundación — Módulo transversal de UI",
  fecha:       "03/07/2026",
  diasEst:     "5",
  diasReal:    "5",
  variacion:   "Sin variación",
  estado:      "Completado",
  output:      '/sessions/quirky-trusting-hopper/mnt/outputs/Informe_Cierre_M02.docx',

  alcance: [
    [
      "OficiosTheme",
      "Tema global de la aplicación implementado con Material 3. Expone paleta de colores, tipografía y formas coherentes en modos claro y oscuro. Es el punto de entrada visual de todos los composables de la aplicación."
    ],
    [
      "Paleta de colores",
      "Colores semánticos definidos como constantes tipadas: OfiPrimary, OfiSecondary, OfiBackground, OfiSurface, OfiError, OfiOnPrimary, entre otros. La paleta distingue entre colores en modo claro y en modo oscuro."
    ],
    [
      "Sistema tipográfico",
      "Escala completa de tipografía: DisplayLarge, HeadlineMedium, TitleSmall, BodyLarge, BodyMedium, LabelSmall y variantes adicionales. Fuente Urbanist importada como recurso de la aplicación."
    ],
    [
      "Tokens de espaciado (AppSpacing)",
      "Constantes de espaciado estandarizadas en pasos de 4 dp: AppSpacing.XS (4 dp), SM (8 dp), MD (16 dp), LG (24 dp), XL (32 dp), XXL (48 dp). Su uso garantiza una cuadrícula visual consistente en toda la interfaz."
    ],
    [
      "Tokens complementarios",
      "AppElevation (elevaciones predefinidas: level0..level5), Alpha (opacidades: disabled, medium, high), AppRadius (radios de esquinas: small, medium, large, full). Se emplean en todos los componentes de la biblioteca."
    ],
    [
      "Componentes atómicos",
      "OficiosButton, OficiosPrimaryButton, OficiosSecondaryButton, OficiosTextButton, OficiosTextField, OficiosPasswordField, OficiosSearchField. Todos con estados enabled/disabled/loading y soporte a accesibilidad."
    ],
    [
      "Componentes de contenido",
      "OficiosCard, OficiosProfileCard, OficiosServiceCard, OficiosBadge, OficiosAvatar, OficiosRatingBar, OficiosChip, OficiosTagChip."
    ],
    [
      "Componentes de navegación y layout",
      "OficiosBottomNav, OficiosTopAppBar, OficiosSearchBar, OficiosLoadingSpinner, OficiosErrorState, OficiosEmptyState, OficiosDivider."
    ],
    [
      "Componentes de retroalimentación",
      "OficiosSnackbar, OficiosDialog, OficiosConfirmDialog, OficiosToast, OficiosProgressBar, OficiosStepIndicator, OficiosSkeleton."
    ],
    [
      "Previews de componentes",
      "Cada composable cuenta con @Preview anotada para modo claro y oscuro, con datos de muestra representativos. Permite revisión visual en tiempo de diseño sin necesidad de ejecutar la aplicación."
    ],
    [
      "Módulo :core:designsystem",
      "Todos los componentes residen en un módulo Gradle independiente, sin dependencia de lógica de negocio. Cualquier feature puede importarlo sin introducir dependencias circulares."
    ],
  ],

  decisiones: [
    "Módulo independiente :core:designsystem: separar el sistema de diseño de los módulos de feature elimina las dependencias circulares y permite que cualquier equipo extienda la biblioteca sin afectar la lógica de negocio. Las builds parciales también se benefician, ya que el módulo de diseño solo se recompila cuando sus propios archivos cambian.",
    "Prefijo Oficios* en todos los composables: la convención de nomenclatura evita colisiones con los nombres de Material 3 y facilita la autocompletación en el IDE. Al escribir 'Oficios', el desarrollador obtiene la lista completa de componentes disponibles de forma inmediata.",
    "Tokens de diseño tipados (AppSpacing, AppElevation, Alpha, AppRadius): en lugar de usar valores numéricos dispersos (padding = 8.dp), se utilizan constantes semánticas (padding = AppSpacing.SM). Esto garantiza coherencia visual en todos los features y simplifica cambios globales de espaciado.",
    "Material 3 como base del sistema: el uso de MaterialTheme.colorScheme y MaterialTheme.typography internamente en cada componente asegura compatibilidad automática con el modo oscuro y con cambios de tema dinámico de Android 12+.",
    "@Preview en modos claro y oscuro: todas las previews incluyen @Preview(uiMode = UI_MODE_NIGHT_YES) junto a la versión estándar, lo que permite detectar problemas de contraste y legibilidad sin ejecutar la aplicación ni conectar un dispositivo.",
  ],

  testing: [
    ["Componentes visuales (40+)",  "Revisión de @Preview", "Validados en modo claro y oscuro directamente desde Android Studio."],
    ["Tokens de espaciado y radio",  "Revisión manual",      "Verificados en pantallas con densidades 1x, 2x y 3x (mdpi, xhdpi, xxhdpi)."],
    ["Soporte modo oscuro",          "Revisión de @Preview", "Revisión de contraste y legibilidad mediante previews con UI_MODE_NIGHT_YES."],
  ],

  dependencias: [
    "Todos los módulos de feature (auth, home, onboarding, settings) dependen de :core:designsystem para sus pantallas Compose. Sin este módulo, ninguna pantalla puede ser construida.",
    "El sistema de tipografía y la paleta de colores son la referencia visual única de la aplicación; cualquier componente nuevo debe importar los tokens de este módulo en lugar de definir valores propios.",
    "Los componentes de estado (OficiosLoadingSpinner, OficiosErrorState, OficiosEmptyState) son utilizados directamente por los ViewModels a través del patrón UiState, que distingue los estados Loading, Success y Error.",
    "OficiosTheme envuelve toda la jerarquía de composables desde el nivel de Activity, lo que garantiza que la paleta de colores y la tipografía estén disponibles en cualquier punto del árbol de composición.",
  ],
});
