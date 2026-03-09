package com.sugarmunch.app.p2p

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.spec.MGF1ParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec

/**
 * Transfer Encryption - Secure P2P APK transfers
 * 
 * Features:
 * - AES-256-GCM for file encryption
 * - RSA-2048 for key exchange
 * - SHA-256 checksums for integrity verification
 * - Per-transfer unique encryption keys
 */
class TransferEncryption private constructor() {

    companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "SugarMunchP2PKey"
        const val AES_KEY_SIZE = 256
        const val GCM_TAG_LENGTH = 128
        const val GCM_IV_LENGTH = 12
        const val RSA_KEY_SIZE = 2048
        const val CHUNK_SIZE = 8192 // 8KB chunks for streaming encryption

        @Volatile
        private var instance: TransferEncryption? = null

        fun getInstance(): TransferEncryption {
            return instance ?: synchronized(this) {
                instance ?: TransferEncryption().also { instance = it }
            }
        }
    }

    // ═════════════════════════════════════════════════════════════
    // KEY MANAGEMENT
    // ═════════════════════════════════════════════════════════════

    /**
     * Generate or retrieve RSA key pair from Android Keystore
     */
    fun getOrCreateKeyPair(): java.security.KeyPair {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        
        // Check if key already exists
        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
            return java.security.KeyPair(entry.certificate.publicKey, entry.privateKey)
        }

        // Generate new RSA key pair
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA,
            ANDROID_KEYSTORE
        )

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT
        )
            .setKeySize(RSA_KEY_SIZE)
            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            .build()

        keyPairGenerator.initialize(keyGenParameterSpec)
        return keyPairGenerator.generateKeyPair()
    }

    /**
     * Generate a new AES session key for file encryption
     */
    fun generateSessionKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(AES_KEY_SIZE, SecureRandom())
        return keyGen.generateKey()
    }

    /**
     * Export public key as Base64 string for sharing
     */
    fun exportPublicKey(): String {
        val keyPair = getOrCreateKeyPair()
        return Base64.encodeToString(keyPair.public.encoded, Base64.NO_WRAP)
    }

    /**
     * Import peer's public key from Base64 string
     */
    fun importPublicKey(base64Key: String): java.security.PublicKey {
        val keyBytes = Base64.decode(base64Key, Base64.NO_WRAP)
        val keyFactory = java.security.KeyFactory.getInstance("RSA")
        val keySpec = java.security.spec.X509EncodedKeySpec(keyBytes)
        return keyFactory.generatePublic(keySpec)
    }

    // ═════════════════════════════════════════════════════════════
    // ENCRYPTION
    // ═════════════════════════════════════════════════════════════

    /**
     * Encrypt a session key using RSA-OAEP for secure transmission
     */
    fun encryptSessionKey(sessionKey: SecretKey, peerPublicKey: java.security.PublicKey): String {
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        val oaepSpec = OAEPParameterSpec(
            "SHA-256",
            "MGF1",
            MGF1ParameterSpec.SHA256,
            PSource.PSpecified.DEFAULT
        )
        cipher.init(Cipher.ENCRYPT_MODE, peerPublicKey, oaepSpec)
        val encrypted = cipher.doFinal(sessionKey.encoded)
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    /**
     * Decrypt a session key using our private key
     */
    fun decryptSessionKey(encryptedKey: String): SecretKey {
        val keyPair = getOrCreateKeyPair()
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        val oaepSpec = OAEPParameterSpec(
            "SHA-256",
            "MGF1",
            MGF1ParameterSpec.SHA256,
            PSource.PSpecified.DEFAULT
        )
        cipher.init(Cipher.DECRYPT_MODE, keyPair.private, oaepSpec)
        val decrypted = cipher.doFinal(Base64.decode(encryptedKey, Base64.NO_WRAP))
        return SecretKeySpec(decrypted, "AES")
    }

    /**
     * Encrypt a file using AES-256-GCM
     * Returns the IV + encrypted data
     */
    suspend fun encryptFile(
        inputFile: File,
        outputFile: File,
        sessionKey: SecretKey,
        onProgress: (Float) -> Unit = {}
    ): ByteArray = withContext(Dispatchers.IO) {
        // Generate random IV
        val iv = ByteArray(GCM_IV_LENGTH).apply { SecureRandom().nextBytes(this) }
        
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, sessionKey, gcmSpec)

        FileInputStream(inputFile).use { fis ->
            FileOutputStream(outputFile).use { fos ->
                // Write IV first
                fos.write(iv)

                val buffer = ByteArray(CHUNK_SIZE)
                var bytesRead: Int
                var totalBytes = 0L
                val fileSize = inputFile.length()

                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    val encrypted = cipher.update(buffer, 0, bytesRead)
                    if (encrypted != null) {
                        fos.write(encrypted)
                    }
                    totalBytes += bytesRead
                    onProgress(totalBytes.toFloat() / fileSize)
                }

                // Finalize encryption
                val finalBlock = cipher.doFinal()
                fos.write(finalBlock)
            }
        }

        onProgress(1.0f)
        iv
    }

    /**
     * Decrypt a file using AES-256-GCM
     * Expects IV prepended to encrypted data
     */
    suspend fun decryptFile(
        inputFile: File,
        outputFile: File,
        sessionKey: SecretKey,
        onProgress: (Float) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        FileInputStream(inputFile).use { fis ->
            // Read IV
            val iv = ByteArray(GCM_IV_LENGTH)
            fis.read(iv)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, sessionKey, gcmSpec)

            FileOutputStream(outputFile).use { fos ->
                val buffer = ByteArray(CHUNK_SIZE)
                var bytesRead: Int
                var totalBytes = 0L
                val fileSize = inputFile.length() - GCM_IV_LENGTH - (GCM_TAG_LENGTH / 8)

                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    // Leave room for auth tag at the end
                    val isFinal = fis.available() < CHUNK_SIZE
                    
                    if (isFinal) {
                        // This is the last chunk, includes auth tag
                        val decrypted = cipher.doFinal(buffer, 0, bytesRead)
                        fos.write(decrypted)
                    } else {
                        val decrypted = cipher.update(buffer, 0, bytesRead)
                        if (decrypted != null) {
                            fos.write(decrypted)
                        }
                    }
                    
                    totalBytes += bytesRead
                    if (fileSize > 0) {
                        onProgress((totalBytes.toFloat() / fileSize).coerceAtMost(1.0f))
                    }
                }
            }
        }

        onProgress(1.0f)
    }

    // ═════════════════════════════════════════════════════════════
    // CHECKSUM VERIFICATION
    // ═════════════════════════════════════════════════════════════

    /**
     * Calculate SHA-256 checksum of a file
     */
    suspend fun calculateChecksum(file: File): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(CHUNK_SIZE)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        Base64.encodeToString(digest.digest(), Base64.NO_WRAP)
    }

    /**
     * Calculate SHA-256 checksum of a byte array
     */
    fun calculateChecksum(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return Base64.encodeToString(digest.digest(data), Base64.NO_WRAP)
    }

    /**
     * Verify file integrity against expected checksum
     */
    suspend fun verifyChecksum(file: File, expectedChecksum: String): Boolean {
        val actualChecksum = calculateChecksum(file)
        return actualChecksum == expectedChecksum
    }

    /**
     * Create a complete transfer manifest with checksums
     */
    fun createTransferManifest(
        fileName: String,
        fileSize: Long,
        checksum: String,
        encryptedSessionKey: String,
        senderPublicKey: String
    ): TransferManifest {
        return TransferManifest(
            fileName = fileName,
            fileSize = fileSize,
            checksum = checksum,
            encryptedSessionKey = encryptedSessionKey,
            senderPublicKey = senderPublicKey,
            timestamp = System.currentTimeMillis()
        )
    }

    // ═════════════════════════════════════════════════════════════
    // BYTE ARRAY ENCRYPTION (for metadata)
    // ═════════════════════════════════════════════════════════════

    /**
     * Encrypt small data (metadata) using AES-GCM
     */
    fun encryptData(plaintext: ByteArray, sessionKey: SecretKey): EncryptedData {
        val iv = ByteArray(GCM_IV_LENGTH).apply { SecureRandom().nextBytes(this) }
        
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, sessionKey, gcmSpec)
        
        val ciphertext = cipher.doFinal(plaintext)
        
        return EncryptedData(
            iv = Base64.encodeToString(iv, Base64.NO_WRAP),
            data = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        )
    }

    /**
     * Decrypt small data (metadata) using AES-GCM
     */
    fun decryptData(encryptedData: EncryptedData, sessionKey: SecretKey): ByteArray {
        val iv = Base64.decode(encryptedData.iv, Base64.NO_WRAP)
        val ciphertext = Base64.decode(encryptedData.data, Base64.NO_WRAP)
        
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, sessionKey, gcmSpec)
        
        return cipher.doFinal(ciphertext)
    }
}

