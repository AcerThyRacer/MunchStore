package com.sugarmunch.app.backup.proton

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

/**
 * Unit tests for Proton Drive backup encryption
 */
class ProtonEncryptionTest {

    companion object {
        private const val ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 256
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
    }

    @Test
    fun `test generate encryption key`() {
        // Given
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(KEY_SIZE)

        // When
        val key = keyGen.generateKey()

        // Then
        assertThat(key).isNotNull()
        assertThat(key.algorithm).isEqualTo("AES")
        assertThat(key.encoded.size).isEqualTo(KEY_SIZE / 8)
    }

    @Test
    fun `test encrypt and decrypt data`() {
        // Given
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(KEY_SIZE)
        val key = keyGen.generateKey()

        val iv = ByteArray(GCM_IV_LENGTH).apply { SecureRandom().nextBytes(this) }
        val testData = "Test backup data".toByteArray()

        // When - Encrypt
        val cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val encryptedBytes = cipher.doFinal(testData)

        // Then - Verify encryption produced different data
        assertThat(encryptedBytes).isNotEqualTo(testData)
        assertThat(encryptedBytes.size).isGreaterThan(testData.size)

        // When - Decrypt
        val decryptCipher = Cipher.getInstance(ENCRYPTION_ALGORITHM)
        decryptCipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val decryptedBytes = decryptCipher.doFinal(encryptedBytes)

        // Then - Verify decryption restored original data
        assertThat(decryptedBytes).isEqualTo(testData)
    }

    @Test
    fun `test calculate SHA-256 checksum`() {
        // Given
        val testData = "Test backup data".toByteArray()
        val md = MessageDigest.getInstance("SHA-256")

        // When
        val checksum = md.digest(testData).joinToString("") { "%02x".format(it) }

        // Then
        assertThat(checksum).hasLength(64) // SHA-256 produces 64 hex characters
        assertThat(checksum).matches(Regex("[a-f0-9]+"))
    }

    @Test
    fun `test same data produces same checksum`() {
        // Given
        val testData = "Test backup data".toByteArray()
        val md = MessageDigest.getInstance("SHA-256")

        // When
        val checksum1 = md.digest(testData).joinToString("") { "%02x".format(it) }
        val checksum2 = md.digest(testData).joinToString("") { "%02x".format(it) }

        // Then
        assertThat(checksum1).isEqualTo(checksum2)
    }

    @Test
    fun `test different data produces different checksum`() {
        // Given
        val data1 = "Test backup data 1".toByteArray()
        val data2 = "Test backup data 2".toByteArray()
        val md = MessageDigest.getInstance("SHA-256")

        // When
        val checksum1 = md.digest(data1).joinToString("") { "%02x".format(it) }
        val checksum2 = md.digest(data2).joinToString("") { "%02x".format(it) }

        // Then
        assertThat(checksum1).isNotEqualTo(checksum2)
    }

    @Test
    fun `test derive key from password`() {
        // Given
        val password = "test_password"
        val salt = "test_salt"

        // When
        val keySpec = java.security.spec.PBEKeySpec(
            password.toCharArray(),
            salt.toByteArray(),
            100000,
            KEY_SIZE
        )
        val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val key = factory.generateSecret(keySpec)

        // Then
        assertThat(key).isNotNull()
        assertThat(key.algorithm).isEqualTo("AES")
        assertThat(key.encoded.size).isEqualTo(KEY_SIZE / 8)
    }

    @Test
    fun `test same password and salt produces same key`() {
        // Given
        val password = "test_password"
        val salt = "test_salt"

        // When
        val keySpec1 = java.security.spec.PBEKeySpec(
            password.toCharArray(),
            salt.toByteArray(),
            100000,
            KEY_SIZE
        )
        val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val key1 = factory.generateSecret(keySpec1)

        val keySpec2 = java.security.spec.PBEKeySpec(
            password.toCharArray(),
            salt.toByteArray(),
            100000,
            KEY_SIZE
        )
        val key2 = factory.generateSecret(keySpec2)

        // Then
        assertThat(key1.encoded).isEqualTo(key2.encoded)
    }

    @Test
    fun `test different salt produces different key`() {
        // Given
        val password = "test_password"
        val salt1 = "salt_1"
        val salt2 = "salt_2"

        // When
        val keySpec1 = java.security.spec.PBEKeySpec(
            password.toCharArray(),
            salt1.toByteArray(),
            100000,
            KEY_SIZE
        )
        val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val key1 = factory.generateSecret(keySpec1)

        val keySpec2 = java.security.spec.PBEKeySpec(
            password.toCharArray(),
            salt2.toByteArray(),
            100000,
            KEY_SIZE
        )
        val key2 = factory.generateSecret(keySpec2)

        // Then
        assertThat(key1.encoded).isNotEqualTo(key2.encoded)
    }
}

/**
 * Unit tests for Proton Drive backup models
 */
class ProtonModelsTest {

