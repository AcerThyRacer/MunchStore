# Design Tokens

SugarMunch uses a single source of truth for spacing, elevation, radius, and semantic colors so the app stays visually consistent.

## Tokens

- **Spacing / layout:** `SugarDimens` in `app/src/main/kotlin/com/sugarmunch/app/ui/design/SugarDimens.kt`  
  Use `SugarDimens.Spacing`, `SugarDimens.Radius`, `SugarDimens.Elevation`, `SugarDimens.TouchTarget`, etc.  
  Base grid: 4dp.

- **Card styles:** `CardStyle` and `SugarCard` in `CardStyles.kt`  
  FLAT, ELEVATED, OUTLINED, GLASSMORPHIC, NEON. Prefer these over ad-hoc Card usage.

- **Shapes:** `SugarShapes.kt` and `SugarDimens.Radius`  
  Use `RoundedCornerShape(SugarDimens.Radius.md)` (or .sm, .lg) for cards and surfaces.

- **Semantic colors (XML):** `app/src/main/res/values/colors.xml`  
  - `semantic_success` / `semantic_success_container` – success toasts, confirmations  
  - `semantic_warning` / `semantic_warning_container` – warnings, caution  
  - `semantic_error` / `semantic_error_container` – errors, destructive actions  
  - `semantic_info` / `semantic_info_container` – info banners, tips  
  Use these for toasts, banners, and empty states so they meet WCAG AA where applicable.

- **Compose theme:** `MaterialTheme.colorScheme` is driven by `Theme.kt` and `ThemeManager`; use `primary`, `onSurface`, `surface`, etc. for UI. For semantic feedback in Compose, map XML semantic colors to `Color(0xFF...)` or add extension properties on `ColorScheme` if desired.

## Accessibility

- Touch targets: prefer `SugarDimens.TouchTarget.minimum` (44dp) or `standard` (48dp).
- Contrast: ensure text on background uses `onSurface` / `onPrimary` etc.; semantic colors in colors.xml are chosen for contrast.
