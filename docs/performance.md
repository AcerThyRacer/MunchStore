# Performance

## Targets

- **UI:** Aim for 60fps (or 90fps on capable devices) on list scroll and effect/theme animations.
- **Startup:** Use baseline profile (`app/src/main/baseline-prof.txt`) for critical path; regenerate when adding new startup code.
- **Memory:** LeakCanary is enabled in debug; fix any reported leaks before release.

## Workers

- [CacheWorker](app/src/main/kotlin/com/sugarmunch/app/work/CacheWorker.kt) and [BackgroundSyncWorker](app/src/main/kotlin/com/sugarmunch/app/work/BackgroundSyncWorker.kt) run in the background. Prefer `Constraints` (e.g. battery not low, network type) to avoid redundant work.
- Clan and trade workers: ensure they use appropriate constraints and backoff.

## Profiling

- Use Android Studio Profiler for CPU and memory.
- Compose recomposition: use layout inspector and Compose compiler reports to minimize unnecessary recomposition.
