package com.sugarmunch.app.theme.profile

import com.google.gson.Gson
import com.sugarmunch.app.theme.builder.CustomTheme
import com.sugarmunch.app.theme.builder.GradientType
import com.sugarmunch.app.theme.builder.ParticleType
import com.sugarmunch.app.theme.presets.ThemePresets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeProfileMappersTest {

    private val gson = Gson()

    @Test
    fun `preset roundtrip keeps identity and palette`() {
        val preset = ThemePresets.getDefault()

        val profile = preset.toThemeProfile()
        val restored = profile.toCandyTheme()

        assertEquals(preset.id, profile.id)
        assertEquals(preset.id, restored.id)
        assertEquals(profile.palette.primaryHex, restored.baseColors.primary.toHexString())
    }

    @Test
    fun `canonical envelope imports back into candidate`() {
        val profile = ThemePresets.CLASSIC_CANDY.toThemeProfile()
        val json = ThemeTransportEnvelope(profile = profile).toJson(gson)

        val candidate = parseThemeImportCandidate(json, gson)

        assertNotNull(candidate)
        assertEquals(profile.id, candidate?.profile?.id)
        assertEquals("Canonical envelope", candidate?.sourceLabel)
    }

    @Test
    fun `legacy builder export code still imports through canonical parser`() {
        val legacy = CustomTheme(
            name = "Legacy Candy",
            primaryColor = 0xFFFF69B4,
            secondaryColor = 0xFF9370DB,
            backgroundColor = 0xFF1A1A2E,
            surfaceColor = 0xFF16213E,
            gradientType = GradientType.LINEAR,
            gradientAngle = 45f,
            gradientColors = listOf(0xFFFF69B4, 0xFF9370DB),
            enableParticles = true,
            particleType = ParticleType.STARS
        )

        val candidate = parseThemeImportCandidate(legacy.toExportCode(), gson)

        assertNotNull(candidate)
        assertTrue(candidate?.isLegacyFormat == true)
        assertEquals("Legacy Candy", candidate?.profile?.name)
    }
}
