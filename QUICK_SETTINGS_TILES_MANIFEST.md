# Quick Settings Tiles - AndroidManifest.xml Entries

Add these entries to your `AndroidManifest.xml` to register the Quick Settings tiles:

## Required Permission

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- Required for Quick Settings tiles -->
    <uses-permission android:name="android.permission.BIND_QUICK_SETTINGS_TILE" />
    
    <!-- ... other permissions ... -->
```

## Service Declarations

Add these `<service>` entries inside your `<application>` tag:

```xml
<application
    android:name=".SugarMunchApplication"
    ... >

    <!-- ═══════════════════════════════════════════════════════════════ -->
    <!-- QUICK SETTINGS TILES -->
    <!-- ═══════════════════════════════════════════════════════════════ -->

    <!-- Effect Toggle Tile - Shows active effect count and opens effects panel -->
    <service
        android:name=".tiles.EffectToggleTileService"
        android:enabled="true"
        android:exported="true"
        android:icon="@drawable/ic_tile_effects"
        android:label="@string/tile_effects_label"
        android:permission="android.permission.BIND_QUICK_SETTINGS_TILE">
        <intent-filter>
            <action android:name="android.service.quicksettings.action.QS_TILE" />
        </intent-filter>
        <meta-data
            android:name="android.service.quicksettings.TOGGLEABLE_TILE"
            android:value="false" />
    </service>

    <!-- SugarRush Tile - Dedicated tile for SugarRush effect -->
    <service
        android:name=".tiles.SugarRushTileService"
        android:enabled="true"
        android:exported="true"
        android:icon="@drawable/ic_tile_sugarrush"
        android:label="@string/tile_sugarrush_label"
        android:permission="android.permission.BIND_QUICK_SETTINGS_TILE">
        <intent-filter>
            <action android:name="android.service.quicksettings.action.QS_TILE" />
        </intent-filter>
        <meta-data
            android:name="android.service.quicksettings.TOGGLEABLE_TILE"
            android:value="true" />
    </service>

    <!-- Theme Switcher Tile - Cycle through favorite themes -->
    <service
        android:name=".tiles.ThemeSwitcherTileService"
        android:enabled="true"
        android:exported="true"
        android:icon="@drawable/ic_tile_theme"
        android:label="@string/tile_theme_label"
        android:permission="android.permission.BIND_QUICK_SETTINGS_TILE">
        <intent-filter>
            <action android:name="android.service.quicksettings.action.QS_TILE" />
        </intent-filter>
        <meta-data
            android:name="android.service.quicksettings.TOGGLEABLE_TILE"
            android:value="false" />
    </service>

    <!-- Quick Install Tile - Shows suggested app and download progress -->
    <service
        android:name=".tiles.QuickInstallTileService"
        android:enabled="true"
        android:exported="true"
        android:icon="@drawable/ic_tile_install"
        android:label="@string/tile_install_label"
        android:permission="android.permission.BIND_QUICK_SETTINGS_TILE">
        <intent-filter>
            <action android:name="android.service.quicksettings.action.QS_TILE" />
        </intent-filter>
        <meta-data
            android:name="android.service.quicksettings.TOGGLEABLE_TILE"
            android:value="false" />
    </service>

    <!-- ... rest of your application ... -->

</application>
```

## Required String Resources

Add these to `res/values/strings.xml`:

```xml
<string name="tile_effects_label">SugarMunch Effects</string>
<string name="tile_sugarrush_label">SugarRush</string>
<string name="tile_theme_label">Candy Theme</string>
<string name="tile_install_label">Quick Install</string>
```

## Recommended Drawable Resources

Create these drawable resources in `res/drawable/` (or use existing app icons):

### ic_tile_effects.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM12,20c-4.41,0 -8,-3.59 -8,-8s3.59,-8 8,-8 8,3.59 8,8 -3.59,8 -8,8zM12,6c-3.31,0 -6,2.69 -6,6s2.69,6 6,6 6,-2.69 6,-6 -2.69,-6 -6,-6z" />
</vector>
```

