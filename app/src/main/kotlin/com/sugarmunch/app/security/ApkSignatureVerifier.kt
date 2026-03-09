package com.sugarmunch.app.security

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.content.pm.SigningInfo
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.annotation.VisibleForTesting
import java.io.File
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.jar.JarFile

/**
 * APK Signature Verification System
 *
 * Ensures downloaded APKs are from trusted sources by verifying:
 * 1. Certificate signatures against trusted certificates
 * 2. APK integrity (valid ZIP structure)
 * 3. Package name matches expected values
 *
 * Security levels:
 * - STRICT: Only allow explicitly trusted certificates (production)
 * - WARN: Allow unknown certificates but log warnings (development)
 * - DISABLED: Skip verification (testing only)
 *
 * ## Hilt Migration Note
 * This class has been migrated from a singleton pattern to Hilt dependency injection.
 * Inject this class using:
 * ```
 * @Inject lateinit var apkSignatureVerifier: ApkSignatureVerifier
 * ```
 * In a Hilt module, provide it with [@ApplicationContext](file:///home/ace/Downloads/SugarMunch/app/src/main/kotlin/com/sugarmunch/app/ui/theme/Color.kt#L5-L5):
 * ```
 * @Provides
 * @Singleton
 * fun provideApkSignatureVerifier(@ApplicationContext context: Context): ApkSignatureVerifier {
 *     return ApkSignatureVerifier(context)
 * }
 * ```
 */
