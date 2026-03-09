package com.sugarmunch.app.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Optimized Coroutine Utilities
 * Best practices for coroutine usage in SugarMunch
 */

/**
 * Application-wide coroutine scope with SupervisorJob
 * Use this for long-running operations that should survive configuration changes
 */
class AppCoroutineScope {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return scope.launch(context, start, block)
    }
    
    suspend fun <T> async(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> T
    ): kotlinx.coroutines.Deferred<T> {
        return scope.async(context, start, block)
    }
    
    fun cancel() {
        scope.cancel()
    }
}

/**
 * Retry with exponential backoff
 * @param maxRetries Maximum number of retry attempts
 * @param initialDelay Initial delay before first retry
 * @param maxDelay Maximum delay between retries
 * @param factor Multiplier for exponential backoff
 */
suspend fun <T> retryWithBackoff(
    maxRetries: Int = 3,
    initialDelay: Duration = 1.seconds,
    maxDelay: Duration = 30.seconds,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(maxRetries - 1) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            if (attempt == maxRetries - 1) throw e
            kotlinx.coroutines.delay(currentDelay.inWholeMilliseconds)
            currentDelay = (currentDelay * factor).coerceAtMost(maxDelay)
        }
    }
    return block() // Last attempt
}

/**
 * Flow retry with exponential backoff
 */
fun <T> Flow<T>.retryWithBackoff(
    maxRetries: Int = 3,
    initialDelay: Duration = 1.seconds,
    maxDelay: Duration = 30.seconds,
    factor: Double = 2.0,
    retryOnException: (Exception) -> Boolean = { true }
): Flow<T> {
    var currentDelay = initialDelay
    
    return retryWhen { cause, attempt ->
        if (attempt >= maxRetries - 1) return@retryWhen false
        if (cause is Exception && !retryOnException(cause)) return@retryWhen false
        
        kotlinx.coroutines.delay(currentDelay.inWholeMilliseconds)
        currentDelay = (currentDelay * factor).coerceAtMost(maxDelay)
        true
    }.flowOn(Dispatchers.IO)
}

/**
 * Run multiple operations in parallel and wait for all to complete
 * Returns list of results or throws first exception
 */
suspend fun <T> parallelExecute(
    dispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.Default,
    operations: List<suspend () -> T>
): List<T> = coroutineScope {
    operations.map { op ->
        async(dispatcher) { op() }
    }.awaitAll()
}

/**
 * Run multiple operations in parallel with individual error handling
 * Returns list of Result objects
 */
suspend fun <T> parallelExecuteSafe(
    dispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.Default,
    operations: List<suspend () -> T>
): List<Result<T>> = coroutineScope {
    operations.map { op ->
        async(dispatcher) {
            runCatching { op() }
        }
    }.awaitAll()
}

/**
 * Execute with timeout
 * @param timeout Duration to wait before cancelling
 */
suspend fun <T> withTimeoutSafe(
    timeout: Duration,
    defaultValue: T,
    block: suspend CoroutineScope.() -> T
): T {
    return try {
        kotlinx.coroutines.withTimeout(timeout.inWholeMilliseconds) {
            block()
        }
    } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
        defaultValue
    }
}

/**
 * Debounce rapid function calls
 * @param delayMs Delay in milliseconds before executing
 */
class Debouncer(private val delayMs: Long) {
    private var debounceJob: Job? = null
    
    fun <T> debounce(
        scope: CoroutineScope,
        block: suspend () -> T
    ): Job {
        debounceJob?.cancel()
        debounceJob = scope.launch {
            kotlinx.coroutines.delay(delayMs)
            block()
        }
        return debounceJob!!
    }
    
    fun cancel() {
        debounceJob?.cancel()
    }
}

/**
 * Throttle function calls to once per interval
 */
class Throttle(private val intervalMs: Long) {
    private var lastExecutionTime = 0L
    private var throttleJob: Job? = null
    
    fun throttle(
        scope: CoroutineScope,
        block: suspend () -> Unit
    ): Job {
        throttleJob?.cancel()
        
        val currentTime = System.currentTimeMillis()
        val timeSinceLastExecution = currentTime - lastExecutionTime
        
        throttleJob = scope.launch {
            if (timeSinceLastExecution >= intervalMs) {
                lastExecutionTime = currentTime
                block()
            } else {
                kotlinx.coroutines.delay(intervalMs - timeSinceLastExecution)
                lastExecutionTime = System.currentTimeMillis()
                block()
            }
        }
        
        return throttleJob!!
    }
    
    fun cancel() {
        throttleJob?.cancel()
    }
}

/**
 * Safe flow collection with error handling
 */
fun <T> Flow<T>.safeCollect(
    onError: suspend (Throwable) -> Unit = {}
): Flow<T> {
    return this.catch { exception ->
        onError(exception)
    }.flowOn(Dispatchers.IO)
}

/**
 * IO-optimized coroutine scope for database and disk operations
 */
object IOCoroutineScope {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return scope.launch(block = block)
    }

    suspend fun <T> async(block: suspend CoroutineScope.() -> T): kotlinx.coroutines.Deferred<T> {
        return scope.async(block = block)
    }
}

/**
 * Main-optimized coroutine scope for UI operations
 */
object MainCoroutineScope {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return scope.launch(block = block)
    }
}

/**
 * Default-optimized coroutine scope for CPU-intensive operations
 */
object DefaultCoroutineScope {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return scope.launch(block = block)
    }

    suspend fun <T> async(block: suspend CoroutineScope.() -> T): kotlinx.coroutines.Deferred<T> {
        return scope.async(block = block)
    }
}

/**
 * Execute operation with retry logic
 */
suspend fun <T> executeWithRetry(
    operation: suspend () -> T,
    maxRetries: Int = 3,
    retryDelay: Duration = 1.seconds,
    shouldRetry: (Exception) -> Boolean = { true }
): T {
    var lastException: Exception? = null
    
    repeat(maxRetries) { attempt ->
        try {
            return operation()
        } catch (e: Exception) {
            lastException = e
            if (!shouldRetry(e) || attempt == maxRetries - 1) {
                throw e
            }
            kotlinx.coroutines.delay(retryDelay.inWholeMilliseconds)
        }
    }
    
    throw lastException!!
}

/**
 * Batch operations for better performance
 */
suspend fun <T, R> batchProcess(
    items: List<T>,
    batchSize: Int = 10,
    process: suspend (List<T>) -> List<R>
): List<R> {
    return items.chunked(batchSize).flatMap { batch ->
        process(batch)
    }
}

/**
 * Rate limiter for API calls
 */
class RateLimiter(
    private val permitsPerSecond: Double,
    private val scope: CoroutineScope
) {
    private var nextAvailableTime = 0L
    private val minDelayMs = (1000 / permitsPerSecond).toLong()
    
    suspend fun <T> execute(block: suspend () -> T): T {
        val now = System.currentTimeMillis()
        val waitTime = nextAvailableTime - now
        
        if (waitTime > 0) {
            kotlinx.coroutines.delay(waitTime)
        }
        
        nextAvailableTime = System.currentTimeMillis() + minDelayMs
        return block()
    }
}

/**
 * Cancellable operation wrapper
 */
suspend fun <T> cancellableOperation(
    block: suspend () -> T
): Result<T> {
    return try {
        Result.success(block())
    } catch (e: kotlinx.coroutines.CancellationException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
