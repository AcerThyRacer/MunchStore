# ADR 0001: Canonical Theme Profile

## Status

Accepted

## Context

SugarMunch had multiple overlapping theme authoring and transport models:

- `CandyTheme` drove the live runtime
- `LayeredTheme` existed as an isolated layer system
- `CustomTheme` and `ThemeBuilderState` powered legacy builder export codes
- `CustomThemeSpec` and `ExportableTheme` powered the richer studio/editor path

That fragmentation made it difficult to add Phase 1 features consistently. Layers, typography, mesh gradients, per-app overrides, and import/export all needed one shared source of truth.

## Decision

Phase 1 converges theme authoring around `theme/profile/ThemeProfile.kt`.

`ThemeProfile` is now the canonical schema for:

- palette and resolved color roles
- background and mesh definitions
- layer ordering, opacity, and blend modes
- typography font sources and variable axis values
- particle and animation defaults
- versioned transport envelopes for import/export

`CandyTheme` remains the compatibility/runtime format, but it is treated as a resolved output from `ThemeProfile` instead of the authoring source.

## Consequences

- Editors, sharing, and runtime resolution can target one schema.
- Built-in presets and legacy exports can be migrated through mapper functions.
- Per-app overrides can reference stable profile IDs instead of ad-hoc theme payloads.
- Future transport migrations can happen at the envelope/profile version layer without rewriting the runtime again.
