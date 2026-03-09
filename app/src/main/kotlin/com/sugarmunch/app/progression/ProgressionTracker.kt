package com.sugarmunch.app.progression

import android.content.Context
import com.sugarmunch.app.events.ChallengeProgressManager
import com.sugarmunch.app.events.ChallengeType
import com.sugarmunch.app.events.EventChallenges
import com.sugarmunch.app.features.AchievementManager
import com.sugarmunch.app.pass.XpManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Small integration layer that turns feature actions into progression updates.
 *
 * This keeps the existing managers as the source of truth while avoiding
 * duplicated achievement, XP, and quest wiring across screens.
 */
class ProgressionTracker private constructor(
    private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val achievementManager: AchievementManager by lazy {
        AchievementManager.getInstance(context)
    }

    private val xpManager: XpManager by lazy {
        XpManager.getInstance(context)
    }

    private val challengeManager: ChallengeProgressManager by lazy {
        ChallengeProgressManager.getInstance(context)
    }

    fun onDailyRewardClaimed(streak: Int) {
        scope.launch {
            achievementManager.trackStreak(streak)
            xpManager.addDailyLoginXp()
            updateChallengesByType(ChallengeType.DAILY_LOGIN)
            updateStreakChallenges(streak)
        }
    }

    fun onThemeChanged(themeId: String) {
        scope.launch {
            achievementManager.trackThemeChange(themeId)
            xpManager.addThemeChangedXp()
            updateChallengesByType(ChallengeType.USE_THEME)
        }
    }

    fun onThemeIntensityChanged(intensity: Float) {
        if (intensity >= 2f) {
            achievementManager.trackIntensityMax(intensity)
        }
    }

    fun onEffectEnabled(effectId: String) {
        scope.launch {
            achievementManager.trackEffectEnabled(effectId)
            xpManager.addEffectUsedXp()
        }
    }

    fun onEffectPresetUsed(presetId: String, effectCount: Int) {
        scope.launch {
            achievementManager.trackEffectPresetUsed(presetId, effectCount)
        }
    }

    fun onAppInstalled(appId: String) {
        scope.launch {
            achievementManager.trackAppInstall(appId)
            xpManager.addAppInstallXp()
            updateChallengesByType(ChallengeType.INSTALL_APPS)
            maybeUnlockNightInstallChallenge()
        }
    }

    fun onShareCompleted() {
        scope.launch {
            achievementManager.trackShare()
            xpManager.addShareXp()
            updateChallengesByType(ChallengeType.SHARE_APP)
        }
    }

    private suspend fun updateChallengesByType(type: ChallengeType, increment: Int = 1) {
        EventChallenges.ALL_CHALLENGES
            .asSequence()
            .filter { it.type == type }
            .forEach { challenge ->
                challengeManager.updateProgress(challenge.id, increment)
            }
    }

    private suspend fun updateStreakChallenges(streak: Int) {
        EventChallenges.ALL_CHALLENGES
            .asSequence()
            .filter { it.type == ChallengeType.STREAK_DAYS }
            .forEach { challenge ->
                challengeManager.setProgress(challenge.id, streak)
            }
    }

    private suspend fun maybeUnlockNightInstallChallenge() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hour in 0..2) {
            challengeManager.updateProgress("halloween_night_install")
        }
    }

    companion object {
        @Volatile
        private var instance: ProgressionTracker? = null

        fun getInstance(context: Context): ProgressionTracker {
            return instance ?: synchronized(this) {
                instance ?: ProgressionTracker(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