class ApkSignatureVerifier constructor(
    private val context: Context,
    private val verificationMode: VerificationMode = VerificationMode.fromBuildConfig()
) {

    private val packageManager = context.packageManager

    // ═════════════════════════════════════════════════════════════
    // CONSTANTS
    // ═════════════════════════════════════════════════════════════

    companion object {
        private const val TAG = "ApkSignatureVerifier"

        /**
         * Known trusted certificate hashes (SHA-256, Base64 encoded).
         *
         * To add your release certificate:
         * 1. Build release APK: ./gradlew assembleRelease
         * 2. Extract certificate hash:
         *    keytool -printcert -jarfile app/build/outputs/apk/release/app-release.apk | grep SHA256
         * 3. Convert to Base64 or use the hex value directly
         * 4. Add to SecurityConfig.TRUSTED_CERTIFICATE_HASHES
         *
         * Common certificate hashes to consider:
         * - Google Play signing certificate (if using Play App Signing)
         * - Your release keystore certificate
         * - Certificates from trusted open-source projects
         */
        @VisibleForTesting
        internal val DEBUG_CERTIFICATE_HASHES = mutableSetOf<String>()

        /**
         * Verification result codes for better error handling.
         */
        object ResultCode {
            const val SUCCESS = 0
            const val SUCCESS_UNTRUSTED_DEBUG = 1
            const val ERROR_FILE_NOT_FOUND = -1
            const val ERROR_INVALID_EXTENSION = -2
            const val ERROR_NO_CERTIFICATES = -3
            const val ERROR_UNTRUSTED_CERTIFICATE = -4
            const val ERROR_INTEGRITY_FAILED = -5
            const val ERROR_PACKAGE_NOT_FOUND = -6
            const val ERROR_VERIFICATION_EXCEPTION = -7
        }
    }

    /**
     * Verify APK signature before installation.
     *
     * @param apkFile The APK file to verify
     * @param expectedPackageName Optional expected package name for additional validation
     * @return VerificationResult with detailed status
     */
    fun verifyApk(
        apkFile: File,
        expectedPackageName: String? = null
    ): VerificationResult {
        return try {
            // Validate file exists
            if (!apkFile.exists()) {
                return VerificationResult.Error(
                    "APK file does not exist",
                    ResultCode.ERROR_FILE_NOT_FOUND
                )
            }

            // Validate file extension
            if (!apkFile.name.endsWith(".apk", ignoreCase = true)) {
                return VerificationResult.Error(
                    "Invalid file extension: ${apkFile.extension}",
                    ResultCode.ERROR_INVALID_EXTENSION
                )
            }

            // Validate file size
            val fileSize = apkFile.length()
            if (!SecurityConfig.isValidApkSize(fileSize)) {
                return VerificationResult.Error(
                    "APK size ($fileSize bytes) exceeds maximum allowed (${SecurityConfig.MAX_APK_SIZE_BYTES} bytes)",
                    ResultCode.ERROR_FILE_NOT_FOUND
                )
            }

            // Extract and verify certificates
            val certificates = extractCertificates(apkFile)
            if (certificates.isEmpty()) {
                return VerificationResult.Error(
                    "No certificates found in APK",
                    ResultCode.ERROR_NO_CERTIFICATES
                )
            }

            // Calculate certificate hashes
            val certHashes = certificates.map { cert ->
                getCertificateHash(cert) to cert
            }

            // Verify against trusted certificates
            val verificationStatus = verifyCertificates(certHashes.map { it.first }, expectedPackageName)

            // Verify package name if provided
            if (expectedPackageName != null) {
                val apkInfo = getApkInfo(apkFile)
                if (apkInfo?.packageName != expectedPackageName) {
                    return VerificationResult.Error(
                        "Package name mismatch: expected $expectedPackageName, got ${apkInfo?.packageName}",
                        ResultCode.ERROR_UNTRUSTED_CERTIFICATE
                    )
                }
            }

            // Verify APK integrity
            val integrityVerified = verifyApkIntegrity(apkFile)
            if (!integrityVerified) {
                return VerificationResult.Error(
                    "APK integrity check failed - file may be corrupted",
                    ResultCode.ERROR_INTEGRITY_FAILED
                )
            }

            // Determine final result based on verification mode
            when {
                verificationStatus.isTrusted -> {
                    VerificationResult.Success(
                        isTrusted = true,
                        certificateHashes = certHashes.map { it.first },
                        certificateCount = certificates.size,
                        integrityVerified = integrityVerified,
                        resultCode = ResultCode.SUCCESS
                    )
                }
                verificationStatus.allowInDebug -> {
                    Log.w(
                        TAG,
                        "APK has untrusted signature but allowed in debug mode: ${verificationStatus.warningMessage}"
                    )
                    VerificationResult.Success(
                        isTrusted = false,
                        certificateHashes = certHashes.map { it.first },
                        certificateCount = certificates.size,
                        integrityVerified = integrityVerified,
                        resultCode = ResultCode.SUCCESS_UNTRUSTED_DEBUG,
                        warning = verificationStatus.warningMessage
                    )
                }
                else -> {
                    Log.e(TAG, "APK signature verification failed: ${verificationStatus.warningMessage}")
                    VerificationResult.Error(
                        verificationStatus.warningMessage,
                        ResultCode.ERROR_UNTRUSTED_CERTIFICATE
                    )
                }
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception during APK verification", e)
            VerificationResult.Error(
                "Security violation: ${e.message}",
                ResultCode.ERROR_UNTRUSTED_CERTIFICATE
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error verifying APK", e)
            VerificationResult.Error(
                "Verification failed: ${e.message}",
                ResultCode.ERROR_VERIFICATION_EXCEPTION
            )
        }
    }

    /**
     * Verify installed app signature.
     *
     * @param packageName The package name to verify
     * @return VerificationResult with detailed status
     */
    fun verifyInstalledApp(packageName: String): VerificationResult {
        return try {
            val packageInfo = getPackageInfoWithSignatures(packageName)
                ?: return VerificationResult.Error(
                    "Package not found: $packageName",
                    ResultCode.ERROR_PACKAGE_NOT_FOUND
                )

            val signatures = extractSignatures(packageInfo)
            if (signatures.isEmpty()) {
                return VerificationResult.Error(
                    "No signatures found for package: $packageName",
                    ResultCode.ERROR_NO_CERTIFICATES
                )
            }

            val certHashes = signatures.map { sig ->
                getSignatureHash(sig)
            }

            val verificationStatus = verifyCertificates(certHashes, packageName)

            when {
                verificationStatus.isTrusted -> {
                    VerificationResult.Success(
                        isTrusted = true,
                        certificateHashes = certHashes,
                        certificateCount = signatures.size,
                        integrityVerified = true,
                        resultCode = ResultCode.SUCCESS
                    )
                }
                verificationStatus.allowInDebug -> {
                    VerificationResult.Success(
                        isTrusted = false,
                        certificateHashes = certHashes,
                        certificateCount = signatures.size,
                        integrityVerified = true,
                        resultCode = ResultCode.SUCCESS_UNTRUSTED_DEBUG,
                        warning = verificationStatus.warningMessage
                    )
                }
                else -> {
                    VerificationResult.Error(
                        verificationStatus.warningMessage,
                        ResultCode.ERROR_UNTRUSTED_CERTIFICATE
                    )
                }
            }

        } catch (e: PackageManager.NameNotFoundException) {
            VerificationResult.Error(
                "Package not found: $packageName",
                ResultCode.ERROR_PACKAGE_NOT_FOUND
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying installed app: $packageName", e)
            VerificationResult.Error(
                "Verification failed: ${e.message}",
                ResultCode.ERROR_VERIFICATION_EXCEPTION
            )
        }
    }

    /**
     * Get signature hash for a package.
     */
    fun getPackageSignatureHash(packageName: String): String? {
        return try {
            val packageInfo = getPackageInfoWithSignatures(packageName)
            val signatures = packageInfo?.let { extractSignatures(it) } ?: return null
            signatures.firstOrNull()?.let { getSignatureHash(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting signature hash for: $packageName", e)
            null
        }
    }

    /**
     * Compare signatures of two packages.
     */
    fun haveSameSignature(package1: String, package2: String): Boolean {
        val hash1 = getPackageSignatureHash(package1)
        val hash2 = getPackageSignatureHash(package2)
        return hash1 != null && hash1 == hash2
    }

    /**
     * Verify a batch of APKs.
     * Useful for verifying multiple downloads at once.
     */
    fun verifyApkBatch(apkFiles: Collection<File>): BatchVerificationResult {
        val results = apkFiles.associateWith { verifyApk(it) }
        val successCount = results.count { it.value is VerificationResult.Success }
        val failureCount = results.size - successCount

        return BatchVerificationResult(
            total = apkFiles.size,
            successCount = successCount,
            failureCount = failureCount,
            results = results
        )
    }

    // ═════════════════════════════════════════════════════════════
    // PRIVATE HELPER METHODS
    // ═════════════════════════════════════════════════════════════

    private fun verifyCertificates(certHashes: List<String>, packageName: String? = null): VerificationStatus {
        // Validate hash format first
        val invalidHashes = certHashes.filter { !SecurityConfig.isValidCertificateHashFormat(it) }
        if (invalidHashes.isNotEmpty()) {
            Log.w(TAG, "Some certificate hashes have invalid format: ${invalidHashes.map { it.take(8) }}")
        }

        // Check each certificate against trusted lists
        val trustResults = certHashes.map { hash ->
            val isTrusted = SecurityConfig.isCertificateTrusted(hash, packageName)
            Triple(hash, isTrusted, SecurityConfig.CERTIFICATE_VALIDATION_MODE)
        }

        val trustedHashes = trustResults.filter { it.second }
        if (trustedHashes.isNotEmpty()) {
            Log.d(TAG, "Found ${trustedHashes.size} trusted certificate(s)")
            return VerificationStatus(isTrusted = true)
        }

        // No trusted certificates found - check validation mode
        val mode = SecurityConfig.CERTIFICATE_VALIDATION_MODE
        val allowInDebug = mode == SecurityConfig.CertificateValidationMode.PERMISSIVE ||
            mode == SecurityConfig.CertificateValidationMode.DISABLED ||
            verificationMode == VerificationMode.WARN ||
            verificationMode == VerificationMode.DEBUG_ALLOW_UNTRUSTED

        val warningMessage = buildString {
            append("Untrusted certificate(s): [")
            append(certHashes.joinToString(", ") { it.take(16) + "..." })
            append("]")
            if (packageName != null) {
                append(" for package: $packageName")
            }
            append(" | Validation mode: $mode")
            if (allowInDebug) {
                append(" | Allowed in current mode")
            }
            append(" | Add certificate hash to SecurityConfig.TRUSTED_CERTIFICATE_HASHES to trust this APK")
        }

        Log.w(TAG, warningMessage)

        return VerificationStatus(
            isTrusted = false,
            allowInDebug = allowInDebug,
            warningMessage = warningMessage
        )
    }

    private fun getPackageInfoWithSignatures(packageName: String) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageManager.getPackageInfo(
            packageName,
            PackageManager.GET_SIGNING_CERTIFICATES
        )
    } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(
            packageName,
            PackageManager.GET_SIGNATURES
        )
    }

    private fun extractSignatures(packageInfo: android.content.pm.PackageInfo): Array<Signature> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.signingInfo?.apkContentsSigners
                ?: packageInfo.signingInfo?.signingCertificateHistory
                ?: emptyArray()
        } else {
            @Suppress("DEPRECATION")
            packageInfo.signatures?.toTypedArray() ?: emptyArray()
        }
    }

    private fun extractCertificates(apkFile: File): List<X509Certificate> {
        val certificates = mutableListOf<X509Certificate>()
        val certificateFactory = CertificateFactory.getInstance("X.509")

        try {
            JarFile(apkFile).use { jarFile ->
                // Extract certificates from META-INF
                jarFile.entries().asSequence()
                    .filter {
                        !it.isDirectory &&
                        (it.name.endsWith(".RSA") || it.name.endsWith(".DSA") || it.name.endsWith(".EC"))
                    }
                    .forEach { entry ->
                        try {
                            jarFile.getInputStream(entry).use { input ->
                                val cert = certificateFactory.generateCertificate(input) as X509Certificate
                                certificates.add(cert)
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to parse certificate: ${entry.name}", e)
                        }
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting certificates from APK", e)
        }

        return certificates
    }

    private fun getCertificateHash(cert: X509Certificate): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val hash = md.digest(cert.encoded)
            Base64.encodeToString(hash, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Error computing certificate hash", e)
            ""
        }
    }

    private fun getSignatureHash(signature: Signature): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val hash = md.digest(signature.toByteArray())
            Base64.encodeToString(hash, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Error computing signature hash", e)
            ""
        }
    }

    private fun verifyApkIntegrity(apkFile: File): Boolean {
        return try {
            JarFile(apkFile).use { jarFile ->
                // Verify ZIP structure is valid
                jarFile.entries().asSequence().forEach { entry ->
                    // Try to read entry name - will throw if corrupted
                    entry.name
                }
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "APK integrity check failed", e)
            false
        }
    }

    /**
     * Get APK info without installing.
     */
    fun getApkInfo(apkFile: File): ApkInfo? {
        return try {
            val packageInfo = packageManager.getPackageArchiveInfo(
                apkFile.absolutePath,
                PackageManager.GET_ACTIVITIES or
                PackageManager.GET_SIGNATURES or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    PackageManager.GET_SIGNING_CERTIFICATES
                } else {
                    0
                }
            )

            packageInfo?.let {
                ApkInfo(
                    packageName = it.packageName,
                    versionName = it.versionName,
                    versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        it.longVersionCode
                    } else {
                        @Suppress("DEPRECATION")
                        it.versionCode.toLong()
                    },
                    applicationName = it.applicationInfo?.loadLabel(packageManager)?.toString(),
                    signatureHash = extractSignatures(it).firstOrNull()?.let { sig ->
                        getSignatureHash(sig)
                    }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting APK info", e)
            null
        }
    }
}

/**
 * Verification mode for APK signature checking.
 */
enum class VerificationMode {
    /**
     * Strict mode - only trusted certificates allowed.
     * Use in production builds.
     */
    STRICT,

    /**
     * Warning mode - allow untrusted but log warnings.
     * Use in debug builds for testing.
     */
    WARN,

    /**
     * Debug mode - allow all certificates.
     * Use only for development/testing.
     */
    DEBUG_ALLOW_UNTRUSTED;

    companion object {
        fun fromBuildConfig(): VerificationMode {
            return when {
                SecurityConfig.STRICT_SIGNATURE_VERIFICATION -> STRICT
                else -> WARN
            }
        }
    }
}

/**
 * Internal verification status.
 */
internal data class VerificationStatus(
    val isTrusted: Boolean = false,
    val allowInDebug: Boolean = false,
    val warningMessage: String = ""
)

/**
 * Verification result sealed class.
 */
sealed class VerificationResult {
    abstract val resultCode: Int

    data class Success(
        val isTrusted: Boolean,
        val certificateHashes: List<String>,
        val certificateCount: Int,
        val integrityVerified: Boolean,
        override val resultCode: Int = ApkSignatureVerifier.ResultCode.SUCCESS,
        val warning: String? = null
    ) : VerificationResult()

    data class Error(
        val message: String,
        override val resultCode: Int = ApkSignatureVerifier.ResultCode.ERROR_VERIFICATION_EXCEPTION
    ) : VerificationResult()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
}

/**
 * Batch verification result.
 */
data class BatchVerificationResult(
    val total: Int,
    val successCount: Int,
    val failureCount: Int,
    val results: Map<File, VerificationResult>
) {
    val allSuccessful: Boolean get() = failureCount == 0
    val successRate: Float get() = if (total > 0) successCount.toFloat() / total else 0f
}

/**
 * APK information data class.
 */
data class ApkInfo(
    val packageName: String,
    val versionName: String?,
    val versionCode: Long,
    val applicationName: String?,
    val signatureHash: String? = null
) {
    /**
     * Check if this APK's signature is trusted.
     */
    fun isSignatureTrusted(): Boolean {
        return signatureHash?.let { SecurityConfig.isCertificateTrusted(it, packageName) } ?: false
    }
}
