package com.sugarmunch.app.crash

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Unit tests for CrashReportingManager
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class CrashReportingManagerTest {

    private lateinit var crashReportingManager: CrashReportingManager

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        crashReportingManager = CrashReportingManager.getInstance(context)
    }

    @Test
    fun `recordException with throwable should add to history`() = runTest {
        // Given
        val exception = RuntimeException("Test exception")
        val context = ErrorContext(
            screen = "TestScreen",
            action = "TestAction"
        )

        // When
        crashReportingManager.recordException(exception, Severity.ERROR, context)

        // Wait for coroutine to complete
        kotlinx.coroutines.delay(100)

        // Then
        val history = crashReportingManager.getErrorHistory()
        assertThat(history).isNotEmpty()
        assertThat(history.last().type).isEqualTo("RuntimeException")
        assertThat(history.last().message).isEqualTo("Test exception")
        assertThat(history.last().severity).isEqualTo(Severity.ERROR)
    }

    @Test
    fun `recordException with message should add to history`() = runTest {
        // Given
        val message = "Test error message"

        // When
        crashReportingManager.recordException(message, Severity.WARNING)

        // Wait for coroutine to complete
        kotlinx.coroutines.delay(100)

        // Then
        val history = crashReportingManager.getErrorHistory()
        assertThat(history).isNotEmpty()
        assertThat(history.last().type).isEqualTo("Manual")
        assertThat(history.last().message).isEqualTo(message)
        assertThat(history.last().severity).isEqualTo(Severity.WARNING)
    }

    @Test
    fun `getErrorSummary should return correct counts`() = runTest {
        // Given
        crashReportingManager.recordException(RuntimeException("Error 1"), Severity.ERROR)
        crashReportingManager.recordException(RuntimeException("Error 2"), Severity.ERROR)
        crashReportingManager.recordException(RuntimeException("Error 3"), Severity.CRITICAL)
        
        // Wait for coroutines
        kotlinx.coroutines.delay(100)

        // When
        val summary = crashReportingManager.getErrorSummary()

        // Then
        assertThat(summary.totalErrors).isAtLeast(3)
        assertThat(summary.criticalErrors).isAtLeast(1)
    }

    @Test
    fun `clearErrorHistory should remove all errors`() = runTest {
        // Given
        crashReportingManager.recordException(RuntimeException("Test"))
        kotlinx.coroutines.delay(100)
        assertThat(crashReportingManager.getErrorHistory()).isNotEmpty()

        // When
        crashReportingManager.clearErrorHistory()

        // Then
        assertThat(crashReportingManager.getErrorHistory()).isEmpty()
    }

    @Test
    fun `error history should respect max size`() = runTest {
        // Given - add more than max history size errors
        repeat(150) { i ->
            crashReportingManager.recordException(RuntimeException("Error $i"))
        }
        
        // Wait for coroutines
        kotlinx.coroutines.delay(500)

        // Then - should only keep last 100
        val history = crashReportingManager.getErrorHistory()
        assertThat(history.size).isAtMost(100)
    }

    @Test
    fun `ErrorEvent formattedTime should be valid date string`() {
        // Given
        val event = ErrorEvent(
            type = "TestException",
            message = "Test message",
            severity = Severity.ERROR,
            timestamp = System.currentTimeMillis()
        )

        // Then
        assertThat(event.formattedTime).isNotEmpty()
        assertThat(event.formattedTime).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")
    }

    @Test
    fun `Severity enum should have correct ordinal values`() {
        // Then
        assertThat(Severity.DEBUG.ordinal).isEqualTo(0)
        assertThat(Severity.INFO.ordinal).isEqualTo(1)
        assertThat(Severity.WARNING.ordinal).isEqualTo(2)
        assertThat(Severity.ERROR.ordinal).isEqualTo(3)
        assertThat(Severity.CRITICAL.ordinal).isEqualTo(4)
    }
}
