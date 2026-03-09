# Project Instructions

## Tech Stack

- **Platform:** Android (Kotlin 1.9+, Jetpack Compose, Material 3)
- **DI:** Hilt
- **Data:** Room (AppDatabase, PassDatabase), DataStore (preferences), OkHttp/Retrofit
- **Testing:** JUnit, AndroidX Test, Compose UI test
- **Tools:** Gradle (Kotlin DSL), Git

## Quick Commands

- `./gradlew assembleDebug` – Build debug APK
- `./gradlew lintDebug` – Run lint (uses lint-baseline.xml)
- `./gradlew test` – Run unit tests
- `./gradlew :app:testDebugUnitTest` – App unit tests

## Code Standards

### Kotlin

- Strict null safety; prefer `val` over `var`
- Use data classes for models; export types from `data/` or `types`
- Coroutines/Flow for async; prefer `StateFlow` for UI state

### Compose

- Functional composables only; no View-based UI in new code
- State hoisting; use ViewModels with `hiltViewModel()` where needed
- Use design tokens from `ui/design/` (SugarDimens, CardStyles, SugarShapes)

### Architecture

- **UI** → **ViewModel** → **Repository / Manager** → **Room or Network**
- Business logic in services/managers; controllers and composables stay thin
- New screens use Hilt ViewModels; repositories live in the data layer and are injected

### Security

- Validate and sanitize external input; use the security rules in `.cursor/rules/security.mdc`
- No secrets in repo; use BuildConfig or env for API keys (e.g. OPENWEATHER_API_KEY)
- Parameterized queries only; no SQL concatenation

## Project Structure

```
app/
├── data/           # Repositories, manifest, preferences
├── di/             # Hilt modules
├── ui/             # Compose screens, components, theme, navigation
├── theme/          # Theme engine, builder, reactive themes
├── effects/        # Effect engine, FAB, overlay
├── work/           # WorkManager workers
├── auth/           # Firebase Auth, session
└── ...
core/ui/            # Shared Compose theme and base components (target)
wear/               # Wear OS module
tv/                 # Android TV module
docs/               # Architecture, API, design-tokens, deployment
```

## References

- [Architecture](docs/architecture.md)
- [Design tokens](docs/design-tokens.md)
- [Accessibility](docs/accessibility.md)
- [Performance](docs/performance.md)
- [Securing release](docs/SECURING_RELEASE.md)
