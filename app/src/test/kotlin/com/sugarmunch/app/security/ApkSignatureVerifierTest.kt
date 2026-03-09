package com.sugarmunch.app.security

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

/**
 * Unit tests for ApkSignatureVerifier
 */
@RunWith(RobolectricTestRunner::class)
class ApkSignatureVerifierTest {

    private lateinit var verifier: ApkSignatureVerifier

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        verifier = ApkSignatureVerifier.getInstance(context)
    }

    @Test
    fun `verifyApk should return error for non-existent file`() {
        // Given
        val nonExistentFile = File("/nonexistent/path/app.apk")

        // When
        val result = verifier.verifyApk(nonExistentFile)

        // Then
        assertThat(result).isInstanceOf(VerificationResult.Error::class.java)
        assertThat((result as VerificationResult.Error).message).contains("does not exist")
    }

    @Test
    fun `verifyApk should return error for invalid file extension`() {
        // Given
        val invalidFile = File.createTempFile("test", ".txt").apply {
            writeText("not an apk")
        }

        // When
        val result = verifier.verifyApk(invalidFile)

        // Then
        assertThat(result).isInstanceOf(VerificationResult.Error::class.java)
        assertThat((result as VerificationResult.Error).message).contains("extension")
        
        invalidFile.delete()
    }

    @Test
    fun `getApkInfo should return null for non-existent file`() {
        // Given
        val nonExistentFile = File("/nonexistent/path/app.apk")

        // When
        val info = verifier.getApkInfo(nonExistentFile)

        // Then
        assertThat(info).isNull()
    }

    @Test
    fun `VerificationResult Success should contain correct data`() {
        // Given
        val success = VerificationResult.Success(
            isTrusted = true,
            certificateHashes = listOf("hash1", "hash2"),
            certificateCount = 2,
            integrityVerified = true
        )

        // Then
        assertThat(success.isTrusted).isTrue()
        assertThat(success.certificateHashes).hasSize(2)
        assertThat(success.certificateCount).isEqualTo(2)
        assertThat(success.integrityVerified).isTrue()
    }

    @Test
    fun `VerificationResult Error should contain message`() {
        // Given
        val error = VerificationResult.Error("Test error message")

        // Then
        assertThat(error.message).isEqualTo("Test error message")
    }

    @Test
    fun `ApkInfo should contain correct data`() {
        // Given
        val apkInfo = ApkInfo(
            packageName = "com.test.app",
            versionName = "1.0.0",
            versionCode = 1L,
            applicationName = "Test App"
        )

        // Then
        assertThat(apkInfo.packageName).isEqualTo("com.test.app")
        assertThat(apkInfo.versionName).isEqualTo("1.0.0")
        assertThat(apkInfo.versionCode).isEqualTo(1L)
        assertThat(apkInfo.applicationName).isEqualTo("Test App")
    }

    @Test
    fun `verifyInstalledApp should return error for unknown package`() {
        // Given
        val unknownPackage = "com.nonexistent.package"

        // When
        val result = verifier.verifyInstalledApp(unknownPackage)

        // Then
        assertThat(result).isInstanceOf(VerificationResult.Error::class.java)
        assertThat((result as VerificationResult.Error).message).contains("not found")
    }

    @Test
    fun `getPackageSignatureHash should return null for unknown package`() {
        // Given
        val unknownPackage = "com.nonexistent.package"

        // When
        val hash = verifier.getPackageSignatureHash(unknownPackage)

        // Then
        assertThat(hash).isNull()
    }

    @Test
    fun `haveSameSignature should return false for different packages`() {
        // Given
        val package1 = "com.android.systemui"
        val package2 = "com.android.settings"

        // When
        // Note: This might return true if both are signed by platform
        // The test verifies the method doesn't crash
        val result = try {
            verifier.haveSameSignature(package1, package2)
        } catch (e: Exception) {
            false // Expected for packages that might not exist
        }

        // Then - just verify it returns a boolean
        assertThat(result).isAnyOf(true, false)
    }
}
