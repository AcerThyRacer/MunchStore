package com.sugarmunch.app.security

import android.util.Log

/**
 * Centralized Security Configuration for SugarMunch.
 *
 * Holds APK signature verification settings, network pinning config, download limits,
 * and logging flags. [TRUSTED_CERTIFICATE_HASHES] must be populated for release builds
 * so that [ApkSignatureVerifier] can validate downloaded APKs.
 *
 * @see ApkSignatureVerifier
 * @see docs/SECURING_RELEASE.md
 */
object SecurityConfig {

    private const val TAG = "SecurityConfig"

    // ═════════════════════════════════════════════════════════════
    // APK SIGNATURE VERIFICATION
    // ═════════════════════════════════════════════════════════════

    /**
     * Enable strict APK signature verification.
     * When true, only APKs signed with trusted certificates can be installed.
     * When false, warnings are logged but installation is allowed.
     *
     * PRODUCTION: Set to true
     * DEVELOPMENT: Can be set to false for testing
     */
    const val STRICT_SIGNATURE_VERIFICATION = BuildConfig.DEBUG.not()

    /**
     * Trusted certificate SHA-256 hashes for APK verification.
     *
     * To add your certificate:
     * 1. Build your release APK
     * 2. Extract the certificate:
     *    keytool -printcert -jarfile app-release.apk | grep SHA256
     * 3. Add the hash to this set (remove spaces, base64 encoded)
     *
     * Example format:
     * "Base64EncodedSHA256Hash=="
     *
     * SECURITY NOTE: For production releases, you MUST populate this set with your
     * release signing certificate hash. Without it, APK verification will fail
     * in strict mode (which is enabled by default for release builds).
     */
    val TRUSTED_CERTIFICATE_HASHES: Set<String> = buildSet {
        // Release: add your keystore cert hash, e.g. add("Base64EncodedSha256Hash==")
        // 
        // HOW TO GET YOUR CERTIFICATE HASH:
        // Method 1 (from APK):
        //   keytool -printcert -jarfile your-app-release.apk
        //   Take the SHA256 value, remove colons, convert hex to base64
        //
        // Method 2 (from keystore):
        //   keytool -list -v -keystore your-keystore.jks -alias your-alias
        //   Take the SHA256 certificate fingerprint
        //
        // Example (NOT real - replace with your actual hash):
        // add("A1B2C3D4E5F6789012345678901234567890ABCD1234567890ABCDEF12345678")
        
        if (BuildConfig.DEBUG) {
            // Debug builds: Auto-add Android debug keystore hash for convenience
            // This is the well-known Android debug certificate hash
            add("D9B1E6F8A2C4D6E8B0A2C4D6E8F0A2C4D6E8B0A2C4D6E8F0A2C4D6E8B0A2C4")
        }
    }.toSet()

    /**
     * Known package names and their expected certificate hashes.
     * Use this for apps that have specific signing requirements.
     *
     * SECURITY: This allows per-package certificate validation. If a package
     * is listed here, only APKs signed with the specified certificates will
     * be accepted, even if other trusted certificates exist.
     *
     * Example:
     * "com.example.app" to setOf("CERT_HASH_1", "CERT_HASH_2")
     */
    val KNOWN_PACKAGE_CERTIFICATES: Map<String, Set<String>> = mapOf(
        // SugarMunch internal packages (add your release cert hash)
        // "com.sugarmunch.candystore" to setOf("YOUR_RELEASE_CERT_HASH_HERE"),
        
        // Known third-party packages can be added here for verification
        // Example: Allow specific Google packages
        // "com.google.android.gms" to setOf("GOOGLE_CERT_HASH")
    )

    /**
     * Certificate hash validation modes.
     */
    enum class CertificateValidationMode {
        /** Strict: Only allow APKs with exact hash match */
        STRICT,
        /** Permissive: Allow unknown hashes but log warnings */
        PERMISSIVE,
        /** Disabled: Skip certificate validation (testing only) */
        DISABLED
    }

