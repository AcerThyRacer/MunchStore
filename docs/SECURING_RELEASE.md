# Securing SugarMunch for Release

Before shipping to production, complete these steps.

## 1. Certificate pinning

- **File:** `app/src/main/res/xml/network_security_config.xml`
- GitHub (raw.githubusercontent.com) pins are set; rotate before expiration (see `<pin-set expiration="...">`).
- To regenerate GitHub pins: run SSL Labs against `raw.githubusercontent.com` or:
  `openssl s_client -connect raw.githubusercontent.com:443 -servername raw.githubusercontent.com 2>/dev/null | openssl x509 -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64`
- Firebase/Google use system CAs (no pins in config).

## 2. Firebase

- **File:** `app/google-services.json`
- Replace placeholder values with your Firebase project:
  - `YOUR_PROJECT_NUMBER`, `your-project-id`, `YOUR_APP_ID`, `YOUR_API_KEY`
- Do not commit real API keys to public repos; use CI secrets or a private config.

## 3. Release signing

- **Local:** Set env vars or `gradle.properties` (do not commit secrets):
  - `KEYSTORE_PATH` – path to release keystore
  - `KEYSTORE_PASSWORD`
  - `KEY_ALIAS` (default: `sugarmunch`)
  - `KEY_PASSWORD`
- **CI:** Add these GitHub Actions secrets to enable signed release APKs:
  - `KEYSTORE_BASE64` – base64-encoded release keystore file
  - `KEYSTORE_PASSWORD`
  - `KEY_ALIAS`
  - `KEY_PASSWORD`
  If `KEYSTORE_BASE64` is not set, the workflow still runs but the APK is debug-signed.
- When signing env vars are set (locally or in CI), `assembleRelease` uses the release keystore; otherwise it falls back to debug signing.

## 4. Logging strategy

- **SecureLogger:** Use for all application logs. In release, verbose/debug are suppressed via [SecurityConfig.ENABLE_VERBOSE_LOGGING](app/src/main/kotlin/com/sugarmunch/app/security/SecurityConfig.kt).
- **Crashlytics:** Uncaught exceptions and non-fatals are reported via [GlobalExceptionHandler](app/src/main/kotlin/com/sugarmunch/app/crash/GlobalExceptionHandler.kt) and [CrashReportingManager](app/src/main/kotlin/com/sugarmunch/app/crash/CrashReportingManager.kt). Do not log passwords or tokens.
- **ProGuard mapping:** Upload `app/build/outputs/mapping/release/mapping.txt` to Crashlytics after each release so stack traces are readable (see [deployment.md](deployment.md)).
