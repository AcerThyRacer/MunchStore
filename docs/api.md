# API and Contracts

## App catalog manifest

- **Source:** JSON from GitHub raw (see [ManifestRepository](app/src/main/kotlin/com/sugarmunch/app/data/ManifestRepository.kt)).
- **URL:** Configurable; default in code. Schema: list of apps with `id`, `name`, `packageName`, `downloadUrl`, `iconUrl`, `version`, `featured`, etc.
- **Caching:** Room and in-memory; stale data is refreshed on launch or pull-to-refresh.

## Plugin API

- **Manifest:** Plugins provide a manifest (ID, name, version, author, permissions, minApiLevel). Validated by [PluginSecurity](app/src/main/kotlin/com/sugarmunch/app/plugin/security/PluginSecurity.kt).
- **Signature:** Directory plugins can include a `signature` file (X.509 cert); verified against trusted authorities (issuer/subject match). `.smpkg` plugins use JAR signing in META-INF.
- **Permissions:** See [PluginPermission](app/src/main/kotlin/com/sugarmunch/app/plugin/model/PluginModels.kt). Dangerous permissions (e.g. SYSTEM_HOOKS, ACCESSIBILITY) require explicit user approval; safe ones can be auto-granted.
- **Versioning:** Plugin manifest declares `minApiLevel` and compatibility with SugarMunch version; `PluginSecurity.validateMinimumRequirements` enforces them.

## Live ops (optional)

- **URL:** Configured in [LiveOpsManager](app/src/main/kotlin/com/sugarmunch/app/events/LiveOpsManager.kt). JSON can drive feature flags, events, or A/B config.
