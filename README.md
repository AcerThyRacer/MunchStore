# SugarMunch

**Live, Life, Love**

A candy-themed Android app that distributes modded APKs (your own builds and modified open-source apps) and adds a system-wide floating action button (FAB) to control **Sugarrush** and many visual, sound, and haptic effects.

## Features

- **APK catalog** – Browse and install modded APKs. The app list is loaded from a JSON manifest in this repo; APK files are hosted on GitHub Releases.
- **System-wide FAB** – A draggable candy-styled FAB overlay. Tap to expand and toggle effects.
- **Effects** – Sugarrush (gradient overlay + haptic), Rainbow Tint, Mint Wash, Caramel Dim, Candy Confetti, Heartbeat haptic, and more.

## Build

- **Requirements:** Android Studio or CLI with JDK 17+, Android SDK (compileSdk 34, minSdk 24).
- **Build:** Open the project in Android Studio and sync (this generates the Gradle wrapper if missing), then run:  
  `./gradlew assembleDebug`  
  Output: `app/build/outputs/apk/debug/app-debug.apk`
- **Run on device/emulator:** Enable **Display over other apps** (overlay permission) for SugarMunch in system settings so the FAB can appear.

## Adding APKs

1. Build or obtain your modded APK.
2. In this repo, go to **Releases** → create a new release (e.g. `v1.0.0`) and upload the APK as an asset.
3. Copy the asset’s **browser_download_url** (e.g. `https://github.com/your-username/SugarMunch/releases/download/v1.0.0/MyApp.apk`).
4. Edit `docs/sugarmunch-apps.json`: add an object to the `apps` array with:
   - `id` – unique id (e.g. `my-app`)
   - `name` – display name
   - `packageName` – Android package name
   - `description` – short description
   - `iconUrl` – (optional) URL to an icon image
   - `downloadUrl` – the release asset URL from step 3
   - `version` – version string
   - `source` – (optional) link or credit to original project
   - `category` – (optional) e.g. `"Video & Music"` or `"Other"`; apps are grouped by category in the catalog.
5. Commit and push. The app fetches this JSON from the default URL (see below); point it at your fork if needed.

**Sugartube** (YouTube/YouTube Music app, first in the “Video & Music” category) is built from the sibling `Sugartube/` project (fork of [NouTube](https://github.com/nonbili/NouTube)). Build that project (see Sugartube/README.md), upload the APK to a release, and set its `downloadUrl` in `docs/sugarmunch-apps.json`.

## Manifest URL

The app loads the catalog from:

`https://raw.githubusercontent.com/<owner>/<repo>/<branch>/docs/sugarmunch-apps.json`

Replace `<owner>/<repo>/<branch>` with your repo and branch (e.g. `your-username/SugarMunch/main`). The default in code is `sugarmunch/SugarMunch/main`; change `ManifestRepository.DEFAULT_MANIFEST_URL` to match your repo.

## License

GPL v3 (see LICENSE).
