package com.sugarmunch.app.automation

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Unit tests for AutomationEngine.
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class AutomationEngineTest {

    private lateinit var engine: AutomationEngine

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        engine = AutomationEngine.getInstance(context)
    }

    @Test
    fun createTask_shouldPersistAndReturnTask() = runTest {
        val trigger = AutomationTrigger.TimeTrigger(hour = 12, minute = 0)
        val actions = listOf(AutomationAction.OpenAppAction(packageName = "com.example.app"))
        val task = engine.createTask(
            name = "Test task",
            description = "Description",
            trigger = trigger,
            actions = actions
        )
        assertThat(task.id).isNotEmpty()
        assertThat(task.name).isEqualTo("Test task")
        assertThat(task.actions).hasSize(1)
    }

    @Test
    fun deleteTask_shouldRemoveTask() = runTest {
        val trigger = AutomationTrigger.TimeTrigger(hour = 14, minute = 0)
        val actions = listOf(AutomationAction.OpenAppAction(packageName = "com.test.app"))
        val task = engine.createTask("To delete", "Desc", trigger, actions)
        val deleted = engine.deleteTask(task.id)
        assertThat(deleted).isTrue()
    }
}
