# Cross-App Theme Sync & Flavor Profiles

## Theme deep link (Candy Store, Sugartube, LollipopLauncher)

SugarMunch main app handles theme sync via deep link so that when the user applies a theme in LollipopLauncher (or Candy Store), the main app can apply the same theme.

### Contract

- **URL:** `sugarmunch://theme?id=<theme_id>`
- **Example:** `sugarmunch://theme?id=midnight_mint`
- **theme_id:** Must match a theme ID from SugarMunch's theme presets (e.g. `classic_candy`, `sunrise_sherbet`, `sunset_swirl`, `midnight_mint`, `cotton_candy`, etc.).

### Sender (LollipopLauncher / Candy Store)

When the user selects a theme, send an intent or open the URL so that SugarMunch can receive it:

```kotlin
// Example: open URL (triggers MainActivity if SugarMunch is installed)
val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sugarmunch://theme?id=${themeId}"))
intent.setPackage("com.sugarmunch.app") // optional, to target SugarMunch
startActivity(intent)
```

Or broadcast / set result for in-process integration.

### Receiver (SugarMunch)

`MainActivity.handleDeepLink()` parses `sugarmunch://theme?id=...` and calls `ThemeManager.setThemeById(themeId)`.

---

## Flavor Profiles (LollipopLauncher)

"Flavor Profiles" are machine-learning or rule-based profiles that adapt the home screen layout by time or usage (e.g. "Workaholic Black Licorice" by day, "Relaxing Cotton Candy" by night).

### Contract for integration

- **Profile IDs** can align with theme IDs for consistency (e.g. `midnight_mint`, `cotton_candy`).
- LollipopLauncher may read a **shared preference or file** that SugarMunch writes when the user enables "Dynamic theme by time", so the launcher can switch layout/profile in sync:
  - Key: `sugarmunch_current_theme_id` (or similar)
  - Value: theme ID string
  - Storage: app-specific file or ContentProvider if cross-process.

Optional: SugarMunch can write the current theme ID to a shared location when the theme changes; LollipopLauncher reads it on resume and applies the matching Flavor Profile.
