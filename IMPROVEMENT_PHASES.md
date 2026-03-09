# 🍭 SugarMunch - 5-Phase Improvement Plan

Based on comprehensive security scan and code analysis, this document outlines the complete improvement roadmap.

---

## 📊 Overview

| Phase | Focus Area | Priority | Estimated Effort | Status |
|-------|------------|----------|------------------|--------|
| **Phase 1** | Critical Security Fixes | 🔴 Critical | 2-3 hours | ⏳ Pending |
| **Phase 2** | Stability & Bug Fixes | 🟠 High | 4-6 hours | ⏳ Pending |
| **Phase 3** | Code Quality | 🟡 Medium | 3-4 hours | ⏳ Pending |
| **Phase 4** | Architecture | 🟢 Low | 6-8 hours | ⏳ Pending |
| **Phase 5** | Feature Completion | 🔵 Future | 8-12 hours | ⏳ Pending |

---

## 🔴 PHASE 1: Critical Security Fixes

**Goal:** Address all critical security vulnerabilities that could expose users to risk.

### 1.1 Fix Empty Trusted Certificates List
- **File:** `app/src/main/kotlin/com/sugarmunch/app/security/ApkSignatureVerifier.kt`
- **Issue:** `TRUSTED_CERTIFICATES` and `KNOWN_PACKAGES` are empty
- **Fix:** Add certificate hash validation system with configurable trusted sources
- **Risk if unfixed:** Malicious APKs could be distributed

### 1.2 Add Network Security Configuration
- **File:** `app/src/main/res/xml/network_security_config.xml`
- **Issue:** No certificate pinning, vulnerable to MITM attacks
- **Fix:** Create network security config with domain-specific pinning
- **Risk if unfixed:** Man-in-the-middle attacks on API calls

### 1.3 Remove printStackTrace() Calls
- **Files:** `P2PUiManager.kt`, `PluginManager.kt` (4 instances)
- **Issue:** Stack traces logged to production, potential info leakage
- **Fix:** Replace with proper Timber/Crashlytics logging
- **Risk if unfixed:** Sensitive information exposure

### 1.4 Fix ProGuard Rules
- **File:** `app/proguard-rules.pro`
- **Issue:** Overly permissive rules expose code structure
- **Fix:** Add specific keep rules, enable better obfuscation
- **Risk if unfixed:** Easier reverse engineering

### 1.5 Add Security Configuration Constants
- **File:** `app/src/main/kotlin/com/sugarmunch/app/security/SecurityConfig.kt` (NEW)
- **Issue:** Security settings scattered throughout codebase
- **Fix:** Centralize security configuration with compile-time checks

### 1.6 Update AndroidManifest
- **File:** `app/src/main/AndroidManifest.xml`
- **Issue:** Network security config not referenced
- **Fix:** Add `android:networkSecurityConfig` attribute

---

## 🟠 PHASE 2: Stability & Bug Fixes

**Goal:** Fix all unimplemented features and stability issues.

### 2.1 Implement Effect Engine Settings Loading
- **File:** `app/src/main/kotlin/com/sugarmunch/app/effects/v2/engine/EffectEngineV2.kt`
- **Issue:** Settings map always empty (line 97)
- **Fix:** Load and persist effect-specific settings from DataStore

### 2.2 Implement Cache Warming State Tracking
- **File:** `app/src/main/kotlin/com/sugarmunch/app/ai/SmartCacheManager.kt`
- **Issue:** `isCacheWarming` always false (line 396)
- **Fix:** Track actual cache warming operations with coroutines

### 2.3 Implement Hit Rate Tracking
- **File:** `app/src/main/kotlin/com/sugarmunch/app/ai/SmartCacheManager.kt`
- **Issue:** `calculateHitRate()` returns hardcoded 0.75f (line 503)
- **Fix:** Track cache hits/misses and calculate actual rate

### 2.4 Complete QuickInstallTileService
- **File:** `app/src/main/kotlin/com/sugarmunch/app/tiles/QuickInstallTileService.kt`
- **Issue:** Placeholder implementation (line 198)
- **Fix:** Implement actual quick install functionality

### 2.5 Fix Singleton Patterns
- **Files:** 36 files using double-checked locking
- **Issue:** Manual singleton pattern instead of Hilt
- **Fix:** Migrate to Hilt dependency injection where appropriate

### 2.6 Fix Context Memory Leaks
- **Files:** `OverlayService.kt`, `P2PUiManager.kt`
- **Issue:** Context stored in singletons
- **Fix:** Use `ApplicationContext` or weak references

---

## 🟡 PHASE 3: Code Quality Improvements

**Goal:** Improve code maintainability and reduce technical debt.

### 3.1 Migrate Wear Module to Version Catalogs
- **File:** `wear/build.gradle.kts`
- **Issue:** Hardcoded dependency versions
- **Fix:** Use `libs.versions.toml` for all dependencies

### 3.2 Fix Broad Exception Catching
- **Files:** 122 instances of `catch (e: Exception)`
- **Issue:** Catches all exceptions including unintended ones
- **Fix:** Catch specific exception types

