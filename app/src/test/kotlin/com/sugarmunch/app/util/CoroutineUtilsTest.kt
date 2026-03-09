package com.sugarmunch.app.util

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Unit tests for CoroutineUtils
 */
@ExperimentalCoroutinesApi
class CoroutineUtilsTest {

    @Test
    fun `retryWithBackoff should succeed on first try`() = runTest {
        // Given
        var callCount = 0
        val block: suspend () -> String = {
            callCount++
            "Success"
        }

        // When
        val result = retryWithBackoff(block = block)

        // Then
        assertThat(result).isEqualTo("Success")
        assertThat(callCount).isEqualTo(1)
    }

    @Test
    fun `retryWithBackoff should retry on failure and eventually succeed`() = runTest {
        // Given
        var callCount = 0
        val block: suspend () -> String = {
            callCount++
            if (callCount < 3) {
                throw RuntimeException("Temporary failure")
            }
            "Success after retries"
        }

        // When
        val result = retryWithBackoff(
            maxRetries = 5,
            initialDelay = 10.milliseconds,
            block = block
        )

        // Then
        assertThat(result).isEqualTo("Success after retries")
        assertThat(callCount).isEqualTo(3)
    }

    @Test
    fun `retryWithBackoff should throw after max retries exceeded`() = runTest {
        // Given
        var callCount = 0
        val block: suspend () -> String = {
            callCount++
            throw RuntimeException("Always fails")
        }

        // When/Then
        try {
            retryWithBackoff(
                maxRetries = 3,
                initialDelay = 10.milliseconds,
                block = block
            )
            // Should not reach here
            throw AssertionError("Expected exception was not thrown")
        } catch (e: RuntimeException) {
            assertThat(e.message).isEqualTo("Always fails")
            assertThat(callCount).isEqualTo(3)
        }
    }

    @Test
    fun `parallelExecute should run operations concurrently`() = runTest {
        // Given
        val results = mutableListOf<String>()
        val operations = listOf(
            { delay(50); "A" },
            { delay(50); "B" },
            { delay(50); "C" }
        )

        // When
        val startTime = System.currentTimeMillis()
        val result = parallelExecute(operations = operations)
        val elapsed = System.currentTimeMillis() - startTime

        // Then - should complete in ~50ms not 150ms
        assertThat(result).containsExactly("A", "B", "C")
        assertThat(elapsed).isLessThan(100)
    }

    @Test
    fun `Debouncer should delay execution`() = runTest {
        // Given
        val debouncer = Debouncer(50)
        var executed = false
        val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.Default)

        // When
        debouncer.debounce(scope) {
            executed = true
        }

        // Initially not executed
        assertThat(executed).isFalse()

        // Wait for debounce delay
        delay(100)

        // Then
        assertThat(executed).isTrue()
        
        scope.close()
    }

    @Test
    fun `Debouncer should cancel previous execution on rapid calls`() = runTest {
        // Given
        val debouncer = Debouncer(50)
        var executionCount = 0
        val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.Default)

        // When - rapid calls
        repeat(5) {
            debouncer.debounce(scope) {
                executionCount++
            }
            delay(20)
        }

        // Wait for final debounce
        delay(100)

        // Then - should only execute once
        assertThat(executionCount).isEqualTo(1)
        
        scope.close()
    }

    @Test
    fun `Throttle should limit execution rate`() = runTest {
        // Given
        val throttle = Throttle(100)
        var executionCount = 0
        val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.Default)

        // When - rapid calls
        repeat(5) {
            throttle.throttle(scope) {
                executionCount++
            }
            delay(50)
        }

        // Wait for all to complete
        delay(500)

        // Then - should execute fewer times due to throttling
        assertThat(executionCount).isLessThan(5)
        
        scope.close()
    }

    @Test
    fun `withTimeoutSafe should return value if completes in time`() = runTest {
        // Given
        val block: suspend () -> String = {
            delay(50)
            "Completed"
        }

        // When
        val result = withTimeoutSafe(
            timeout = 1.seconds,
            defaultValue = "Timeout"
        ) {
            block()
        }

        // Then
        assertThat(result).isEqualTo("Completed")
    }

    @Test
    fun `withTimeoutSafe should return default on timeout`() = runTest {
        // Given
        val block: suspend () -> String = {
            delay(200)
            "Completed"
        }

        // When
        val result = withTimeoutSafe(
            timeout = 50.milliseconds,
            defaultValue = "Timeout"
        ) {
            block()
        }

        // Then
        assertThat(result).isEqualTo("Timeout")
    }

    @Test
    fun `batchProcess should process items in batches`() = runTest {
        // Given
        val items = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val processedItems = mutableListOf<Int>()

        // When
        val result = batchProcess(
            items = items,
            batchSize = 3
        ) { batch ->
            processedItems.addAll(batch)
            batch.map { it * 2 }
        }

        // Then
        assertThat(result).containsExactly(2, 4, 6, 8, 10, 12, 14, 16, 18, 20)
        assertThat(processedItems).containsExactlyElementsIn(items)
    }

    @Test
    fun `cancellableOperation should return success result`() = runTest {
        // Given
        val block: suspend () -> String = { "Success" }

        // When
        val result = cancellableOperation(block)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo("Success")
    }

    @Test
    fun `cancellableOperation should return failure on exception`() = runTest {
        // Given
        val block: suspend () -> String = { throw RuntimeException("Test error") }

        // When
        val result = cancellableOperation(block)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(RuntimeException::class.java)
    }
}
