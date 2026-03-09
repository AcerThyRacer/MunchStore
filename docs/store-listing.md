# Store Listing Readiness

For publishing the main SugarMunch app to Google Play (or other stores), use this checklist and the `fastlane/metadata` structure.

## Metadata

- **Short description:** 80 characters max. Example: "Candy-themed app catalog and system-wide effects. Browse, install, and customize."
- **Full description:** 4000 characters max. Describe catalog, FAB, effects, themes, backup, and that APKs are from a JSON manifest.
- **Privacy policy URL:** Required. Host a policy (e.g. GitHub Pages) and add the URL in Play Console and in app Settings (see `TvSettingsScreen` and main Settings).
- **Data safety:** Declare what data is collected (e.g. Firebase anonymous ID, crash data). No user content or precise location unless used.

## Screenshots

Produce 2–8 screenshots and place under:

- **Phone:** `fastlane/metadata/android/en-US/images/phoneScreenshots/`  
  Recommended: 1080 x 1920 px (or 1080 x 2340 for full screen).
- **7" tablet (optional):** `fastlane/metadata/android/en-US/images/sevenInchScreenshots/`  
  Recommended: 1200 x 1920 px.

Suggested screens: Catalog, App detail, Effects/FAB, Theme settings, Shop or Rewards.

## Fastlane

The repo has `fastlane/metadata/android/en-US/images/` for the main app. Add:

- `title.txt` – 30 chars (app name).
- `short_description.txt` – 80 chars.
- `full_description.txt` – full description.

Then use `fastlane supply` (or your CI) to upload metadata and screenshots to Play Console.

## App signing

For store uploads, configure release signing (see `app/build.gradle.kts`). Do not rely on debug signing for release builds in CI; require `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, and `KEY_PASSWORD` so the build fails if not set.