### ic_tile_sugarrush.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M13.5,0.67s0.74,2.65 0.74,4.8c0,2.06 -1.35,3.73 -3.41,3.73 -2.07,0 -3.63,-1.67 -3.63,-3.73l0.03,-0.36C5.21,7.51 4,10.62 4,14c0,4.42 3.58,8 8,8s8,-3.58 8,-8C20,8.61 17.41,3.8 13.5,0.67zM11.71,19c-1.78,0 -3.22,-1.4 -3.22,-3.14 0,-1.62 1.05,-2.76 2.81,-3.12 1.77,-0.36 3.6,-1.21 4.62,-2.58 0.39,1.29 0.59,2.65 0.59,4.04 0,2.65 -2.15,4.8 -4.8,4.8z" />
</vector>
```

### ic_tile_theme.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M12,3c-4.97,0 -9,4.03 -9,9s4.03,9 9,9c0.83,0 1.5,-0.67 1.5,-1.5 0,-0.39 -0.15,-0.74 -0.39,-1.01 -0.23,-0.26 -0.38,-0.61 -0.38,-0.99 0,-0.83 0.67,-1.5 1.5,-1.5H16c2.76,0 5,-2.24 5,-5 0,-4.42 -4.03,-8 -9,-8zM6.5,12c-0.83,0 -1.5,-0.67 -1.5,-1.5S5.67,9 6.5,9 8,9.67 8,10.5 7.33,12 6.5,12zM9.5,8C8.67,8 8,7.33 8,6.5S8.67,5 9.5,5s1.5,0.67 1.5,1.5S10.33,8 9.5,8zM14.5,8c-0.83,0 -1.5,-0.67 -1.5,-1.5S13.67,5 14.5,5s1.5,0.67 1.5,1.5S15.33,8 14.5,8zM17.5,12c-0.83,0 -1.5,-0.67 -1.5,-1.5S16.67,9 17.5,9s1.5,0.67 1.5,1.5 -0.67,1.5 -1.5,1.5z" />
</vector>
```

### ic_tile_install.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M19,9h-4V3H9v6H5l7,7 7,-7zM5,18v2h14v-2H5z" />
</vector>
```

## How to Add Tiles

After installing the app with these services:

1. **Open Quick Settings Panel** - Swipe down from the top of the screen twice
2. **Edit Tiles** - Tap the pencil/edit icon
3. **Add SugarMunch Tiles** - Drag the SugarMunch tiles from the "Available tiles" section to the "Active tiles" section
4. **Reorder** - Arrange tiles in your preferred order

## Tile Features Summary

| Tile | Tap Action | Long Press Action | Dynamic State |
|------|-----------|-------------------|---------------|
| **Effects Toggle** | Open effects panel | Open main app | Shows active effect count |
| **SugarRush** | Toggle effect ON/OFF | Open effect settings | Shows ON/OFF subtitle |
| **Theme Switcher** | Cycle to next theme | Open theme settings | Shows current theme name |
| **Quick Install** | Start download | Open app catalog | Shows download progress |

## Handling Tile Intents in MainActivity

To handle the intent extras sent by the tiles, add this to your `MainActivity.onCreate()` or `onNewIntent()`:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Handle tile intents
    handleTileIntent(intent)
    
    // ... rest of onCreate
}

override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    handleTileIntent(intent)
}

private fun handleTileIntent(intent: Intent?) {
    intent?.let {
        when {
            it.getBooleanExtra(EffectToggleTileService.EXTRA_OPEN_EFFECTS, false) -> {
                // Navigate to effects screen
                navigateToEffectsScreen()
            }
            it.getBooleanExtra(SugarRushTileService.EXTRA_OPEN_SUGARRUSH_SETTINGS, false) -> {
                // Navigate to SugarRush settings
                navigateToEffectSettings(SugarRushTileService.SUGARRUSH_EFFECT_ID)
            }
            it.getBooleanExtra(ThemeSwitcherTileService.EXTRA_OPEN_THEME_SETTINGS, false) -> {
                // Navigate to theme settings
                navigateToThemeSettings()
            }
            it.getBooleanExtra(QuickInstallTileService.EXTRA_OPEN_CATALOG, false) -> {
                // Navigate to catalog
                val highlightApp = it.getStringExtra(QuickInstallTileService.EXTRA_HIGHLIGHT_APP)
                navigateToCatalog(highlightApp)
            }
        }
    }
}
```

## Notes

- Tiles are only available on Android 7.0 (API 24) and above
- The `BIND_QUICK_SETTINGS_TILE` permission is required
- Tiles must be added by the user through the Quick Settings panel
- Tile icons should be white (alpha channel only) for proper theming
- Use `android:label` for the tile name shown in the tile picker
