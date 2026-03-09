package com.sugarmunch.app.clan

import android.content.Context
import com.sugarmunch.app.clan.model.Clan
import com.sugarmunch.app.data.local.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Weekly clan goals and featured (winning) clan for Catalog banner.
 * Winning clan = top of leaderboard for current season; displayed on Candy Store front page.
 */
class ClanGoalsManager private constructor(private val context: Context) {
    private val clanDao = AppDatabase.getDatabase(context).clanDao()

    /** Clan currently featured as "winning" (e.g. rank 1 this week). Null if none. */
    val featuredWinningClan: Flow<Clan?> = clanDao.getTopClansFlow(1).map { list -> list.firstOrNull() }

    companion object {
        @Volatile
        private var instance: ClanGoalsManager? = null
        fun getInstance(context: Context): ClanGoalsManager =
            instance ?: synchronized(this) {
                instance ?: ClanGoalsManager(context.applicationContext).also { instance = it }
            }
    }
}
