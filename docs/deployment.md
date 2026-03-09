# Deployment and Release Runbook

## Release build

1. **Version:** Update `versionCode` and `versionName` in `app/build.gradle.kts`.
2. **Secrets:** Ensure `KEYSTORE_*` env vars or GitHub secrets are set (see [SECURING_RELEASE.md](SECURING_RELEASE.md)).
3. **Build:** `./gradlew assembleRelease`
4. **Output:** `app/build/outputs/apk/release/app-release.apk`

## ProGuard and Crashlytics

- Mapping file: `app/build/outputs/mapping/release/mapping.txt`
- Upload this file to Firebase Crashlytics (e.g. via CI or manual) so stack traces are deobfuscated.
- In CI, add a step after `assembleRelease` to upload the mapping file using the Crashlytics Gradle plugin or REST API.

## Tagged release (CI)

- Pushing a tag `v*` triggers `.github/workflows/release.yml`.
- The workflow builds a release APK and attaches it to a GitHub Release.
- Ensure keystore secrets are configured in the repo for signed builds.

## Rollback

- No server-side rollback (app is client-only). For a bad release, ship a new version that reverts or fixes the issue.
