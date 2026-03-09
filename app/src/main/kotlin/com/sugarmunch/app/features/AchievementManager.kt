package com.sugarmunch.app.features

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sugarmunch.app.features.model.*
import com.sugarmunch.app.pass.XpManager
import com.sugarmunch.app.shop.ShopManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*

private val Context.achievementDataStore: DataStore<Preferences> by preferencesDataStore(name = "sugarmunch_achievements")

/**
 * Achievement Manager - Tracks and unlocks achievements
 */
class AchievementManager private constructor(private val context: Context) {
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private val dataStore = context.achievementDataStore
    
    // Achievement state flows
    private val _userAchievements = MutableStateFlow<Map<String, UserAchievement>>(emptyMap())
    val userAchievements: StateFlow<Map<String, UserAchievement>> = _userAchievements.asStateFlow()
    
    val unlockedAchievements = _userAchievements.map { map ->
        map.values.filter { it.isUnlocked }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())
    
    val progressAchievements = _userAchievements.map { map ->
        map.values.filter { !it.isUnlocked && it.progress > 0 }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())
    
    val totalProgress = _userAchievements.map { map ->
        val unlocked = map.values.count { it.isUnlocked }
        val total = AchievementSystem.ALL_ACHIEVEMENTS.size
        unlocked.toFloat() / total
    }.stateIn(scope, SharingStarted.Eagerly, 0f)
    
    val sugarPoints = _userAchievements.map { map ->
        map.values.filter { it.isUnlocked }.sumOf { 
            when (AchievementSystem.getById(it.achievementId)?.rarity) {
                AchievementRarity.COMMON -> 10
                AchievementRarity.UNCOMMON -> 25
                AchievementRarity.RARE -> 50
                AchievementRarity.EPIC -> 100
                AchievementRarity.LEGENDARY -> 250
                else -> 0
            }
        }
    }.stateIn(scope, SharingStarted.Eagerly, 0)
    
    // Stats tracking
    private val _stats = MutableStateFlow(UserStats())
    val stats: StateFlow<UserStats> = _stats.asStateFlow()
    
    init {
        loadAchievements()
        initializeStats()
    }
    
    private fun loadAchievements() {
        scope.launch {
            dataStore.data.collect { prefs ->
                val achievements = mutableMapOf<String, UserAchievement>()
                
                AchievementSystem.ALL_ACHIEVEMENTS.forEach { achievement ->
                    val isUnlocked = prefs[booleanPreferencesKey("unlocked_${achievement.id}")] ?: false
                    val progress = prefs[intPreferencesKey("progress_${achievement.id}")] ?: 0
                    val unlockedAt = prefs[longPreferencesKey("unlocked_at_${achievement.id}")]
                    
                    achievements[achievement.id] = UserAchievement(
                        achievementId = achievement.id,
                        unlockedAt = unlockedAt,
                        progress = progress,
                        maxProgress = achievement.requirementValue
                    )
                }
                
                _userAchievements.value = achievements
            }
        }
    }
    
