package com.sugarmunch.app.events

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class ChallengeProgressManagerTest {

    private lateinit var manager: ChallengeProgressManager

    @Before
    fun setup() = runTest {
        val context = RuntimeEnvironment.getApplication()
        manager = ChallengeProgressManager.getInstance(context)
        challengeDataStore(manager).edit { it.clear() }
    }

    @Test
    fun `updateProgress should complete challenge at target value`() = runTest {
        val progress = manager.updateProgress("winter_daily_login")

        assertThat(progress).isNotNull()
        assertThat(progress?.currentValue).isEqualTo(1)
        assertThat(progress?.isCompleted).isTrue()
        assertThat(progress?.canClaim).isTrue()
    }

    @Test
    fun `claimChallengeReward should mark challenge as claimed`() = runTest {
        manager.updateProgress("winter_daily_login")

        val claimed = manager.claimChallengeReward("winter_daily_login")
        val progress = manager.getProgress("winter_daily_login")

        assertThat(claimed).isTrue()
        assertThat(progress?.isClaimed).isTrue()
    }

    @Test
    fun `resetDailyChallenges should clear existing progress`() = runTest {
        manager.updateProgress("winter_daily_login")
        manager.resetDailyChallenges()

        val progress = manager.getProgress("winter_daily_login")

        assertThat(progress?.currentValue).isEqualTo(0)
        assertThat(progress?.isCompleted).isFalse()
        assertThat(progress?.isClaimed).isFalse()
    }

    @Suppress("UNCHECKED_CAST")
    private fun challengeDataStore(manager: ChallengeProgressManager): DataStore<Preferences> {
        val field = ChallengeProgressManager::class.java.getDeclaredField("dataStore")
        field.isAccessible = true
        return field.get(manager) as DataStore<Preferences>
    }
}
