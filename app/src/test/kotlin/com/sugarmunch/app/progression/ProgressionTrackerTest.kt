package com.sugarmunch.app.progression

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.google.common.truth.Truth.assertThat
import com.sugarmunch.app.events.ChallengeProgressManager
import com.sugarmunch.app.features.AchievementManager
import com.sugarmunch.app.pass.XpManager
import com.sugarmunch.app.shop.ShopManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class ProgressionTrackerTest {

    private lateinit var tracker: ProgressionTracker
    private lateinit var achievementManager: AchievementManager
    private lateinit var xpManager: XpManager
    private lateinit var challengeManager: ChallengeProgressManager
    private lateinit var shopManager: ShopManager

    @Before
    fun setup() = runTest {
        val context = RuntimeEnvironment.getApplication()
        tracker = ProgressionTracker.getInstance(context)
        achievementManager = AchievementManager.getInstance(context)
        xpManager = XpManager.getInstance(context)
        challengeManager = ChallengeProgressManager.getInstance(context)
        shopManager = ShopManager.getInstance(context)

        privateDataStore(achievementManager).edit { it.clear() }
        privateDataStore(xpManager).edit { it.clear() }
        privateDataStore(challengeManager).edit { it.clear() }
        privateDataStore(shopManager).edit { it.clear() }
    }

    @Test
    fun `onShareCompleted should unlock achievements and award progression`() = runTest {
        tracker.onShareCompleted()
        delay(300)

        val xpBreakdown = xpManager.xpBreakdown.first()
        val shareChallenge = challengeManager.getProgress("winter_share_warmth")

        assertThat(achievementManager.isAchievementUnlocked("social_butterfly")).isTrue()
        assertThat(xpBreakdown.fromAchievements).isGreaterThan(0)
        assertThat(xpBreakdown.fromOther).isGreaterThan(0)
        assertThat(shareChallenge?.currentValue).isEqualTo(1)
    }

    @Suppress("UNCHECKED_CAST")
    private fun privateDataStore(instance: Any): DataStore<Preferences> {
        val field = instance.javaClass.getDeclaredField("dataStore")
        field.isAccessible = true
        return field.get(instance) as DataStore<Preferences>
    }
}
