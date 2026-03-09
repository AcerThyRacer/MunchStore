package com.sugarmunch.app.bounties

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sugarmunch.app.shop.ShopManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
private val Context.bountyDataStore: DataStore<Preferences> by preferencesDataStore(name = "bounties")

/**
 * Local-first bounty repository. Replace with API when backend is available.
 */
class BountyRepository(private val context: Context) {
    private val dataStore = context.bountyDataStore
    private val shopManager = ShopManager.getInstance(context)
    private val claimedKey = stringSetPreferencesKey("claimed_bounty_ids")

    private val _bounties = mutableListOf<Bounty>(
        Bounty("b1", "sugartube", BountyType.REVIEW_5_STAR, 50, "5-star review", "Leave a 5-star review for Sugartube", null),
        Bounty("b2", "lollipoplauncher", BountyType.FIND_BUG, 100, "Bug hunter", "Report a bug in LollipopLauncher", null)
    )

    fun getBountiesForApp(appId: String): Flow<List<Bounty>> = dataStore.data.map { prefs ->
        val claimed = prefs[claimedKey] ?: emptySet()
        _bounties.filter { it.appId == appId }.map { b ->
            b.copy(isClaimed = b.id in claimed)
        }
    }

    suspend fun claimBounty(bountyId: String): Boolean {
        val bounty = _bounties.find { it.id == bountyId } ?: return false
        dataStore.edit { prefs ->
            val claimed = prefs[claimedKey] ?: emptySet()
            if (bountyId in claimed) return@edit
            prefs[claimedKey] = claimed + bountyId
        }
        shopManager.addSugarPoints(bounty.rewardAmount, "bounty_${bounty.type.name}")
        return true
    }
}