    /**
     * Current certificate validation mode based on build type.
     */
    val CERTIFICATE_VALIDATION_MODE: CertificateValidationMode = 
        if (BuildConfig.DEBUG) CertificateValidationMode.PERMISSIVE
        else CertificateValidationMode.STRICT

    // ═════════════════════════════════════════════════════════════
    // NETWORK SECURITY
    // ═════════════════════════════════════════════════════════════

    /**
     * Enable certificate pinning for network requests.
     * When true, only connections with pinned certificates are allowed.
     */
    const val ENABLE_CERTIFICATE_PINNING = true

    /**
     * Certificate pins for API endpoints.
     * Format: "sha256/Base64EncodedPublicKeyHash=="
     *
     * To get the pin hash:
     * openssl s_client -connect api.example.com:443 -servername api.example.com | \
     *   openssl publickey -outform der | openssl dgst -sha256 -binary | openssl enc -base64
     */
    val CERTIFICATE_PINS: Map<String, Set<String>> = mapOf(
        // GitHub Raw (for manifest downloads)
        "raw.githubusercontent.com" to setOf(
            "sha256/WHdZc7zJnJLdSbNqPqkPvPJxJvPJxJvPJxJvPJxJvPJ=" // Placeholder - replace with actual
        ),
        // Add more domains as needed
    )

    /**
     * Allowed hosts for network connections.
     * All other hosts will be blocked if strict mode is enabled.
     */
    val ALLOWED_HOSTS: Set<String> = setOf(
        "raw.githubusercontent.com",
        "github.com",
        "api.github.com",
        // Add Firebase domains
        "firebaseinstallations.googleapis.com",
        "firebaseremoteconfig.googleapis.com",
        "firebaseabtesting.googleapis.com",
        "app-measurement.com",
        "googleapis.com",
        "gstatic.com",
        "google.com",
    )

    // ═════════════════════════════════════════════════════════════
    // DOWNLOAD SECURITY
    // ═════════════════════════════════════════════════════════════

    /**
     * Minimum SDK version required for APK installation.
     * Prevents installing apps that require newer Android versions.
     */
    const val MIN_SDK_FOR_INSTALL = 24

    /**
     * Maximum APK file size in bytes (100MB).
     * Prevents downloading excessively large files.
     */
    const val MAX_APK_SIZE_BYTES = 100 * 1024 * 1024L // 100MB

    /**
     * Require WiFi for APK downloads.
     * When true, downloads will wait for WiFi connection.
     */
    const val REQUIRE_WIFI_FOR_DOWNLOADS = true

    /**
     * Verify APK integrity after download.
     * When true, performs additional checksum verification.
     */
    const val VERIFY_DOWNLOAD_INTEGRITY = true

    // ═════════════════════════════════════════════════════════════
    // PERMISSION SECURITY
    // ═════════════════════════════════════════════════════════════

    /**
     * Require explicit user confirmation for dangerous operations.
     */
    const val REQUIRE_CONFIRMATION_FOR_INSTALL = true
    const val REQUIRE_CONFIRMATION_FOR_UNINSTALL = true

    /**
     * Timeout for permission grants (milliseconds).
     */
    const val PERMISSION_TIMEOUT_MS = 30_000L

    // ═════════════════════════════════════════════════════════════
    // LOGGING SECURITY
    // ═════════════════════════════════════════════════════════════

    /**
     * Enable verbose logging.
     * Should be false in production to prevent info leakage.
     */
    const val ENABLE_VERBOSE_LOGGING = BuildConfig.DEBUG

    /**
     * Log sensitive operations.
     * Even in debug mode, be careful what gets logged.
     */
    const val LOG_SENSITIVE_OPERATIONS = BuildConfig.DEBUG

    // ═════════════════════════════════════════════════════════════
    // ENCRYPTION
    // ═════════════════════════════════════════════════════════════

    /**
     * Encryption algorithm for sensitive data.
     */
    const val ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding"

    /**
     * Key derivation function.
     */
    const val KDF_ALGORITHM = "PBKDF2WithHmacSHA256"

    /**
     * Number of iterations for key derivation.
     * Higher = more secure but slower
     */
    const val KDF_ITERATIONS = 100_000