/**
 * Data class for encrypted metadata
 */
data class EncryptedData(
    val iv: String,
    val data: String
)

/**
 * Transfer manifest containing all metadata for a secure transfer
 */
data class TransferManifest(
    val fileName: String,
    val fileSize: Long,
    val checksum: String,
    val encryptedSessionKey: String,
    val senderPublicKey: String,
    val timestamp: Long,
    val appId: String? = null,
    val appName: String? = null,
    val appVersion: String? = null
) {
    fun toJson(): String {
        return """{
            "fileName": "$fileName",
            "fileSize": $fileSize,
            "checksum": "$checksum",
            "encryptedSessionKey": "$encryptedSessionKey",
            "senderPublicKey": "$senderPublicKey",
            "timestamp": $timestamp,
            "appId": ${appId?.let { """ "$it" """ } ?: "null"},
            "appName": ${appName?.let { """ "$it" """ } ?: "null"},
            "appVersion": ${appVersion?.let { """ "$it" """ } ?: "null"}
        }""".trimIndent()
    }

    companion object {
        fun fromJson(json: String): TransferManifest {
            // Simple JSON parsing - in production use Gson or similar
            val map = json.trim().removeSurrounding("{", "}")
                .split(",\n")
                .associate { line ->
                    val (key, value) = line.trim().split(": ", limit = 2)
                    key.trim('"') to value.trim().trim('"')
                }
            
            return TransferManifest(
                fileName = map["fileName"] ?: "",
                fileSize = map["fileSize"]?.toLongOrNull() ?: 0,
                checksum = map["checksum"] ?: "",
                encryptedSessionKey = map["encryptedSessionKey"] ?: "",
                senderPublicKey = map["senderPublicKey"] ?: "",
                timestamp = map["timestamp"]?.toLongOrNull() ?: 0,
                appId = map["appId"]?.takeIf { it != "null" },
                appName = map["appName"]?.takeIf { it != "null" },
                appVersion = map["appVersion"]?.takeIf { it != "null" }
            )
        }
    }
}

/**
 * Security validation result
 */
sealed class SecurityValidationResult {
    data class Success(val manifest: TransferManifest) : SecurityValidationResult()
    data class Failure(val reason: String) : SecurityValidationResult()
}
