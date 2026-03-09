# SugarMunch – Deep Scan Improvement Report

**Date:** March 5, 2026  
**Scope:** Architecture, DI, data layer, security, testing, performance, and code quality.

---

## Executive summary

The app is a large Android (Kotlin + Compose) codebase with Hilt, Room, DataStore, and many features (themes, effects, marketplace, backup, P2P, etc.). Below are **high-impact improvements** grouped by category. One **critical bug** (cache fallback in `ManifestRepository`) has been fixed in this pass.

---

## Critical / bugs fixed in this pass

### 1. **ManifestRepository.fetchFromCache() – Flow usage (FIXED)**

- **File:** `app/src/main/kotlin/com/sugarmunch/app/data/ManifestRepository.kt`
- **Issue:** Code called `kotlinx.coroutines.flow.firstOrNull()` with no receiver, so the Flow was never collected. Cache fallback on network failure would not work.
- **Fix applied:** Use the Flow as receiver: get the flow in a variable and call `flow.firstOrNull() ?: emptyList()` with proper `firstOrNull` import.

---

## High priority – fix soon

### 2. **ThemeBuilderScreen – “Create Theme” button no-op**

- **File:** `app/src/main/kotlin/com/sugarmunch/app/theme/builder/ThemeBuilderScreen.kt` (around line 142)
- **Issue:** Final step button has `onClick = { /* Create theme */ }` and does nothing. The Preview step already has a working `onThemeCreated` path; the bottom “Create Theme” button does not call it.
- **Fix:** Wire the button to the same logic as the Preview step (e.g. call `onThemeCreated(state.toCustomTheme()); onNavigateBack()`).

### 3. **AppModule – unused / misleading parameters**

- **File:** `app/src/main/kotlin/com/sugarmunch/app/di/AppModule.kt`
- **provideManifestRepository:** Takes `dao: AppDao` but does not pass it to `ManifestRepository` (constructor only needs context and client). Either remove `dao` from the provider or add a constructor overload that uses the DAO and pass it.
- **provideSmartCacheManager:** Takes `database: AppDatabase`, `predictor: UsagePredictor`, `downloadManager: SmartDownloadManager` but only passes `context` to `SmartCacheManager(context)`. Either use these dependencies in `SmartCacheManager` or remove them from the provider to avoid confusion and wrong assumptions (e.g. that cache uses DB/predictor).

### 4. **PreferencesRepository not in Hilt + duplicate DataStore name**

- **Files:** `PreferencesRepository.kt`, `NavGraph.kt`, `DetailScreen.kt`, `CatalogScreenV2.kt`, `AnimatedThemeBackground.kt`, `SettingsViewModel.kt`, `CatalogViewModel.kt`
- **Issues:**
  - `PreferencesRepository` is created manually in multiple places with `PreferencesRepository(context)` instead of being injected. This makes testing harder and can lead to multiple instances.
  - App has two preference stores: `sugarmunch_prefs` (used by `PreferencesRepository`) and `sugarmunch_preferences` (provided in `AppModule`). Naming and usage are inconsistent.
- **Fix:** Add `PreferencesRepository` to Hilt (e.g. inject `DataStore<Preferences>` with a single, agreed name), provide it from a module, and inject it in ViewModels and composables (via `hiltViewModel()` or composition local). Use one DataStore name for app UI preferences and document the other if it’s for a different purpose.

### 5. **CatalogViewModel not using Hilt**

- **Files:** `CatalogViewModel.kt`, `CatalogViewModelFactory.kt`, `CatalogScreenV2.kt`
- **Issue:** `CatalogViewModel` uses a custom `ViewModelProvider.Factory` and manual construction instead of `@HiltViewModel` and `hiltViewModel()`. This is inconsistent with e.g. `BackupRestoreViewModel` and makes testing and reuse harder.
- **Fix:** Convert `CatalogViewModel` to `@HiltViewModel` and inject `ManifestRepository` and `PreferencesRepository`; use `hiltViewModel()` in `CatalogScreenV2` and remove `CatalogViewModelFactory`.

### 6. **ManifestRepository dual source of truth for DB**

- **Files:** `ManifestRepository.kt`, `AppModule.kt`
- **Issue:** When context is present, `ManifestRepository` gets the DB via `AppDatabase.getDatabase(it)` instead of injected `AppDatabase`/`AppDao`. Hilt provides a single `AppDatabase`; the static `getDatabase` path can create a second instance or different config (e.g. migrations).
- **Fix:** Make `ManifestRepository` depend only on injected `AppDao` (and `OkHttpClient`), and remove internal use of `AppDatabase.getDatabase(context)`. Then `provideManifestRepository` can pass the Hilt-provided `AppDao`.

