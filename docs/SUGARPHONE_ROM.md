# SugarPhone ROM – Groundwork

This document outlines requirements and steps for a heavily integrated, customized Android ROM built around LollipopLauncher and the decentralized SugarMunch app store at the OS level.

## Goals

- **Default launcher**: LollipopLauncher as the only/system launcher.
- **Default install source**: SugarMunch store as the primary (or only) app installation path.
- **Theme sync**: System-level theme sync with SugarMunch dynamic themes (e.g. Midnight Mint, Sunrise Sherbet).
- **Reduced surface**: Disable or hide non-essential system apps; focus on SugarMunch ecosystem.

## Prerequisites

- AOSP or LineageOS (or similar) source tree.
- LollipopLauncher built as system app and set as `default_launcher` in overlay.
- SugarMunch app built as system/privileged app with install permissions.
- Contract for theme broadcast (e.g. `sugarmunch://theme?id=...`) from launcher to other apps.

## Build-time configuration

- **Build flag / variant**: e.g. `sugarphone` product flavor or `SUGARPHONE_ROM=true` in build config.
- In this variant:
  - Point default install source to SugarMunch (or restrict sideloading).
  - Include LollipopLauncher and SugarMunch in system image.
  - Optional: strip Google Play and other stores from the image.

## References

- [CROSS_APP_THEME_AND_PROFILES.md](CROSS_APP_THEME_AND_PROFILES.md) – Theme deep link and Flavor Profiles contract.
- LollipopLauncher repo (sibling or submodule).
- SugarMunch [architecture](architecture.md) and [API](openapi.yaml).

## Disclaimer

No actual ROM build or device images are produced in this repository. This document is for planning and third-party ROM maintainers.
