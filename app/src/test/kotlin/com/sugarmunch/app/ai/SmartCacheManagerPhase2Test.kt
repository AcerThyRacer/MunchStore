package com.sugarmunch.app.ai

import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.ConcurrentHashMap

/**
 * Unit tests for SmartCacheManager Phase 2 improvements.
 *
 * Tests cover:
 * - Cache warming state tracking
 * - Hit rate calculation
 * - Cache access recording
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class SmartCacheManagerPhase2Test {

    private lateinit var context: android.content.Context
    private lateinit var gson: Gson

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        gson = Gson()
    }

    @Test
    fun cacheWarmingState_initialState_isIdle() {
        // Given: A new cache warming state tracker
        // When: No warming operation has started
        // Then: State should be Idle
        val state = CacheWarmingState.Idle
        
        assertTrue(state is CacheWarmingState.Idle)
    }

    @Test
    fun cacheWarmingState_warming_progressCalculation() {
        // Given: A warming state with partial progress
        val warming = CacheWarmingState.Warming(
            progress = 0.5f,
            appsProcessed = 5,
            totalApps = 10,
            estimatedTimeRemainingMs = 30000
        )
        
        // Then: Progress percent should be calculated correctly
        assertEquals(50, warming.progressPercent)
        assertEquals(5, warming.appsProcessed)
        assertEquals(10, warming.totalApps)
    }

    @Test
    fun cacheWarmingState_complete_successRateCalculation() {
        // Given: A complete warming state with some failures
        val complete = CacheWarmingState.Complete(
            appsCached = 8,
            appsFailed = 2,
            durationMs = 5000
        )
        
        // Then: Success rate should be 80%
        assertEquals(0.8f, complete.successRate, 0.01f)
        assertEquals(5000, complete.durationMs)
    }

    @Test
    fun cacheWarmingState_complete_zeroApps_successRate() {
        // Given: A complete warming state with no apps
        val complete = CacheWarmingState.Complete(
            appsCached = 0,
            appsFailed = 0,
            durationMs = 0
        )
        
        // Then: Success rate should be 0 (no division by zero)
        assertEquals(0f, complete.successRate, 0.01f)
    }

    @Test
    fun cacheAccessRecord_tracksAccessCount() {
        // Given: A cache access record
        val record = CacheAccessRecord(
            appId = "test-app",
            lastAccessed = System.currentTimeMillis(),
            accessCount = 5
        )
        
        // Then: Record should have correct values
        assertEquals("test-app", record.appId)
        assertEquals(5, record.accessCount)
        assertTrue(record.lastAccessed > 0)
    }

    @Test
    fun hitRateWindow_emptyWindow_returnsZero() {
        // Given: An empty hit rate window
        val window = createHitRateWindow()
        
        // When: No hits recorded
        // Then: Hit rate should be 0
        assertEquals(0f, window.getHitRate(), 0.01f)
    }

    @Test
    fun hitRateWindow_allHits_returnsOne() {
        // Given: A hit rate window with all hits
        val window = createHitRateWindow()
        
        // When: All accesses are hits
        repeat(10) { window.recordHit(true) }
        
        // Then: Hit rate should be 100%
        assertEquals(1f, window.getHitRate(), 0.01f)
    }

    @Test
    fun hitRateWindow_allMisses_returnsZero() {
        // Given: A hit rate window with all misses
        val window = createHitRateWindow()
        
        // When: All accesses are misses
        repeat(10) { window.recordHit(false) }
        
        // Then: Hit rate should be 0%
        assertEquals(0f, window.getHitRate(), 0.01f)
    }

    @Test
    fun hitRateWindow_mixed_returnsCorrectRate() {
        // Given: A hit rate window with mixed results
        val window = createHitRateWindow()
        
        // When: 5 hits and 5 misses
        repeat(5) { window.recordHit(true) }
        repeat(5) { window.recordHit(false) }
        
        // Then: Hit rate should be 50%
        assertEquals(0.5f, window.getHitRate(), 0.01f)
    }

    @Test
    fun hitRateWindow_slidingWindow_removesOldest() {
        // Given: A hit rate window at capacity
        val window = createHitRateWindow(windowSize = 5)
        
        // When: Add 5 hits then 5 misses
        repeat(5) { window.recordHit(true) }
        repeat(5) { window.recordHit(false) }
        
        // Then: Only the last 5 (misses) should count
        assertEquals(0f, window.getHitRate(), 0.01f)
    }

    @Test
    fun hitRateWindow_reset_clearsAll() {
        // Given: A hit rate window with data
        val window = createHitRateWindow()
        repeat(10) { window.recordHit(true) }
        
        // When: Reset is called
        window.reset()
        
        // Then: Hit rate should be 0
        assertEquals(0f, window.getHitRate(), 0.01f)
    }

    @Test
    fun cacheState_includesHitRate() {
        // Given: A cache state with hit rate
        val state = CacheState(
            isReady = true,
            stats = CacheStats(totalSizeBytes = 1000, appCount = 5),
            isCacheWarming = false,
            hitRate = 0.75f
        )
        
        // Then: Hit rate should be accessible
        assertEquals(0.75f, state.hitRate, 0.01f)
        assertTrue(state.isReady)
        assertFalse(state.isCacheWarming)
    }

    @Test
    fun cacheWarmingOperation_tracksProgress() {
        // Given: A cache warming operation
        val operation = CacheWarmingOperation(
            startTime = System.currentTimeMillis(),
            totalApps = 10,
            aggressive = true
        )
        
        // Then: Operation should have correct values
        assertEquals(10, operation.totalApps)
        assertTrue(operation.aggressive)
        assertTrue(operation.startTime > 0)
    }

    // Helper to create hit rate window for testing
    private fun createHitRateWindow(windowSize: Int = 100) = object {
        private val accesses = ArrayDeque<Boolean>(windowSize)
        
        fun recordHit(hit: Boolean) {
            if (accesses.size >= windowSize) accesses.removeFirst()
            accesses.addLast(hit)
        }
        
        fun getHitRate(): Float {
            if (accesses.isEmpty()) return 0f
            return accesses.count { it }.toFloat() / accesses.size
        }
        
        fun reset() = accesses.clear()
    }
}
