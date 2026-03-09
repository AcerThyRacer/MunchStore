package com.sugarmunch.app.ui.customization.utils

import com.sugarmunch.app.ui.customization.StaggerPattern
import com.sugarmunch.app.ui.customization.utils.AnimationUtils
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for AnimationUtils
 */
class AnimationUtilsTest {

    @Test
    fun calculateStaggerDelay_forward_pattern_increases() {
        // Given
        val index = 3
        val totalItems = 10
        val baseDelay = 50
        val pattern = StaggerPattern.FORWARD

        // When
        val delay = AnimationUtils.calculateStaggerDelay(index, totalItems, baseDelay, pattern)

        // Then
        assertEquals(150, delay) // 3 * 50
    }

    @Test
    fun calculateStaggerDelay_backward_pattern_decreases() {
        // Given
        val index = 3
        val totalItems = 10
        val baseDelay = 50
        val pattern = StaggerPattern.BACKWARD

        // When
        val delay = AnimationUtils.calculateStaggerDelay(index, totalItems, baseDelay, pattern)

        // Then
        assertEquals(300, delay) // (10 - 3 - 1) * 50 = 6 * 50
    }

    @Test
    fun calculateStaggerDelay_centerOut_middle_isZero() {
        // Given
        val totalItems = 5
        val baseDelay = 50
        val pattern = StaggerPattern.CENTER_OUT

        // When (middle item)
        val delay = AnimationUtils.calculateStaggerDelay(2, totalItems, baseDelay, pattern)

        // Then
        assertEquals(0, delay) // Center item has no delay
    }

    @Test
    fun calculateStaggerDelay_random_pattern_isPositive() {
        // Given
        val index = 3
        val totalItems = 10
        val baseDelay = 50
        val pattern = StaggerPattern.RANDOM

        // When
        val delay = AnimationUtils.calculateStaggerDelay(index, totalItems, baseDelay, pattern)

        // Then
        assertTrue(delay >= 0)
    }

    @Test
    fun calculateCascadeDelays_sequential_increases() {
        // Given
        val itemCount = 5
        val baseDelay = 50
        val cascadeType = AnimationUtils.CascadeType.SEQUENTIAL

        // When
        val delays = AnimationUtils.calculateCascadeDelays(itemCount, baseDelay, cascadeType)

        // Then
        assertEquals(5, delays.size)
        assertEquals(0, delays[0])
        assertEquals(50, delays[1])
        assertEquals(100, delays[2])
    }

    @Test
    fun calculateCascadeDelays_grouped_sameForGroup() {
        // Given
        val itemCount = 8
        val baseDelay = 50
        val cascadeType = AnimationUtils.CascadeType.GROUPED

        // When
        val delays = AnimationUtils.calculateCascadeDelays(itemCount, baseDelay, cascadeType)

        // Then
        assertEquals(8, delays.size)
        // First 4 items should have same delay
        assertEquals(delays[0], delays[1])
        assertEquals(delays[0], delays[2])
        assertEquals(delays[0], delays[3])
    }

    @Test
    fun calculateCascadeDelays_random_allPositive() {
        // Given
        val itemCount = 5
        val baseDelay = 50
        val cascadeType = AnimationUtils.CascadeType.RANDOM

        // When
        val delays = AnimationUtils.calculateCascadeDelays(itemCount, baseDelay, cascadeType)

        // Then
        assertEquals(5, delays.size)
        delays.forEach { assertTrue(it >= 0) }
    }

    @Test
    fun calculateCascadeDelays_wave_pattern() {
        // Given
        val itemCount = 10
        val baseDelay = 50
        val cascadeType = AnimationUtils.CascadeType.WAVE

        // When
        val delays = AnimationUtils.calculateCascadeDelays(itemCount, baseDelay, cascadeType)

        // Then
        assertEquals(10, delays.size)
        delays.forEach { assertTrue(it >= 0) }
    }

    @Test
    fun shouldReduceAnimation_ultra_false() {
        // Given
        val qualityPreset = "ULTRA"

        // When
        val shouldReduce = AnimationUtils.shouldReduceAnimation(qualityPreset, 10, false)

        // Then
        assertFalse(shouldReduce)
    }

