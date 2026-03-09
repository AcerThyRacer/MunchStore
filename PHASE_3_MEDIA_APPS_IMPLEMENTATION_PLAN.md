# Phase 3 - Media + Creation App Suite Implementation Plan

This document defines the execution approach for the "Media + Creation" suite inside the SugarMunch workspace. The suite is implemented as standalone Android app forks that are distributed through the existing SugarMunch APK catalog instead of being merged into the root Gradle project.

## 1. Delivery Model

### Workspace Layout

- `SugarMunch/` remains the host app, catalog, and release metadata source.
- `SugarMunch/SugarBeats/` becomes the branded music-player fork.
- `SugarMunch/GummyGallery/` becomes the branded gallery fork.
- `SugarMunch/CandyCam/` becomes the branded camera fork.

### Why This Model

- Upstream sync stays manageable because each fork is isolated.
- The existing catalog pipeline already supports external APK delivery.
- The root project avoids extra module complexity across phone, TV, and Wear targets.
- Each app can ship on its own schedule while still appearing as part of the SugarMunch family.

## 2. App Targets

### A. SugarBeats

- **Upstream Base:** [Fossify Music](https://github.com/FossifyOrg/Music)
- **Package / Brand:** `com.sugarmunch.sugarbeats`
- **Primary Differentiators:**
  - Candy-themed color system, gradients, surfaces, and launcher assets
  - Playback-reactive candy particles
  - Album-art-driven accent color shifts mapped to the SugarMunch palette

### B. GummyGallery

- **Upstream Base:** [Fossify Gallery](https://github.com/FossifyOrg/Gallery)
- **Package / Brand:** `com.sugarmunch.gummygallery`
- **Primary Differentiators:**
  - Shared candy visual system aligned with SugarBeats
  - Jelly-style grid and detail transitions
  - Seasonal sticker and media enhancement entry points

### C. CandyCam

- **Upstream Base:** Open Camera or a modern camera alternative if Open Camera proves too expensive to re-theme
- **Package / Brand:** `com.sugarmunch.candycam`
- **Primary Differentiators:**
  - Candy filter packs
  - Theme-preview overlays
  - Live sticker and AR-style visuals as a follow-up milestone after baseline theming is stable

## 3. Reusable SugarMunch Sources

The host app already contains the core systems needed to guide the visual language of the forked apps:

- `app/src/main/kotlin/com/sugarmunch/app/theme/engine/ThemeManager.kt`
- `app/src/main/kotlin/com/sugarmunch/app/theme/components/AnimatedThemeBackground.kt`
- `app/src/main/kotlin/com/sugarmunch/app/ui/particles/ParticleSystemEngine.kt`
- `app/src/main/kotlin/com/sugarmunch/app/theme/reactive/MusicReactiveTheme.kt`
- `app/src/main/kotlin/com/sugarmunch/app/ui/adaptive/AudioVisualizerEngine.kt`
- `docs/sugarmunch-apps.json`

These files act as the design reference and feature reference for Phase 3, but the first implementation should copy or adapt only the minimum pieces needed to deliver a stable SugarBeats slice.

## 4. Execution Strategy

### Step 1. Clone And Stabilize

For each target app:

1. Clone upstream into its own sibling directory.
2. Rename package, app label, and launcher branding.
3. Align Gradle and SDK settings with the current SugarMunch environment where required.
4. Verify the renamed app builds before any visual customization.

### Step 2. Apply Candy Brand Layer

For each target app:

1. Replace launcher branding, app name, and primary accent colors.
2. Introduce candy gradients, rounded surfaces, and typography updates.
3. Restyle the highest-value surfaces first: main activity chrome, top bars, cards, dialogs, and primary buttons.
4. Keep the first skin thin and maintainable; avoid broad architectural rewrites.

### Step 3. Add One Signature Twist Per App

- **SugarBeats:** Music-reactive candy particles and album-art-driven palette changes.
- **GummyGallery:** Elastic content transitions and sticker-ready hooks.
- **CandyCam:** Color-filter overlays before any real-time AR expansion.

### Step 4. Publish Through SugarMunch

1. Build release-ready APK artifacts for each app.
2. Add or update entries in `docs/sugarmunch-apps.json`.
3. Supply metadata such as `accentColor`, `badge`, `featured`, `previewUrl`, `components`, and `trailerUrl` when available.
4. Verify the SugarMunch catalog renders the new app cards correctly.

## 5. First Milestone: SugarBeats Vertical Slice

The first completed milestone must prove the full Phase 3 loop end to end:

1. Clone Fossify Music into `SugarBeats/`.
2. Rename the package and application branding.
3. Get a successful debug build.
4. Apply the first candy visual layer to core screens.
5. Implement a real playback-reactive feature.
6. Add SugarBeats to `docs/sugarmunch-apps.json`.
7. Update repository docs with the release and integration workflow.

## 6. Repeatable Fork Playbook

Every future Phase 3 fork follows the same baseline recipe:

1. **Baseline clone:** upstream checkout and clean local build.
2. **Identity pass:** package rename, app name, icons, strings, manifests.
3. **Compatibility pass:** Gradle, SDK, and dependency alignment.
4. **Candy pass:** shared colors, shapes, gradients, and high-value screen polish.
5. **Signature pass:** one standout feature unique to the app category.
6. **Catalog pass:** publish metadata and APK through SugarMunch.
7. **Stretch pass:** advanced animations, AR, stickers, or heavier media effects only after the baseline slice is stable.

## 7. Guardrails

- Do not add the forked apps as modules in `settings.gradle.kts`.
- Do not attempt a shared internal UI library before the first SugarBeats slice is validated.
- Validate renamed upstream builds before adding candy effects.
- Keep Phase 3 milestone 1 focused on one production-quality differentiator instead of several experimental ones.
- Treat AR-heavy CandyCam features as milestone 2 work unless the baseline render pipeline proves trivial to extend.

## 8. Immediate Priorities

1. Complete the SugarBeats vertical slice.
2. Document the repeatable fork strategy for GummyGallery and CandyCam.
3. Defer deeper cross-app integration until SugarBeats is shipping cleanly through the catalog.