    @Test
    fun `test BackupMetadata creation`() {
        // Given
        val includedData = listOf(DataType.THEMES, DataType.EFFECTS, DataType.SETTINGS)

        // When
        val metadata = BackupMetadata(
            backupId = "backup_123",
            userId = "user_456",
            timestamp = System.currentTimeMillis(),
            version = "2.0.0",
            backupType = BackupType.FULL,
            fileSize = 1024 * 1024, // 1MB
            encrypted = true,
            checksum = "abc123",
            compressionType = CompressionType.GZIP,
            includedData = includedData
        )

        // Then
        assertThat(metadata.backupId).isEqualTo("backup_123")
        assertThat(metadata.userId).isEqualTo("user_456")
        assertThat(metadata.version).isEqualTo("2.0.0")
        assertThat(metadata.backupType).isEqualTo(BackupType.FULL)
        assertThat(metadata.encrypted).isTrue()
        assertThat(metadata.compressionType).isEqualTo(CompressionType.GZIP)
        assertThat(metadata.includedData).hasSize(3)
    }

    @Test
    fun `test BackupManifest creation`() {
        // Given
        val backups = listOf(
            BackupMetadata(
                backupId = "backup_1",
                userId = "user_1",
                timestamp = System.currentTimeMillis(),
                version = "2.0.0",
                backupType = BackupType.FULL,
                fileSize = 1024 * 1024,
                encrypted = true,
                checksum = "abc123",
                compressionType = CompressionType.GZIP,
                includedData = DataType.values().toList()
            )
        )

        // When
        val manifest = BackupManifest(
            manifestId = "manifest_123",
            userId = "user_1",
            created = System.currentTimeMillis(),
            updated = System.currentTimeMillis(),
            backups = backups,
            totalSize = 1024 * 1024
        )

        // Then
        assertThat(manifest.manifestId).isEqualTo("manifest_123")
        assertThat(manifest.backups).hasSize(1)
        assertThat(manifest.totalSize).isEqualTo(1024 * 1024)
        assertThat(manifest.encryptionVersion).isEqualTo("AES-256-GCM")
    }

    @Test
    fun `test StorageUsage calculations`() {
        // Given
        val usage = StorageUsage(
            totalBytes = 15728640, // 15MB
            backupCount = 5,
            oldestBackup = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000),
            newestBackup = System.currentTimeMillis()
        )

        // Then
        assertThat(usage.totalMB).isWithin(0.01f).of(15.0f)
        assertThat(usage.totalGB).isWithin(0.0001f).of(0.0146f)
        assertThat(usage.backupCount).isEqualTo(5)
    }

    @Test
    fun `test BackupType values`() {
        // Then
        assertThat(BackupType.values()).containsExactly(
            BackupType.FULL,
            BackupType.INCREMENTAL,
            BackupType.THEMES_ONLY,
            BackupType.EFFECTS_ONLY,
            BackupType.SETTINGS_ONLY
        )
    }

    @Test
    fun `test DataType values`() {
        // Then
        assertThat(DataType.values()).containsExactly(
            DataType.THEMES,
            DataType.EFFECTS,
            DataType.SETTINGS,
            DataType.ACHIEVEMENTS,
            DataType.SHOP_INVENTORY,
            DataType.DAILY_REWARDS,
            DataType.AUTOMATIONS,
            DataType.FAVORITES,
            DataType.HISTORY
        )
    }

    @Test
    fun `test CompressionType values`() {
        // Then
        assertThat(CompressionType.values()).containsExactly(
            CompressionType.NONE,
            CompressionType.GZIP,
            CompressionType.ZIP
        )
    }

    @Test
    fun `test ProtonAuthState types`() {
        // Given
        val unauthenticated = ProtonAuthState.Unauthenticated
        val authenticating = ProtonAuthState.Authenticating
        val authenticated = ProtonAuthState.Authenticated("test@proton.me", "user_123")
        val error = ProtonAuthState.Error("Test error")

        // Then
        assertThat(unauthenticated).isInstanceOf(ProtonAuthState.Unauthenticated::class.java)
        assertThat(authenticating).isInstanceOf(ProtonAuthState.Authenticating::class.java)
        assertThat(authenticated.email).isEqualTo("test@proton.me")
        assertThat(authenticated.userId).isEqualTo("user_123")
        assertThat((error as ProtonAuthState.Error).message).isEqualTo("Test error")
    }

    @Test
    fun `test BackupState types`() {
        // Given
        val idle = BackupState.Idle
        val backingUp = BackupState.BackingUp(0.5f, "Uploading...")
        val restoring = BackupState.Restoring(0.3f, "Downloading...")
        val error = BackupState.Error("Test error")

        // Then
        assertThat(idle).isInstanceOf(BackupState.Idle::class.java)
        assertThat(backingUp.progress).isEqualTo(0.5f)
        assertThat(backingUp.status).isEqualTo("Uploading...")
        assertThat(restoring.progress).isEqualTo(0.3f)
        assertThat(restoring.status).isEqualTo("Downloading...")
        assertThat((error as BackupState.Error).message).isEqualTo("Test error")
    }
}
