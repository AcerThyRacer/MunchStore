package com.sugarmunch.app.automation

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Core automation engine for SugarMunch
 * Manages task lifecycle, execution, and event handling
 */
class AutomationEngine private constructor(private val context: Context) {
    
    private val TAG = "AutomationEngine"
    
    private val repository = AutomationRepository.getInstance(context)
    private val triggerEvaluator = TriggerEvaluator(context)
    private val actionExecutor = ActionExecutor(context)
    private val conditionEvaluator = ConditionEvaluator(context)
    private val scheduler = TaskScheduler.getInstance(context)
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Active task monitors
    private val activeMonitors = ConcurrentHashMap<String, Job>()
    private val activeTasks = ConcurrentHashMap<String, AutomationTask>()
    private val isRunning = AtomicBoolean(false)
    
    // Event flows
    private val _taskStateFlow = MutableStateFlow<Map<String, TaskState>>(emptyMap())
    val taskStateFlow: StateFlow<Map<String, TaskState>> = _taskStateFlow.asStateFlow()
    
    private val _executionFlow = MutableSharedFlow<ExecutionRecord>(extraBufferCapacity = 64)
    val executionFlow: SharedFlow<ExecutionRecord> = _executionFlow.asSharedFlow()
    
    // ═════════════════════════════════════════════════════════════
    // TASK MANAGEMENT
    // ═════════════════════════════════════════════════════════════
    
    /**
     * Create a new automation task
     */
    suspend fun createTask(
        name: String,
        description: String,
        trigger: AutomationTrigger,
        actions: List<AutomationAction>,
        conditions: List<AutomationCondition> = emptyList(),
        templateId: String? = null
    ): AutomationTask {
        val task = AutomationTask(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            trigger = trigger,
            conditions = conditions,
            actions = actions,
            templateId = templateId,
            enabled = true
        )
        
        repository.saveTask(task)
        
        if (task.enabled) {
            startTaskMonitoring(task)
        }
        
        Log.d(TAG, "Created task: ${task.id} - ${task.name}")
        return task
    }
    
    /**
     * Delete a task by ID
     */
    suspend fun deleteTask(taskId: String): Boolean {
        stopTaskMonitoring(taskId)
        repository.deleteTask(taskId)
        scheduler.cancel(taskId)
        
        Log.d(TAG, "Deleted task: $taskId")
        return true
    }
    