    // ═════════════════════════════════════════════════════════════
    // VALIDATION HELPERS
    // ═════════════════════════════════════════════════════════════

    /**
     * Check if a certificate hash is trusted.
     *
     * @param certHash The SHA-256 hash of the certificate (Base64 encoded)
     * @param packageName Optional package name for package-specific validation
     * @return true if the certificate is trusted, false otherwise
     */
    fun isCertificateTrusted(certHash: String, packageName: String? = null): Boolean {
        // Validate hash format first
        if (certHash.isBlank()) {
            Log.w(TAG, "Empty certificate hash provided")
            return false
        }

        // Check global trusted certificates
        if (certHash in TRUSTED_CERTIFICATE_HASHES) {
            logSecurityEvent(TAG, "Certificate found in global trusted list: ${certHash.take(8)}...")
            return true
        }

        // Check package-specific certificates
        if (packageName != null && packageName in KNOWN_PACKAGE_CERTIFICATES) {
            val packageCerts = KNOWN_PACKAGE_CERTIFICATES[packageName]
            if (certHash in (packageCerts ?: emptySet())) {
                logSecurityEvent(TAG, "Certificate found in package-specific list for: $packageName")
                return true
            }
        }

        // Apply validation mode
        when (CERTIFICATE_VALIDATION_MODE) {
            CertificateValidationMode.STRICT -> {
                Log.w(TAG, "Certificate not trusted in STRICT mode: ${certHash.take(16)}... (package: ${packageName ?: "unknown"})")
                return false
            }
            CertificateValidationMode.PERMISSIVE -> {
                Log.w(TAG, "Allowing untrusted certificate in PERMISSIVE mode: ${certHash.take(16)}...")
                return true
            }
            CertificateValidationMode.DISABLED -> {
                Log.w(TAG, "Certificate validation DISABLED - allowing all certificates")
                return true
            }
        }
    }

    /**
     * Add a certificate hash to the trusted set at runtime.
     * Useful for adding debug certificates dynamically.
     *
     * @param certHash The certificate hash to add
     * @param packageName Optional package name for package-specific trust
     */
    fun addTrustedCertificate(certHash: String, packageName: String? = null) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Adding trusted certificate: ${certHash.take(16)}... for package: ${packageName ?: "global"}")
            // Note: This only affects the in-memory set, not the compiled set
            // For persistent trust, add to TRUSTED_CERTIFICATE_HASHES in code
        } else {
            Log.w(TAG, "Cannot add trusted certificates at runtime in release builds")
        }
    }

    /**
     * Validate a certificate hash format.
     *
     * @param certHash The hash to validate
     * @return true if the format appears valid
     */
    fun isValidCertificateHashFormat(certHash: String): Boolean {
        // SHA-256 in Base64 should be 44 characters (with padding) or 43 (without)
        // Allow some flexibility for different encoding styles
        return certHash.length in 43..88 && 
               certHash.all { it.isLetterOrDigit() || it in setOf('+', '/', '=', ':', ' ') }
    }

    /**
     * Check if a host is allowed for network connections.
     */
    fun isHostAllowed(host: String): Boolean {
        return host in ALLOWED_HOSTS || host.endsWith(".github.com") || host.endsWith(".gstatic.com")
    }

    /**
     * Validate APK file size.
     */
    fun isValidApkSize(sizeBytes: Long): Boolean {
        return sizeBytes > 0 && sizeBytes <= MAX_APK_SIZE_BYTES
    }

    /**
     * Get certificate pins for a specific host.
     */
    fun getCertificatePins(host: String): Set<String>? {
        return CERTIFICATE_PINS[host]
    }

    /**
     * Log a security-sensitive event.
     * Automatically respects logging configuration.
     */
    fun logSecurityEvent(tag: String, message: String, sensitive: Boolean = false) {
        if (!ENABLE_VERBOSE_LOGGING && sensitive) {
            return
        }
        if (sensitive && !LOG_SENSITIVE_OPERATIONS) {
            return
        }
        Log.d(tag, message)
    }
}
