package com.sugarmunch.app.effects.v2.engine

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Unit tests for EffectEngineV2.
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class EffectEngineV2Test {

    private lateinit var engine: EffectEngineV2

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        engine = EffectEngineV2.getInstance(context)
    }

    @Test
    fun allEffects_shouldNotBeEmpty() {
        assertThat(engine.allEffects.value).isNotEmpty()
    }

    @Test
    fun getEffectSettings_unknownId_returnsEmptyMap() {
        val settings = engine.getEffectSettings("nonexistent_effect_id")
        assertThat(settings).isEmpty()
    }

    @Test
    fun getEffectSettings_knownId_returnsDefaultOrCachedSettings() {
        val firstId = engine.allEffects.value.firstOrNull()?.id ?: return
        val settings = engine.getEffectSettings(firstId)
        assertThat(settings).isNotNull()
    }

    @Test
    fun masterIntensity_shouldBeClampedBetween02And2() {
        engine.setMasterIntensity(0.1f)
        assertThat(engine.masterIntensity.value).isEqualTo(0.2f)
        engine.setMasterIntensity(3f)
        assertThat(engine.masterIntensity.value).isEqualTo(2f)
    }
}
