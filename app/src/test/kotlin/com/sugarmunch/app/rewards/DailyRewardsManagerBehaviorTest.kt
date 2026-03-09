package com.sugarmunch.app.rewards

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class DailyRewardsManagerBehaviorTest {

    private lateinit var manager: DailyRewardsManager

    @Before
    fun setup() = runTest {
        val context = RuntimeEnvironment.getApplication()
        manager = DailyRewardsManager.getInstance(context)
        rewardsDataStore(manager).edit { it.clear() }
    }

    @Test
    fun `claimDailyReward should award a new streak and persist totals`() = runTest {
        val result = manager.claimDailyReward()

        assertThat(result).isInstanceOf(ClaimResult.SUCCESS::class.java)
        assertThat(manager.currentStreak.first()).isEqualTo(1)
        assertThat(manager.totalClaims.first()).isEqualTo(1)
        assertThat(manager.canClaimToday.first()).isFalse()
        assertThat(manager.claimHistory.first()).hasSize(1)
    }

    @Test
    fun `streakStatus should become at risk inside grace window`() = runTest {
        val twentySixHoursAgo = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(26)
        rewardsDataStore(manager).edit { prefs ->
            prefs[intPreferencesKey("current_streak")] = 5
            prefs[longPreferencesKey("last_claim_date")] = twentySixHoursAgo
        }

        val status = manager.streakStatus.first()

        assertThat(status).isInstanceOf(StreakStatus.AT_RISK::class.java)
        val atRisk = status as StreakStatus.AT_RISK
        assertThat(atRisk.currentStreak).isEqualTo(5)
        assertThat(atRisk.hoursRemaining).isGreaterThan(0)
    }

    @Suppress("UNCHECKED_CAST")
    private fun rewardsDataStore(manager: DailyRewardsManager): DataStore<Preferences> {
        val field = DailyRewardsManager::class.java.getDeclaredField("dataStore")
        field.isAccessible = true
        return field.get(manager) as DataStore<Preferences>
    }
}