---

## Security & configuration

### 7. **.env and secrets**

- **Observation:** A `.env` file appears in the project; `.gitignore` does not list `.env`.
- **Risk:** If secrets are ever put in `.env`, they could be committed.
- **Fix:** Add `.env` to `.gitignore` and keep secrets in build config / environment / secret manager, not in repo.

### 8. **WeatherProvider API key**

- **File:** `app/src/main/kotlin/com/sugarmunch/app/theme/reactive/WeatherProvider.kt`
- **Issue:** `apiKey` is `null` with a “Set your API key” comment. There is no wiring to BuildConfig or a secure config.
- **Fix:** Add a BuildConfig field or injected config (e.g. from a Hilt module) for the OpenWeather API key, and use it in `WeatherProvider`. Ensure the key is not in source control.

### 9. **ProGuard: package names not obfuscated**

- **File:** `app/proguard-rules.pro`
- **Issue:** `-dontobfuscatepackage` keeps package names readable, easing reverse engineering.
- **Fix:** Remove this directive for release if you want stronger obfuscation, or limit it to packages that must stay readable (e.g. for reflection). Weigh against crash report deobfuscation (mapping file).

### 10. **Network / base URL**

- **File:** `app/src/main/kotlin/com/sugarmunch/app/di/AppModule.kt`
- **Issue:** Retrofit `baseUrl` is hardcoded as `"https://raw.githubusercontent.com/"`. Acceptable for a fixed catalog, but any other API would need a different base URL.
- **Fix:** For multiple backends, move base URL to BuildConfig or a config module and inject it.

---

## Architecture & consistency

### 11. **Too many getInstance() singletons**

- **Observation:** Many classes use `companion object { getInstance(...) }` (e.g. `SmartDownloadManager`, `DailyRewardsManager`, `ApkSignatureVerifier`, `BatteryOptimizationManager`, `TradeManager`, `MarketManager`, `ProtonDriveManager`, `UsagePredictor`, etc.) while Hilt is already used.
- **Issue:** Double lifecycle (Hilt + getInstance) and possible multiple instances or inconsistent state. Harder to test and to reason about dependencies.
- **Fix:** Prefer Hilt for these: `@Singleton` (or appropriate scope) and `@Inject constructor(...)`. Remove or deprecate `getInstance` and use injection only. Do this incrementally (e.g. start with one manager and its call sites).

### 12. **Navigation and repositories**

- **File:** `app/src/main/kotlin/com/sugarmunch/app/ui/navigation/NavGraph.kt`
- **Issue:** NavGraph creates `PreferencesRepository(context)` and uses it for onboarding state. Navigation should not own repository creation.
- **Fix:** Inject a ViewModel or use case that holds onboarding state (backed by DataStore/PreferencesRepository), and use that in the nav host (e.g. via `hiltViewModel()` in the onboarding composable or a shared ViewModel).

### 13. **core:ui module underused**

- **Files:** `core/ui/build.gradle.kts`, app structure
- **Observation:** `core:ui` exists but the app module has a large amount of UI in `app/`. Shared design system (colors, typography, components) could live in `core:ui`.
- **Fix:** Move shared Compose theme, base components, and design tokens to `core:ui` and have `app` depend on it. Reduces duplication and keeps UI consistent.

---

## Code quality & maintainability

### 14. **Oversized composables**

- **Examples:** `ThemeBuilderScreen.kt` (~1,350+ lines), `ThemeBuilderModels.kt` (~400+ lines). Other large UI files likely exist.
- **Issue:** Hard to navigate, test, and reuse; recomposition and previews are harder.
- **Fix:** Split by step or feature: one file per step (e.g. `ColorPickerStep`, `GradientEditorStep`, `EffectsEditorStep`, `PreviewStep`) and shared state in a small state-holder or ViewModel. Extract reusable pieces (e.g. color picker, gradient editor) into separate composables in the same or a shared module.

### 15. **Dead / redundant code**

- **CatalogScreen.kt:** Uses `CatalogViewModel(ManifestRepository(context))` without `PreferencesRepository`, so sort/filter prefs are never applied in that screen. If `CatalogScreen` is unused, consider removing it or switching it to the same ViewModel/source as `CatalogScreenV2`.

### 16. **CoroutineUtils.parallelExecuteSafe**