    /**
     * Enable or disable a task
     */
    suspend fun setTaskEnabled(taskId: String, enabled: Boolean) {
        repository.setTaskEnabled(taskId, enabled)
        
        val task = repository.getTaskById(taskId)
        if (task != null) {
            if (enabled) {
                startTaskMonitoring(task.copy(enabled = true))
            } else {
                stopTaskMonitoring(taskId)
            }
        }
        
        Log.d(TAG, "Task $taskId ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Run a task immediately (manual execution)
     */
    suspend fun runTask(taskId: String, triggeredBy: String = "manual"): ExecutionRecord {
        val task = repository.getTaskById(taskId)
            ?: throw IllegalArgumentException("Task not found: $taskId")
        
        return executeTask(task, triggeredBy)
    }
    
    /**
     * Run any automation immediately without saving
     */
    suspend fun runOneTime(
        name: String,
        actions: List<AutomationAction>,
        conditions: List<AutomationCondition> = emptyList()
    ): ExecutionRecord {
        val tempTask = AutomationTask(
            id = "temp_${UUID.randomUUID()}",
            name = name,
            description = "One-time execution",
            trigger = AutomationTrigger.ManualTrigger(),
            conditions = conditions,
            actions = actions,
            enabled = true,
            isTemplate = false
        )
        
        return executeTask(tempTask, "one_time")
    }
    
    /**
     * Schedule a task for future execution
     */
    suspend fun scheduleTask(
        taskId: String,
        triggerTime: Long
    ) {
        val task = repository.getTaskById(taskId)
            ?: throw IllegalArgumentException("Task not found: $taskId")
        
        scheduler.schedule(task, triggerTime)
        Log.d(TAG, "Scheduled task $taskId for $triggerTime")
    }
    
    /**
     * Update an existing task
     */
    suspend fun updateTask(task: AutomationTask) {
        repository.saveTask(task)
        
        // Restart monitoring if needed
        stopTaskMonitoring(task.id)
        if (task.enabled) {
            startTaskMonitoring(task)
        }
        
        Log.d(TAG, "Updated task: ${task.id}")
    }
    
    /**
     * Get a task by ID
     */
    suspend fun getTask(taskId: String): AutomationTask? {
        return repository.getTaskById(taskId)
    }
    
    /**
     * Get all user-created automations
     */
    fun getAllTasks(): Flow<List<AutomationTask>> {
        return repository.getAllTasks()
    }
    
    /**
     * Get execution history
     */
    fun getExecutionHistory(limit: Int = 100): Flow<List<ExecutionRecord>> {
        return repository.getRecentHistory(limit)
    }
    
    // ═════════════════════════════════════════════════════════════
    // ENGINE LIFECYCLE
    // ═════════════════════════════════════════════════════════════
    
    /**
     * Start the automation engine
     */
    fun start() {
        if (isRunning.compareAndSet(false, true)) {
            scope.launch {
                loadAndMonitorTasks()
            }
            Log.d(TAG, "Automation engine started")
        }
    }
    
    /**
     * Stop the automation engine
     */
    fun stop() {
        if (isRunning.compareAndSet(true, false)) {
            activeMonitors.values.forEach { it.cancel() }
            activeMonitors.clear()
            activeTasks.clear()
            Log.d(TAG, "Automation engine stopped")
        }
    }
    
    /**
     * Shutdown the engine and cleanup resources
     */
    fun shutdown() {
        stop()
        scope.cancel()
        Log.d(TAG, "Automation engine shutdown")
    }
    
    // ═════════════════════════════════════════════════════════════
    // PRIVATE METHODS
    // ═════════════════════════════════════════════════════════════
    
    private suspend fun loadAndMonitorTasks() {
        try {
            val tasks = repository.getEnabledTasks()
            tasks.forEach { task ->
                startTaskMonitoring(task)
            }
            Log.d(TAG, "Monitoring ${tasks.size} tasks")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading tasks", e)
        }
    }
    
    private fun startTaskMonitoring(task: AutomationTask) {
        if (activeMonitors.containsKey(task.id)) {
            return // Already monitoring
        }
        
        activeTasks[task.id] = task
        
        val job = scope.launch {
            try {
                triggerEvaluator.evaluateTrigger(task.trigger)
                    .collect { triggerEvent ->
                        handleTrigger(task, triggerEvent)
                    }
            } catch (e: CancellationException) {
                // Normal cancellation
            } catch (e: Exception) {
                Log.e(TAG, "Error monitoring task ${task.id}", e)
                _taskStateFlow.update { 
                    it + (task.id to TaskState.Error(e.message ?: "Unknown error")) 
                }
            }
        }
        
        activeMonitors[task.id] = job
        _taskStateFlow.update { it + (task.id to TaskState.Monitoring) }
        
        Log.d(TAG, "Started monitoring task: ${task.id}")
    }
    
    private fun stopTaskMonitoring(taskId: String) {
        activeMonitors.remove(taskId)?.cancel()
        activeTasks.remove(taskId)
        _taskStateFlow.update { it - taskId }
        
        Log.d(TAG, "Stopped monitoring task: $taskId")
    }
    
    private suspend fun handleTrigger(task: AutomationTask, event: TriggerEvent) {
        Log.d(TAG, "Trigger fired for task ${task.id}: $event")
        
        // Check conditions
        val conditionsMet = if (task.conditions.isNotEmpty()) {
            task.conditions.all { conditionEvaluator.evaluateCondition(it) }
        } else {
            true
        }
        
        if (!conditionsMet) {
            Log.d(TAG, "Conditions not met for task ${task.id}")
            return
        }
        
        // Execute task
        executeTask(task, event.javaClass.simpleName)
    }
    
    private suspend fun executeTask(task: AutomationTask, triggeredBy: String): ExecutionRecord {
        val recordId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()
        
        // Create execution record
        var record = ExecutionRecord(
            id = recordId,
            taskId = task.id,
            taskName = task.name,
            triggerTime = startTime,
            status = ExecutionRecord.ExecutionStatus.RUNNING,
            actionsTotal = task.actions.size,
            triggeredBy = triggeredBy
        )
        
        // Emit and save initial record
        _executionFlow.emit(record)
        repository.addExecutionRecord(record)
        
        _taskStateFlow.update { it + (task.id to TaskState.Executing) }
        
        // Execute actions
        var actionsExecuted = 0
        var firstError: String? = null
        
        try {
            for ((index, action) in task.actions.withIndex()) {
                if (!isRunning.get()) {
                    record = record.copy(
                        status = ExecutionRecord.ExecutionStatus.CANCELLED,
                        completionTime = System.currentTimeMillis()
                    )
                    break
                }
                
                Log.d(TAG, "Executing action ${index + 1}/${task.actions.size} for task ${task.id}")
                
                val result = actionExecutor.executeAction(action)
                
                when (result) {
                    is ActionResult.Success -> {
                        actionsExecuted++
                        Log.d(TAG, "Action succeeded: ${result.message}")
                    }
                    is ActionResult.PartialSuccess -> {
                        actionsExecuted++
                        if (firstError == null) firstError = result.message
                        Log.w(TAG, "Action partially succeeded: ${result.message}")
                    }
                    is ActionResult.Failure -> {
                        if (firstError == null) firstError = result.message
                        Log.e(TAG, "Action failed: ${result.message}")
                        // Continue with next action or break based on error handling policy
                    }
                }
            }
            
            // Determine final status
            val status = when {
                actionsExecuted == 0 -> ExecutionRecord.ExecutionStatus.FAILED
                actionsExecuted < task.actions.size -> ExecutionRecord.ExecutionStatus.PARTIAL_SUCCESS
                firstError != null -> ExecutionRecord.ExecutionStatus.PARTIAL_SUCCESS
                else -> ExecutionRecord.ExecutionStatus.SUCCESS
            }
            
            record = record.copy(
                completionTime = System.currentTimeMillis(),
                status = status,
                actionsExecuted = actionsExecuted,
                errorMessage = firstError
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error executing task ${task.id}", e)
            record = record.copy(
                completionTime = System.currentTimeMillis(),
                status = ExecutionRecord.ExecutionStatus.FAILED,
                actionsExecuted = actionsExecuted,
                errorMessage = e.message
            )
        }
        
        // Update task execution stats
        repository.recordTaskExecution(task.id)
        repository.updateExecutionRecord(record)
        
        // Emit final record
        _executionFlow.emit(record)
        
        _taskStateFlow.update { it + (task.id to TaskState.Monitoring) }
        
        Log.d(TAG, "Task ${task.id} execution completed: ${record.status}")
        
        return record
    }
    
    // ═════════════════════════════════════════════════════════════
    // BATCH OPERATIONS
    // ═════════════════════════════════════════════════════════════
    
    /**
     * Enable all tasks
     */
    suspend fun enableAllTasks() {
        repository.getAllTasks().first().forEach { task ->
            setTaskEnabled(task.id, true)
        }
    }
    
    /**
     * Disable all tasks
     */
    suspend fun disableAllTasks() {
        repository.getAllTasks().first().forEach { task ->
            setTaskEnabled(task.id, false)
        }
    }
    
    /**
     * Delete all tasks
     */
    suspend fun deleteAllTasks() {
        repository.getAllTasks().first().forEach { task ->
            deleteTask(task.id)
        }
    }
    
    /**
     * Export all tasks to JSON
     */
    suspend fun exportTasks(): String {
        val tasks = repository.getAllTasks().first()
        return kotlinx.serialization.json.Json {
            prettyPrint = true
            encodeDefaults = true
        }.encodeToString(tasks)
    }
    
    /**
     * Import tasks from JSON
     */
    suspend fun importTasks(json: String): List<AutomationTask> {
        val tasks = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
        }.decodeFromString<List<AutomationTask>>(json)
        
        tasks.forEach { task ->
            val newTask = task.copy(
                id = UUID.randomUUID().toString(),
                createdAt = System.currentTimeMillis(),
                lastRunAt = null,
                runCount = 0
            )
            repository.saveTask(newTask)
        }
        
        return tasks
    }
    
    // ═════════════════════════════════════════════════════════════
    // COMPANION OBJECT
    // ═════════════════════════════════════════════════════════════
    
    companion object {
        @Volatile
        private var instance: AutomationEngine? = null
        
        fun getInstance(context: Context): AutomationEngine {
            return instance ?: synchronized(this) {
                instance ?: AutomationEngine(context.applicationContext).also {
                    instance = it
                }
            }
        }
        
        fun destroyInstance() {
            instance?.shutdown()
            instance = null
        }
    }
}

// ═════════════════════════════════════════════════════════════
// TASK STATE
// ═════════════════════════════════════════════════════════════

sealed class TaskState {
    object Monitoring : TaskState()
    object Executing : TaskState()
    data class Error(val message: String) : TaskState()
    object Paused : TaskState()
}

// ═════════════════════════════════════════════════════════════
// EXTENSION FUNCTIONS
// ═════════════════════════════════════════════════════════════

inline fun <T> MutableStateFlow<T>.update(function: (T) -> T) {
    value = function(value)
}