    @Test
    fun shouldReduceAnimation_powerSaver_true() {
        // Given
        val qualityPreset = "POWER_SAVER"

        // When
        val shouldReduce = AnimationUtils.shouldReduceAnimation(qualityPreset, 100, true)

        // Then
        assertTrue(shouldReduce)
    }

    @Test
    fun shouldReduceAnimation_low_battery_true() {
        // Given
        val qualityPreset = "LOW"
        val batteryLevel = 30

        // When
        val shouldReduce = AnimationUtils.shouldReduceAnimation(qualityPreset, batteryLevel, false)

        // Then
        assertTrue(shouldReduce)
    }

    @Test
    fun shouldReduceAnimation_low_charging_false() {
        // Given
        val qualityPreset = "LOW"
        val batteryLevel = 30

        // When
        val shouldReduce = AnimationUtils.shouldReduceAnimation(qualityPreset, batteryLevel, true)

        // Then
        assertFalse(shouldReduce)
    }

    @Test
    fun getFrameDurationMillis_ultra_is16() {
        // When
        val duration = AnimationUtils.getFrameDurationMillis("ULTRA")

        // Then
        assertEquals(16, duration)
    }

    @Test
    fun getFrameDurationMillis_powerSaver_is50() {
        // When
        val duration = AnimationUtils.getFrameDurationMillis("POWER_SAVER")

        // Then
        assertEquals(50, duration)
    }

    @Test
    fun getFrameDurationMillis_accessibility_is100() {
        // When
        val duration = AnimationUtils.getFrameDurationMillis("ACCESSIBILITY")

        // Then
        assertEquals(100, duration)
    }

    @Test
    fun createBezierEasing_returnsNonNull() {
        // Given
        val bezier = com.sugarmunch.app.ui.customization.CustomBezier(
            x1 = 0.25f,
            y1 = 0.1f,
            x2 = 0.25f,
            y2 = 1f
        )

        // When
        val easing = AnimationUtils.createBezierEasing(bezier)

        // Then
        assertNotNull(easing)
    }

    @Test
    fun createElasticEasing_returnsNonNull() {
        // When
        val easing = AnimationUtils.createElasticEasing()

        // Then
        assertNotNull(easing)
    }

    @Test
    fun createSpringEasing_returnsNonNull() {
        // When
        val easing = AnimationUtils.createSpringEasing()

        // Then
        assertNotNull(easing)
    }

    @Test
    fun bounceIn_easing_zeroIsZero() {
        // When
        val value = AnimationUtils.bounceIn(0f)

        // Then
        assertEquals(0f, value, 0.001f)
    }

    @Test
    fun bounceOut_easing_oneIsOne() {
        // When
        val value = AnimationUtils.bounceOut(1f)

        // Then
        assertEquals(1f, value, 0.001f)
    }

    @Test
    fun backIn_easing_zeroIsZero() {
        // When
        val value = AnimationUtils.backIn(0f)

        // Then
        assertEquals(0f, value, 0.001f)
    }

    @Test
    fun backOut_easing_oneIsOne() {
        // When
        val value = AnimationUtils.backOut(1f)

        // Then
        assertEquals(1f, value, 0.001f)
    }

    @Test
    fun circIn_easing_zeroIsZero() {
        // When
        val value = AnimationUtils.circIn(0f)

        // Then
        assertEquals(0f, value, 0.001f)
    }

    @Test
    fun circOut_easing_oneIsOne() {
        // When
        val value = AnimationUtils.circOut(1f)

        // Then
        assertEquals(1f, value, 0.001f)
    }

    @Test
    fun expoIn_easing_zeroIsZero() {
        // When
        val value = AnimationUtils.expoIn(0f)

        // Then
        assertEquals(0f, value, 0.001f)
    }

    @Test
    fun expoOut_easing_oneIsOne() {
        // When
        val value = AnimationUtils.expoOut(1f)

        // Then
        assertEquals(1f, value, 0.001f)
    }
}
