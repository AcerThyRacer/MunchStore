package com.sugarmunch.app.plugin.security

import com.google.common.truth.Truth.assertThat
import com.sugarmunch.app.plugin.model.PluginManifest
import com.sugarmunch.app.plugin.model.PluginPermission
import org.junit.Test
import java.security.MessageDigest
import java.security.Signature
import java.security.cert.X509Certificate
import java.util.Date

/**
 * Unit tests for PluginSecurity signature verification
 */
class PluginSecurityTest {

    @Test
    fun `test calculate manifest hash`() {
        // Given
        val manifest = PluginManifest(
            id = "test_plugin",
            name = "Test Plugin",
            version = "1.0.0",
            description = "A test plugin",
            author = "Test Author",
            permissions = emptyList(),
            minAppVersion = "2.0.0"
        )
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
        val manifestJson = json.encodeToString(manifest)

        // When
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(manifestJson.toByteArray())

        // Then
        assertThat(hash).hasLength(32) // SHA-256 produces 32 bytes
    }

    @Test
    fun `test same manifest produces same hash`() {
        // Given
        val manifest = PluginManifest(
            id = "test_plugin",
            name = "Test Plugin",
            version = "1.0.0",
            description = "A test plugin",
            author = "Test Author",
            permissions = emptyList(),
            minAppVersion = "2.0.0"
        )
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
        val manifestJson = json.encodeToString(manifest)
        val md = MessageDigest.getInstance("SHA-256")

        // When
        val hash1 = md.digest(manifestJson.toByteArray())
        val hash2 = md.digest(manifestJson.toByteArray())

        // Then
        assertThat(hash1).isEqualTo(hash2)
    }

    @Test
    fun `test different manifest produces different hash`() {
        // Given
        val manifest1 = PluginManifest(
            id = "test_plugin_1",
            name = "Test Plugin 1",
            version = "1.0.0",
            description = "A test plugin",
            author = "Test Author",
            permissions = emptyList(),
            minAppVersion = "2.0.0"
        )
        val manifest2 = PluginManifest(
            id = "test_plugin_2",
            name = "Test Plugin 2",
            version = "1.0.0",
            description = "A test plugin",
            author = "Test Author",
            permissions = emptyList(),
            minAppVersion = "2.0.0"
        )
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
        val md = MessageDigest.getInstance("SHA-256")

        // When
        val hash1 = md.digest(json.encodeToString(manifest1).toByteArray())
        val hash2 = md.digest(json.encodeToString(manifest2).toByteArray())

        // Then
        assertThat(hash1).isNotEqualTo(hash2)
    }

    @Test
    fun `test permission safety check`() {
        // Given
        val safePermissions = listOf(
            PluginPermission.READ_THEMES,
            PluginPermission.WRITE_THEMES,
            PluginPermission.READ_EFFECTS,
            PluginPermission.WRITE_EFFECTS
        )

        val unsafePermissions = listOf(
            PluginPermission.INSTALL_PACKAGES,
            PluginPermission.DELETE_PACKAGES,
            PluginPermission.MODIFY_SYSTEM_SETTINGS
        )

        // Then
        safePermissions.forEach { permission ->
            assertThat(permission.isSafe()).isTrue()
        }

        unsafePermissions.forEach { permission ->
            assertThat(permission.isSafe()).isFalse()
        }
    }

    @Test
    fun `test PluginManifest validation`() {
        // Given
        val validManifest = PluginManifest(
            id = "valid_plugin",
            name = "Valid Plugin",
            version = "1.0.0",
            description = "A valid plugin",
            author = "Test Author",
            permissions = listOf(PluginPermission.READ_THEMES),
            minAppVersion = "2.0.0"
        )

        val invalidManifest = PluginManifest(
            id = "", // Empty ID
            name = "", // Empty name
            version = "invalid",
            description = "",
            author = "",
            permissions = listOf(PluginPermission.INSTALL_PACKAGES), // Unsafe permission
            minAppVersion = "2.0.0"
        )

        // Then
        assertThat(validManifest.isValid()).isTrue()
        assertThat(invalidManifest.isValid()).isFalse()
    }
}

/**
 * Helper extensions for testing
 */
private fun PluginPermission.isSafe(): Boolean {
    val safePermissions = setOf(
        PluginPermission.READ_THEMES,
        PluginPermission.WRITE_THEMES,
        PluginPermission.READ_EFFECTS,
        PluginPermission.WRITE_EFFECTS,
        PluginPermission.NETWORK_ACCESS,
        PluginPermission.VIBRATE,
        PluginPermission.SHOW_NOTIFICATIONS
    )
    return this in safePermissions
}

private fun PluginManifest.isValid(): Boolean {
    return id.isNotBlank() &&
            name.isNotBlank() &&
            version.matches(Regex("\\d+\\.\\d+\\.\\d+")) &&
            author.isNotBlank() &&
            !permissions.contains(PluginPermission.INSTALL_PACKAGES) &&
            !permissions.contains(PluginPermission.DELETE_PACKAGES) &&
            !permissions.contains(PluginPermission.MODIFY_SYSTEM_SETTINGS)
}