### 3.3 Add Missing Unit Tests
- **Target:** 80% code coverage
- **Issue:** Only 9 test files, no TV/Wear tests
- **Fix:** Add tests for critical paths and new features

### 3.4 Add Integration Tests
- **Target:** TV and Wear modules
- **Issue:** No tests for platform-specific code
- **Fix:** Add UI and integration tests

### 3.5 Remove Deprecated Code
- **File:** `WeatherReactiveTheme.kt:68`
- **Issue:** Deprecated annotations without migration
- **Fix:** Update or remove deprecated APIs

### 3.6 Add KDoc Documentation
- **Target:** Public APIs and complex logic
- **Issue:** Missing documentation
- **Fix:** Add comprehensive KDoc comments

---

## 🟢 PHASE 4: Architecture Improvements

**Goal:** Modernize architecture for better maintainability.

### 4.1 Complete Modularization
- **Modules to create:**
  - `:features:catalog`
  - `:features:effects`
  - `:features:theme`
  - `:features:shop`
  - `:features:rewards`
  - `:features:social`
  - `:core:ui`
  - `:core:data`
  - `:core:analytics`

### 4.2 Kotlin 2.0 Migration
- **Files:** All build configuration
- **Issue:** Using older Kotlin version
- **Fix:** Update to Kotlin 2.0, enable K2 compiler

### 4.3 Update Dependencies
- **Issue:** Some dependencies outdated
- **Fix:** Update to latest stable versions

### 4.4 Add Baseline Profiles
- **File:** `app/src/main/baseline-prof.txt`
- **Issue:** Not updated for new features
- **Fix:** Generate and add profiles for critical paths

### 4.5 Implement Feature Flags
- **File:** `app/src/main/kotlin/com/sugarmunch/app/config/FeatureFlags.kt` (NEW)
- **Issue:** No feature flag system
- **Fix:** Add remote config-based feature flags

---

## 🔵 PHASE 5: Feature Completion

**Goal:** Complete remaining features from implementation plan.

### 5.1 Complete Phase 3 (UX)
- **Status:** 50% complete
- **Remaining:** Advanced accessibility, onboarding flow

### 5.2 Complete Phase 4 (Advanced Features)
- **Status:** 0% complete
- **Features:** Plugin system, advanced customization

### 5.3 Complete Phase 5 (Social Features)
- **Status:** 0% complete
- **Features:** Full clan system, social sharing

### 5.4 Complete Phases 6-10 (Platform Expansion)
- **Status:** 0% complete
- **Platforms:** TV, Wear, Auto full implementations

### 5.5 Performance Optimization
- **Target:** 60fps everywhere
- **Focus:** Animation performance, memory optimization

### 5.6 Accessibility Audit
- **Target:** Full WCAG compliance
- **Focus:** Screen reader, keyboard navigation

---

## 📅 Timeline

| Phase | Duration | Dependencies |
|-------|----------|--------------|
| Phase 1 | 1 day | None |
| Phase 2 | 2-3 days | Phase 1 |
| Phase 3 | 2 days | Phase 2 |
| Phase 4 | 1 week | Phase 3 |
| Phase 5 | 2 weeks | Phase 4 |

---

## ✅ Success Metrics

| Metric | Current | Target |
|--------|---------|--------|
| Security Vulnerabilities | 5 critical | 0 |
| TODO Comments | 3 | 0 |
| Test Coverage | ~30% | 80% |
| printStackTrace Calls | 4 | 0 |
| Manual Singletons | 36 | 0 |

---

---

## Recently completed (5-phase roadmap alignment)

- **Theme builder:** "Create Theme" button wired to `onThemeCreated(state.toCustomTheme()); onNavigateBack()` (ThemeBuilderScreen.kt).
- **PreferencesRepository:** Provided via Hilt; `LocalPreferencesRepository` CompositionLocal for UI; NavGraph and CatalogScreenV2 use injected prefs.
- **CatalogViewModel:** Converted to `@HiltViewModel` with injected ManifestRepository and PreferencesRepository; CatalogScreenV2 uses `hiltViewModel()`; CatalogViewModelFactory removed.
- **ManifestRepository:** Accepts optional `AppDao` from Hilt; uses `effectiveDao` (injected or from context) for single source of truth.
- **Weather API key:** Wired via BuildConfig.OPENWEATHER_API_KEY (env or project property); WeatherProvider uses it when non-blank.
- **AGENTS.md:** Added at project root (Android, Kotlin, Compose, Hilt, conventions).
- **Lint baseline:** Added `app/lint-baseline.xml` so lint runs without failing (regenerate with `./gradlew lintDebug`).
- **Design tokens:** docs/design-tokens.md and semantic colors in res/values/colors.xml.
- **App manifest schema:** AppEntry has optional `screenshots` (List<String>); trailerUrl already present.
- **Empty states:** EmptyStateType extended with CATALOG_LOAD_FAILED, NO_CLAN, NO_TRADES.
- **Store listing:** docs/store-listing.md and fastlane/metadata structure for main app.

*Last Updated: March 6, 2026*
