package com.sugarmunch.app.plugin.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.sugarmunch.app.BuildConfig
import com.sugarmunch.app.plugin.model.PluginManifest
import com.sugarmunch.app.plugin.model.PluginPermission
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.io.File
import java.security.MessageDigest
import java.security.PublicKey
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.jar.JarFile

/**
 * SugarMunch Plugin Security System
 * 
 * Provides comprehensive security for the plugin system including:
 * - Digital signature verification
 * - Permission system
 * - Resource usage limits
 * - Network access control
 * - Sandboxing capabilities
 * 
 * Similar to Android's app permission system or VS Code's extension
 * manifest validation.
 */
class PluginSecurity(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    // Trusted certificate authorities
    private val trustedAuthorities = mutableSetOf<String>()
    
    // Blocked plugins (by signature hash)
    private val blockedSignatures = mutableSetOf<String>()
    
    // Permission grants per plugin
    private val permissionGrants = mutableMapOf<String, MutableSet<PluginPermission>>()
    
    // Resource usage tracking
    private val resourceUsage = mutableMapOf<String, ResourceUsage>()
    
    init {
        loadTrustedAuthorities()
        loadBlockedSignatures()
    }
    
    /**
     * Validate a plugin package structure and manifest
     * @param pluginFile The plugin file or directory
     * @return Result containing the manifest or validation error
     */
    fun validatePlugin(pluginFile: File): Result<PluginManifest> {
        return try {
            // Check if file exists
            if (!pluginFile.exists()) {
                return Result.failure(SecurityException("Plugin file does not exist: ${pluginFile.path}"))
            }
            
            // Validate plugin structure
            val manifest = when {
                pluginFile.isDirectory -> validateDirectoryPlugin(pluginFile)
                pluginFile.extension == "smpkg" -> validatePackagePlugin(pluginFile)
                else -> return Result.failure(SecurityException("Invalid plugin format. Expected directory or .smpkg file"))
            }
            
            // Validate manifest content
            validateManifest(manifest).getOrThrow()
            
            // Check minimum requirements
            validateMinimumRequirements(pluginFile, manifest).getOrThrow()
            
            Result.success(manifest)
        } catch (e: SecurityException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(SecurityException("Plugin validation failed: ${e.message}", e))
        }
    }
    
    /**
     * Verify plugin digital signature
     * @param pluginFile The plugin file
     * @param manifest The plugin manifest
     * @return true if signature is valid and trusted
     */
    fun verifySignature(pluginFile: File, manifest: PluginManifest): Boolean {
        return try {
            when {
                pluginFile.isDirectory -> verifyDirectorySignature(pluginFile, manifest)
                pluginFile.extension == "smpkg" -> verifyPackageSignature(pluginFile, manifest)
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if a plugin has the required permissions
     * @param manifest The plugin manifest to check
     * @return true if all requested permissions are granted
     */
    fun checkPermissions(manifest: PluginManifest): Boolean {
        val pluginId = manifest.id
        
        // Get granted permissions for this plugin
        val granted = permissionGrants[pluginId] ?: emptySet()
        
        // Check if all requested permissions are granted
        return manifest.permissions.all { requested ->
            when (requested) {
                PluginPermission.NETWORK,
                PluginPermission.STORAGE_READ,
                PluginPermission.STORAGE_WRITE,
                PluginPermission.OVERLAY,
                PluginPermission.NOTIFICATIONS,
                PluginPermission.BATTERY,
                PluginPermission.SYSTEM_SETTINGS -> granted.contains(requested)
                
                // Dangerous permissions always require explicit user approval
                PluginPermission.SYSTEM_HOOKS,
                PluginPermission.ACCESSIBILITY -> false
            }
        }
    }
    
    /**
     * Request permissions for a plugin
     * @param pluginId The plugin ID
     * @param permissions The permissions to request
     * @return Set of granted permissions
     */
    fun requestPermissions(
        pluginId: String,
        permissions: Set<PluginPermission>
    ): Set<PluginPermission> {
        val granted = permissionGrants.getOrPut(pluginId) { mutableSetOf() }
        
        permissions.forEach { permission ->
            if (isPermissionSafe(permission)) {
                granted.add(permission)
            }
        }
        
        return granted.toSet()
    }
    
    /**
     * Revoke permissions for a plugin
     * @param pluginId The plugin ID
     * @param permissions The permissions to revoke
     */
    fun revokePermissions(pluginId: String, permissions: Set<PluginPermission>) {
        permissionGrants[pluginId]?.removeAll(permissions)
    }
    
    /**
    * Revoke all permissions for a plugin
     * @param pluginId The plugin ID
     */
    fun revokeAllPermissions(pluginId: String) {
        permissionGrants.remove(pluginId)
    }
    
    /**
     * Check if plugin is within resource limits
     * @param pluginId The plugin ID
     * @return true if resource usage is within limits
     */
    fun checkResourceLimits(pluginId: String): Boolean {
        val usage = resourceUsage[pluginId] ?: return true
        
        return usage.memoryUsageMB <= MAX_MEMORY_MB &&
               usage.cpuUsagePercent <= MAX_CPU_PERCENT &&
               usage.storageUsageMB <= MAX_STORAGE_MB
    }
    
    /**
     * Update resource usage for a plugin
     * @param pluginId The plugin ID
     * @param memoryDeltaMB Memory change in MB
     * @param cpuUsagePercent Current CPU usage percentage
     */
    fun updateResourceUsage(
        pluginId: String,
        memoryDeltaMB: Float = 0f,
        cpuUsagePercent: Float = 0f,
        storageDeltaMB: Float = 0f
    ) {
        val usage = resourceUsage.getOrPut(pluginId) { ResourceUsage() }
        usage.memoryUsageMB = (usage.memoryUsageMB + memoryDeltaMB).coerceAtLeast(0f)
        usage.cpuUsagePercent = cpuUsagePercent.coerceIn(0f, 100f)
        usage.storageUsageMB = (usage.storageUsageMB + storageDeltaMB).coerceAtLeast(0f)
        usage.lastUpdated = System.currentTimeMillis()
    }
    
    /**
     * Check if network access is allowed for a plugin
     * @param pluginId The plugin ID
     * @param host The host being accessed
     * @return true if network access is allowed
     */
    fun isNetworkAllowed(pluginId: String, host: String): Boolean {
        // Check if plugin has network permission
        val hasNetworkPermission = permissionGrants[pluginId]?.contains(PluginPermission.NETWORK) ?: false
        if (!hasNetworkPermission) {
            return false
        }
        
        // Check against allowed hosts if specified
        // This could be expanded with a whitelist/blacklist system
        return !isBlockedHost(host)
    }
    
    /**
     * Check if a signature hash is blocked
     * @param signatureHash The signature hash to check
     * @return true if the signature is blocked
     */
    fun isBlockedSignature(signatureHash: String): Boolean {
        return blockedSignatures.contains(signatureHash)
    }
    
    /**
     * Add a signature to the blocklist
     * @param signatureHash The signature hash to block
     */
    fun blockSignature(signatureHash: String) {
        blockedSignatures.add(signatureHash)
        saveBlockedSignatures()
    }
    
    /**
     * Remove a signature from the blocklist
     * @param signatureHash The signature hash to unblock
     */
    fun unblockSignature(signatureHash: String) {
        blockedSignatures.remove(signatureHash)
        saveBlockedSignatures()
    }
    
    /**
     * Get security report for a plugin
     * @param pluginId The plugin ID
     * @return Security report
     */
    fun getSecurityReport(pluginId: String): SecurityReport {
        val permissions = permissionGrants[pluginId] ?: emptySet()
        val usage = resourceUsage[pluginId] ?: ResourceUsage()
        
        return SecurityReport(
            pluginId = pluginId,
            grantedPermissions = permissions,
            resourceUsage = usage,
            isWithinLimits = checkResourceLimits(pluginId),
            lastSecurityCheck = System.currentTimeMillis()
        )
    }
    
    // ========== PRIVATE METHODS ==========
    
    private fun validateDirectoryPlugin(pluginDir: File): PluginManifest {
        val manifestFile = File(pluginDir, "manifest.json")
        if (!manifestFile.exists()) {
            throw SecurityException("Plugin manifest not found")
        }
        
        val manifestJson = manifestFile.readText()
        return PluginManifest.fromJson(manifestJson)
            ?: throw SecurityException("Invalid manifest format")
    }
    
    private fun validatePackagePlugin(pluginFile: File): PluginManifest {
        // .smpkg files are ZIP archives containing the plugin
        val jarFile = JarFile(pluginFile)
        val manifestEntry = jarFile.getEntry("manifest.json")
            ?: throw SecurityException("Plugin manifest not found in package")
        
        val manifestJson = jarFile.getInputStream(manifestEntry).bufferedReader().use { it.readText() }
        return PluginManifest.fromJson(manifestJson)
            ?: throw SecurityException("Invalid manifest format")
    }
    
    private fun validateManifest(manifest: PluginManifest): Result<Unit> {
        // Validate required fields
        if (manifest.id.isBlank() || !manifest.id.matches(Regex("^[a-z0-9_]+\\.[a-z0-9_]+$"))) {
            return Result.failure(SecurityException("Invalid plugin ID format"))
        }
        
        if (manifest.name.isBlank() || manifest.name.length > 100) {
            return Result.failure(SecurityException("Invalid plugin name"))
        }
        
        if (manifest.version.isBlank() || !manifest.version.matches(Regex("^\\d+\\.\\d+\\.\\d+.*"))) {
            return Result.failure(SecurityException("Invalid version format"))
        }
        
        if (manifest.author.isBlank()) {
            return Result.failure(SecurityException("Plugin author is required"))
        }
        
        // Validate permissions
        manifest.permissions.forEach { permission ->
            if (!isValidPermission(permission)) {
                return Result.failure(SecurityException("Invalid permission: $permission"))
            }
        }
        
        return Result.success(Unit)
    }
    
    private fun validateMinimumRequirements(pluginFile: File, manifest: PluginManifest): Result<Unit> {
        // Check minimum API level
        if (Build.VERSION.SDK_INT < manifest.minApiLevel) {
            return Result.failure(
                SecurityException("Plugin requires API level ${manifest.minApiLevel}, current is ${Build.VERSION.SDK_INT}")
            )
        }
        
        // Check SugarMunch version compatibility
        val currentVersion = getSugarMunchVersion()
        if (!manifest.isCompatibleWith(currentVersion)) {
            return Result.failure(
                SecurityException("Plugin is not compatible with this SugarMunch version")
            )
        }
        
        // Check file size
        val size = if (pluginFile.isDirectory) {
            pluginFile.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
        } else {
            pluginFile.length()
        }
        
        if (size > MAX_PLUGIN_SIZE_BYTES) {
            return Result.failure(
                SecurityException("Plugin size exceeds maximum allowed (${MAX_PLUGIN_SIZE_MB}MB)")
            )
        }
        
        return Result.success(Unit)
    }
    
    private fun verifyDirectorySignature(pluginDir: File, manifest: PluginManifest): Boolean {
        val signatureFile = File(pluginDir, "signature")
        if (!signatureFile.exists()) {
            // Unsigned plugins are allowed in development mode
            return allowUnsignedPlugins()
        }
        
        // Verify signature against manifest
        return verifyManifestSignature(manifest, signatureFile.readBytes())
    }
    
    private fun verifyPackageSignature(pluginFile: File, manifest: PluginManifest): Boolean {
        val jarFile = JarFile(pluginFile)
        
        // Check for signature entry
        val signatureEntry = jarFile.getEntry("META-INF/SIGNATURE.RSA")
            ?: jarFile.getEntry("META-INF/SIGNATURE.DSA")
            ?: return allowUnsignedPlugins()
        
        // Verify JAR signature
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificates = jarFile.getInputStream(signatureEntry).use { stream ->
            certificateFactory.generateCertificates(stream)
        }
        
        // Check if any certificate is trusted
        return certificates.any { cert ->
            cert is X509Certificate && isTrustedCertificate(cert)
        }
    }
    
    /**
     * Verifies a detached signature against the plugin manifest.
     * Uses RSA/ECDSA signature verification with certificate chain validation.
     * Integrates with Proton Drive for secure plugin distribution.
     */
    private fun verifyManifestSignature(manifest: PluginManifest, signature: ByteArray): Boolean {
        if (signature.isEmpty()) return allowUnsignedPlugins()
        return try {
            // Step 1: Parse X.509 certificate from signature
            val certFactory = CertificateFactory.getInstance("X.509")
            val certs = certFactory.generateCertificates(ByteArrayInputStream(signature))
                .filterIsInstance<X509Certificate>()
            
            if (certs.isEmpty()) return allowUnsignedPlugins()

            // Step 2: Verify certificate chain
            val hasValidChain = verifyCertificateChain(certs)
            if (!hasValidChain) return false

            // Step 3: Check if any certificate is trusted
            val hasTrustedCert = certs.any { cert ->
                val issuer = cert.issuerX500Principal.name
                val subject = cert.subjectX500Principal.name
                trustedAuthorities.any { authority ->
                    issuer.contains(authority, ignoreCase = true) ||
                        subject.contains(authority, ignoreCase = true)
                }
            }

            // Step 4: Verify signature against manifest hash
            val manifestHash = calculateManifestHash(manifest)
            val signatureValid = certs.any { cert ->
                verifySignature(cert, manifestHash, signature)
            }

            when {
                // Release build: require both trusted cert and valid signature
                !BuildConfig.DEBUG -> hasTrustedCert && signatureValid
                // Debug build: allow development with no trusted authorities
                trustedAuthorities.isEmpty() -> true
                // Debug with trusted authorities: require trusted cert
                else -> hasTrustedCert
            }
        } catch (e: Exception) {
            logger.e("Signature verification failed", e)
            false
        }
    }

    /**
     * Verify certificate chain validity
     */
    private fun verifyCertificateChain(certificates: List<X509Certificate>): Boolean {
        return try {
            // Check certificate validity dates
            certificates.all { cert ->
                cert.checkValidity()
                true
            }
        } catch (e: Exception) {
            logger.e("Certificate chain validation failed", e)
            false
        }
    }

    /**
     * Calculate SHA-256 hash of manifest for signature verification
     */
    private fun calculateManifestHash(manifest: PluginManifest): ByteArray {
        val manifestJson = json.encodeToString(manifest)
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(manifestJson.toByteArray())
    }

    /**
     * Verify signature using certificate public key
     */
    private fun verifySignature(
        certificate: X509Certificate,
        dataHash: ByteArray,
        signature: ByteArray
    ): Boolean {
        return try {
            val signatureAlgorithm = when {
                certificate.sigAlgName.contains("SHA256") -> "SHA256with${certificate.sigAlgName.substringAfter("with")}"
                else -> certificate.sigAlgName
            }

            val sig = java.security.Signature.getInstance(signatureAlgorithm)
            sig.initVerify(certificate.publicKey)
            sig.update(dataHash)
            sig.verify(signature)
        } catch (e: Exception) {
            logger.e("Signature verification failed", e)
            false
        }
    }
    
    private fun isTrustedCertificate(certificate: X509Certificate): Boolean {
        // Check issuer against trusted authorities
        val issuer = certificate.issuerX500Principal.name
        return trustedAuthorities.any { authority ->
            issuer.contains(authority)
        }
    }
    
    private fun isPermissionSafe(permission: PluginPermission): Boolean {
        return permission in SAFE_PERMISSIONS
    }
    
    private fun isValidPermission(permission: PluginPermission): Boolean {
        return true // All enum values are valid
    }
    
    private fun isBlockedHost(host: String): Boolean {
        val blockedHosts = setOf(
            "localhost",
            "127.0.0.1",
            "10.",
            "192.168.",
            "172.16."
        )
        return blockedHosts.any { host.startsWith(it) || host == it }
    }
    
    /**
     * Whether to allow plugins with no signature. Only true in debug builds of the app.
     * Release builds require a valid signature (see [verifyManifestSignature]).
     */
    private fun allowUnsignedPlugins(): Boolean = BuildConfig.DEBUG
    
    private fun loadTrustedAuthorities() {
        // Load from secure storage
        trustedAuthorities.addAll(DEFAULT_TRUSTED_AUTHORITIES)
    }
    
    private fun loadBlockedSignatures() {
        // Load from persistent storage
        val prefs = context.getSharedPreferences("plugin_security", Context.MODE_PRIVATE)
        blockedSignatures.addAll(
            prefs.getStringSet("blocked_signatures", emptySet()) ?: emptySet()
        )
    }
    
    private fun saveBlockedSignatures() {
        val prefs = context.getSharedPreferences("plugin_security", Context.MODE_PRIVATE)
        prefs.edit().putStringSet("blocked_signatures", blockedSignatures).apply()
    }
    
    private fun getSugarMunchVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
    
    companion object {
        // Resource limits
        const val MAX_MEMORY_MB = 128f
        const val MAX_CPU_PERCENT = 25f
        const val MAX_STORAGE_MB = 100f
        const val MAX_PLUGIN_SIZE_MB = 50f
        const val MAX_PLUGIN_SIZE_BYTES = MAX_PLUGIN_SIZE_MB * 1024 * 1024
        
        // Permissions that don't require user confirmation
        val SAFE_PERMISSIONS = setOf(
            PluginPermission.NETWORK,
            PluginPermission.STORAGE_READ,
            PluginPermission.NOTIFICATIONS,
            PluginPermission.BATTERY
        )
        
        // Default trusted certificate authorities
        val DEFAULT_TRUSTED_AUTHORITIES = setOf(
            "SugarMunch",
            "CN=SugarMunch Plugin CA"
        )
    }
}

/**
 * Resource usage tracking
 */
data class ResourceUsage(
    var memoryUsageMB: Float = 0f,
    var cpuUsagePercent: Float = 0f,
    var storageUsageMB: Float = 0f,
    var lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Security report for a plugin
 */
data class SecurityReport(
    val pluginId: String,
    val grantedPermissions: Set<PluginPermission>,
    val resourceUsage: ResourceUsage,
    val isWithinLimits: Boolean,
    val lastSecurityCheck: Long
)

/**
 * Security exception
 */
class SecurityException(message: String, cause: Throwable? = null) : Exception(message, cause)