    private fun initializeStats() {
        scope.launch {
            dataStore.data.collect { prefs ->
                _stats.value = UserStats(
                    totalInstalls = prefs[intPreferencesKey("stat_installs")] ?: 0,
                    totalEffectsUsed = prefs[intPreferencesKey("stat_effects")] ?: 0,
                    totalThemesTried = prefs[intPreferencesKey("stat_themes")] ?: 0,
                    favoriteAppsCount = prefs[intPreferencesKey("stat_favorites")] ?: 0,
                    achievementsUnlocked = _userAchievements.value.values.count { it.isUnlocked },
                    currentStreak = prefs[intPreferencesKey("stat_streak")] ?: 0,
                    longestStreak = prefs[intPreferencesKey("stat_longest_streak")] ?: 0,
                    timeSpentMinutes = prefs[longPreferencesKey("stat_time")] ?: 0,
                    sugarPoints = sugarPoints.value
                )
            }
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // Track Progress Methods
    // ═════════════════════════════════════════════════════════════════
    
    fun trackAppInstall(appId: String) {
        incrementStat("stat_installs")
        incrementAchievementProgress("install_apps", 1)
        
        // Specific app achievements
        if (appId == "sugartube") {
            unlockAchievement("sugartube_fan")
        }
        
        // Time-based achievements
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hour >= 0 && hour < 6) {
            unlockAchievement("night_owl")
        } else if (hour >= 5 && hour < 7) {
            unlockAchievement("early_bird")
        }
    }
    
    fun trackAppBrowse() {
        incrementStat("stat_browse")
        incrementAchievementProgress("browse_apps", 1)
    }
    
    fun trackThemeChange(themeId: String) {
        incrementStat("stat_themes")
        
        // First theme
        if (_stats.value.totalThemesTried == 0) {
            unlockAchievement("first_theme")
        }
        
        incrementAchievementProgress("try_themes", 1)
        
        // Trippy theme check
        if (themeId.contains("trippy") || themeId.contains("acid")) {
            unlockAchievement("trippy_explorer")
        }
    }
    
    fun trackEffectEnabled(effectId: String) {
        incrementStat("stat_effects")
        
        // First effect
        if (_stats.value.totalEffectsUsed == 0) {
            unlockAchievement("effect_enthusiast")
        }
        
        incrementAchievementProgress("enable_effects", 1)
    }
    
    fun trackEffectPresetUsed(presetId: String, effectCount: Int) {
        if (effectCount >= 3) {
            unlockAchievement("combo_king")
        }
        
        if (presetId == "sugarrush_maximum") {
            unlockAchievement("sugarrush_mode")
        }
        
        if (presetId == "acid_trip") {
            unlockAchievement("trippy_master")
        }
    }
    
    fun trackIntensityMax(intensity: Float) {
        if (intensity >= 2f) {
            unlockAchievement("intensity_freak")
        }
    }
    
    fun trackFavorite(appId: String) {
        incrementStat("stat_favorites")
        
        if (_stats.value.favoriteAppsCount == 0) {
            unlockAchievement("first_favorite")
        }
        
        incrementAchievementProgress("favorite_apps", 1)
    }
    
    fun trackRating() {
        incrementAchievementProgress("rate_app", 1)
        incrementAchievementProgress("rate_apps", 1)
    }
    
    fun trackReview() {
        incrementAchievementProgress("write_reviews", 1)
    }
    
    fun trackShare() {
        incrementAchievementProgress("share_app", 1)
        incrementAchievementProgress("share_apps", 1)
    }
    
    fun trackCollectionCreated() {
        incrementAchievementProgress("create_collection", 1)
        incrementAchievementProgress("create_collections", 1)
    }
    
    fun trackStreak(days: Int) {
        scope.launch {
            dataStore.edit { prefs ->
                prefs[intPreferencesKey("stat_streak")] = days
                val longest = prefs[intPreferencesKey("stat_longest_streak")] ?: 0
                if (days > longest) {
                    prefs[intPreferencesKey("stat_longest_streak")] = days
                }
            }
        }
        
        // Check streak achievements
        when (days) {
            7 -> unlockAchievement("daily_user")
            30 -> unlockAchievement("dedicated")
            100 -> unlockAchievement("veteran")
            365 -> unlockAchievement("legend")
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // Achievement Operations
    // ═════════════════════════════════════════════════════════════════
    
    fun unlockAchievement(achievementId: String) {
        val current = _userAchievements.value[achievementId]
        if (current?.isUnlocked == true) return // Already unlocked
        val achievement = AchievementSystem.getById(achievementId) ?: return
        
        scope.launch {
            dataStore.edit { prefs ->
                prefs[booleanPreferencesKey("unlocked_$achievementId")] = true
                prefs[longPreferencesKey("unlocked_at_$achievementId")] = System.currentTimeMillis()
                prefs[intPreferencesKey("progress_$achievementId")] = 
                    achievement.requirementValue
            }

            ShopManager.getInstance(context).awardAchievementReward(achievement.rarity.toShopRarity())
            XpManager.getInstance(context).addAchievementXp(achievement.rarity)
        }
        
        // Check completionist achievement
        val unlockedCount = _userAchievements.value.values.count { it.isUnlocked } + 1
        if (unlockedCount >= 25) {
            unlockAchievement("achievement_hunter")
        }
        if (unlockedCount >= AchievementSystem.ALL_ACHIEVEMENTS.size) {
            unlockAchievement("completionist")
        }
    }
    
    private fun incrementAchievementProgress(requirementType: String, amount: Int) {
        AchievementSystem.ALL_ACHIEVEMENTS
            .filter { it.requirementType == requirementType }
            .forEach { achievement ->
                val current = _userAchievements.value[achievement.id]
                if (current?.isUnlocked != true) {
                    val newProgress = (current?.progress ?: 0) + amount
                    updateAchievementProgress(achievement.id, newProgress)
                    
                    if (newProgress >= achievement.requirementValue) {
                        unlockAchievement(achievement.id)
                    }
                }
            }
    }
    
    private fun updateAchievementProgress(achievementId: String, progress: Int) {
        scope.launch {
            dataStore.edit { prefs ->
                prefs[intPreferencesKey("progress_$achievementId")] = progress
            }
        }
    }
    
    private fun incrementStat(key: String) {
        scope.launch {
            dataStore.edit { prefs ->
                val current = prefs[intPreferencesKey(key)] ?: 0
                prefs[intPreferencesKey(key)] = current + 1
            }
        }
    }
    
    fun getAchievementProgress(achievementId: String): Float {
        val userAchievement = _userAchievements.value[achievementId]
        val achievement = AchievementSystem.getById(achievementId)
        return if (userAchievement != null && achievement != null) {
            userAchievement.progress.toFloat() / achievement.requirementValue
        } else 0f
    }
    
    fun isAchievementUnlocked(achievementId: String): Boolean {
        return _userAchievements.value[achievementId]?.isUnlocked ?: false
    }

    private fun AchievementRarity.toShopRarity(): Rarity {
        return when (this) {
            AchievementRarity.COMMON -> Rarity.COMMON
            AchievementRarity.UNCOMMON -> Rarity.UNCOMMON
            AchievementRarity.RARE -> Rarity.RARE
            AchievementRarity.EPIC -> Rarity.EPIC
            AchievementRarity.LEGENDARY -> Rarity.LEGENDARY
        }
    }
    
    companion object {
        @Volatile
        private var instance: AchievementManager? = null
        
        fun getInstance(context: Context): AchievementManager {
            return instance ?: synchronized(this) {
                instance ?: AchievementManager(context.applicationContext).also { 
                    instance = it 
                }
            }
        }
    }
}
