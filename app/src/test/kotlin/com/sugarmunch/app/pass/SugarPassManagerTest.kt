package com.sugarmunch.app.pass

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.google.common.truth.Truth.assertThat
import com.sugarmunch.app.shop.ShopManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class SugarPassManagerTest {

    private lateinit var passManager: SugarPassManager
    private lateinit var xpManager: XpManager
    private lateinit var seasonManager: SeasonManager
    private lateinit var shopManager: ShopManager

    @Before
    fun setup() = runTest {
        val context = RuntimeEnvironment.getApplication()
        passManager = SugarPassManager.getInstance(context)
        xpManager = XpManager.getInstance(context)
        seasonManager = SeasonManager.getInstance(context)
        shopManager = ShopManager.getInstance(context)

        passDataStore(passManager).edit { it.clear() }
        passDataStore(xpManager).edit { it.clear() }
        passDataStore(seasonManager).edit { it.clear() }
        passDataStore(shopManager).edit { it.clear() }

        seasonManager.forceStartSeason(seasonManager.getCurrentSeasonInfo()!!.id)
    }

    @Test
    fun `claimReward should allow free rewards once tier is reached`() = runTest {
        val freeReward = SugarPassRewards.getAllRewards().first { it.track == RewardTrack.FREE }
        ensureTierReached(freeReward.tier)

        val result = passManager.claimReward(freeReward.tier, freeReward.track)
        val progress = passManager.seasonProgressData.filterNotNull().first()

        assertThat(result).isInstanceOf(RewardClaimResult.SUCCESS::class.java)
        assertThat(progress.claimedRewards).contains("${freeReward.tier}_${freeReward.track.name}")
    }

    @Test
    fun `claimReward should require premium for premium rewards`() = runTest {
        val premiumReward = SugarPassRewards.getAllRewards().first { it.track == RewardTrack.PREMIUM }
        ensureTierReached(premiumReward.tier)

        val result = passManager.claimReward(premiumReward.tier, premiumReward.track)

        assertThat(result).isEqualTo(RewardClaimResult.PREMIUM_REQUIRED)
    }

    @Test
    fun `buyPremium should unlock premium reward claims`() = runTest {
        val premiumReward = SugarPassRewards.getAllRewards().first { it.track == RewardTrack.PREMIUM }
        ensureTierReached(premiumReward.tier)
        shopManager.addSugarPoints(5000, "test_setup")

        val purchaseResult = passManager.buyPremium(useSugarPoints = true)
        val claimResult = passManager.claimReward(premiumReward.tier, premiumReward.track)

        assertThat(purchaseResult).isEqualTo(PurchaseResult.SUCCESS)
        assertThat(claimResult).isInstanceOf(RewardClaimResult.SUCCESS::class.java)
    }

    private suspend fun ensureTierReached(targetTier: Int) {
        var currentTier = xpManager.currentTier.first()
        var attempts = 0
        while (currentTier < targetTier && attempts < 10) {
            xpManager.addXp(1000, XpSource.OTHER, "test_tier_boost")
            currentTier = xpManager.currentTier.first()
            attempts++
        }
        assertThat(currentTier).isAtLeast(targetTier)
    }

    @Suppress("UNCHECKED_CAST")
    private fun passDataStore(instance: Any): DataStore<Preferences> {
        val field = instance.javaClass.getDeclaredField("dataStore")
        field.isAccessible = true
        return field.get(instance) as DataStore<Preferences>
    }
}