- **File:** `app/src/main/kotlin/com/sugarmunch/app/util/CoroutineUtils.kt`
- **Observation:** Implementation uses `runCatching { op() }` and then `getOrNull()` and wraps in `Result.success(it)`. So a failed operation becomes `Result.success(null)` instead of `Result.failure(...)`. Callers may assume they get a list of `Result<T>` with real success/failure.
- **Fix:** Either return `List<Result<T>>` where each element is `runCatching { op() }`, or document clearly that only success values are returned and failures are represented as null.

### 17. **Lint baseline**

- **File:** `app/build.gradle.kts` references `lint-baseline.xml`; that file was not found in the repo.
- **Fix:** Add a real `app/lint-baseline.xml` (or generate one with `./gradlew lintDebug` and “Create baseline”) so the build does not fail and you can gradually fix baseline issues.

---

## Testing

### 18. **Test coverage vs. codebase size**

- **Observation:** There are ~18 unit/androidTest files for a large number of app classes (many ViewModels, repositories, managers, screens).
- **Fix:** Add unit tests for: `ManifestRepository` (fetch + cache fallback), `PreferencesRepository`, `CatalogViewModel`, and critical theme/effect logic. Add targeted UI tests for main flows (catalog, detail, settings). Enforce a minimum coverage (e.g. 80%) in CI; jacoco is already configured.

### 19. **CatalogScreenUiTest**

- **File:** `app/src/androidTest/.../CatalogScreenUiTest.kt`
- **Observation:** Comments indicate “This would require mocking PreferencesRepository” – tests may be skipped or incomplete.
- **Fix:** Use Hilt’s test setup and replace `PreferencesRepository` (and DataStore) with fakes or in-memory implementations so catalog UI tests run without real prefs.

---

## Performance & dependency hygiene

### 20. **HttpLoggingInterceptor in release**

- **File:** `app/src/main/kotlin/com/sugarmunch/app/di/AppModule.kt`
- **Observation:** Logging level is set to `NONE` when `!BuildConfig.DEBUG`, which is correct. No change needed; just ensure no other interceptors log in release.

### 21. **Dependency and plugin versions**

- **File:** `gradle/libs.versions.toml`
- **Observation:** Versions are pinned (good). AGP 8.2.0, Kotlin 1.9.25, compileSdk 35 – reasonable.
- **Fix:** Periodically update BOMs and key libs (Compose BOM, Room, Hilt, Kotlin) and run tests and manual smoke tests. Consider Dependabot or Renovate.

### 22. **Release signing fallback**

- **File:** `app/build.gradle.kts`
- **Observation:** When release keystore is not configured, release falls back to debug signing. Safe for local builds; for CI/store uploads, fail the build if release signing is missing (e.g. require `KEYSTORE_PATH` in release variant).

---

## Documentation and process

### 23. **IMPROVEMENT_PHASES.md vs. current state**

- **File:** `IMPROVEMENT_PHASES.md`
- **Observation:** Phases 1–5 are marked “Pending”. Some items (e.g. SecurityConfig, network security config, ProGuard) may already be partially done.
- **Fix:** Re-audit each phase, mark completed items, and move “Phase 2: Singleton patterns” and “PreferencesRepository / DataStore” into the same plan as this report so they’re tracked in one place.

### 24. **AGENTS.md / project rules**

- **Observation:** Workspace rules refer to a Node/React/Express/PostgreSQL stack and an `AGENTS.md` that doesn’t exist in this Android repo.
- **Fix:** Add an `AGENTS.md` (or similar) at the repo root describing this project: Android, Kotlin, Compose, Hilt, Room, and any conventions (e.g. “new screens use Hilt ViewModels”, “repos in data layer only”).

---

## Summary checklist

| Area              | Action |
|-------------------|--------|
| Data / cache       | ✅ Fix `fetchFromCache()` (done); unify ManifestRepository on injected DAO |
| DI                 | Remove unused AppModule params; add PreferencesRepository and CatalogViewModel to Hilt |
| UI                 | Wire ThemeBuilderScreen “Create Theme” button; split large composables |
| Security           | Add `.env` to .gitignore; wire Weather API key via config; review ProGuard |
| Testing            | Add tests for repos and main ViewModels; fix CatalogScreenUiTest with fakes |
| Architecture       | Migrate getInstance() to Hilt; use core:ui for shared UI; single DataStore naming |
| Docs / process     | Add AGENTS.md; refresh IMPROVEMENT_PHASES.md; add lint-baseline.xml if missing |

Implementing the high-priority and architecture items will significantly improve correctness, testability, and long-term maintainability of SugarMunch.
