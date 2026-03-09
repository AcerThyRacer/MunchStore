package com.sugarmunch.app.effects.x2026

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.levelDataStore: DataStore<Preferences> by preferencesDataStore(name = "effects_2026_progression")

/**
 * 🎮 EFFECTS LEVEL SYSTEM - 2026 EDITION
 * Gamified progression with XP, levels, achievements, and unlocks!
 */

@Serializable
data class PlayerProgress(
    val currentLevel: Int = 1,
    val currentXp: Int = 0,
    val totalXpEarned: Int = 0,
    val effectsUnlocked: List<String> = emptyList(),
    val effectsUsed: Map<String, Int> = emptyMap(), // Effect ID to usage count
    val achievementsUnlocked: List<String> = emptyList(),
    val dailyStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastLoginDate: Long = 0,
    val favoriteEffect: String? = null,
    val timeSpentMinutes: Int = 0,
    val combosPerformed: Int = 0,
    val perfectActivations: Int = 0
)

@Serializable
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val xpReward: Int,
    val requirement: AchievementRequirement
) {
    @Serializable
    sealed class AchievementRequirement {
        data class LevelReached(val level: Int) : AchievementRequirement()
        data class EffectsOwned(val count: Int) : AchievementRequirement()
        data class EffectUsed(val effectId: String, val times: Int) : AchievementRequirement()
        data class StreakReached(val days: Int) : AchievementRequirement()
        data class TotalXp(val amount: Int) : AchievementRequirement()
        data class ComboPerformed(val count: Int) : AchievementRequirement()
        data class TimeSpent(val minutes: Int) : AchievementRequirement()
        data class SpecificEffects(val effectIds: List<String>) : AchievementRequirement()
    }
}

@Serializable
data class DailyChallenge(
    val id: String,
    val description: String,
    val emoji: String,
    val xpReward: Int,
    val requirement: ChallengeRequirement,
    val expiresAt: Long
) {
    @Serializable
    sealed class ChallengeRequirement {
        data class UseEffect(val effectId: String, val times: Int) : ChallengeRequirement()
        data class UseAnyEffect(val times: Int) : ChallengeRequirement()
        data class TryNewEffect(val count: Int) : ChallengeRequirement()
        data class CreateCombo(val count: Int) : ChallengeRequirement()
        data class ShareEffect(val count: Int) : ChallengeRequirement()
    }
}

@Serializable
data class EffectCombo(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val effectIds: List<String>,
    val xpMultiplier: Float,
    val unlockLevel: Int
)

object AchievementsCatalog {
    val ALL_ACHIEVEMENTS = listOf(
        // Beginner
        Achievement("first_steps", "First Steps", "Use your first 2026 effect", "👶", 50,
            Achievement.AchievementRequirement.EffectsOwned(1)),
        Achievement("collector", "Collector", "Unlock 5 effects", "📦", 100,
            Achievement.AchievementRequirement.EffectsOwned(5)),
        Achievement("enthusiast", "Enthusiast", "Unlock 10 effects", "🎒", 200,
            Achievement.AchievementRequirement.EffectsOwned(10)),
        Achievement("hoarder", "Hoarder", "Unlock 25 effects", "🎁", 500,
            Achievement.AchievementRequirement.EffectsOwned(25)),
        Achievement("completionist", "Completionist", "Unlock all effects", "🏆", 5000,
            Achievement.AchievementRequirement.EffectsOwned(Effects2026Catalog.TOTAL_EFFECTS)),
        
        // Usage
        Achievement("power_user", "Power User", "Use effects 100 times", "⚡", 200,
            Achievement.AchievementRequirement.TotalXp(1000)),
        Achievement("quantum_physicist", "Quantum Physicist", "Master all quantum effects", "⚛️", 500,
            Achievement.AchievementRequirement.SpecificEffects(
                listOf("quantum_flux", "particle_collider", "quantum_tunnel", "schrodinger_candy")
            )),
        Achievement("cosmic_explorer", "Cosmic Explorer", "Master all nebula effects", "🚀", 500,
            Achievement.AchievementRequirement.SpecificEffects(
                listOf("supernova", "black_hole", "nebula_cloud", "gravity_well", "dark_matter")
            )),
        Achievement("tech_guru", "Tech Guru", "Master all cybernetic effects", "💻", 500,
            Achievement.AchievementRequirement.SpecificEffects(
                listOf("ai_consciousness", "neural_network", "data_stream", "circuit_board")
            )),
        
        // Level
        Achievement("level_10", "Rising Star", "Reach level 10", "⭐", 100,
            Achievement.AchievementRequirement.LevelReached(10)),
        Achievement("level_25", "Seasoned User", "Reach level 25", "🌟", 250,
            Achievement.AchievementRequirement.LevelReached(25)),
        Achievement("level_50", "Half Century", "Reach level 50", "💫", 500,
            Achievement.AchievementRequirement.LevelReached(50)),
        Achievement("level_100", "Master of Candy", "Reach MAX LEVEL 100!", "👑", 10000,
            Achievement.AchievementRequirement.LevelReached(100)),
        
        // Streak
        Achievement("week_warrior", "Week Warrior", "7 day streak", "📅", 150,
            Achievement.AchievementRequirement.StreakReached(7)),
        Achievement("month_master", "Month Master", "30 day streak", "📆", 500,
            Achievement.AchievementRequirement.StreakReached(30)),
        Achievement("century_club", "Century Club", "100 day streak!", "💯", 2000,
            Achievement.AchievementRequirement.StreakReached(100)),
        
        // Special
        Achievement("combo_king", "Combo King", "Perform 50 combos", "🎭", 300,
            Achievement.AchievementRequirement.ComboPerformed(50)),
        Achievement("dedicated", "Dedicated", "Spend 10 hours with effects", "⏰", 400,
            Achievement.AchievementRequirement.TimeSpent(600)),
        Achievement("transcended", "TRANSCENDED", "Achieve TRANSCENDENCE", "🌈", 10000,
            Achievement.AchievementRequirement.EffectsOwned(Effects2026Catalog.TOTAL_EFFECTS))
    )
    
    fun getById(id: String) = ALL_ACHIEVEMENTS.find { it.id == id }
}

object EffectCombosCatalog {
    val ALL_COMBOS = listOf(
        EffectCombo("quantum_glitch", "Quantum Glitch", "Quantum Flux + Glitch Artifact", "⚡",
            listOf("quantum_flux", "glitch_artifact"), 1.5f, 10),
        EffectCombo("cyber_organism", "Cyber Organism", "AI Consciousness + Bio-Luminescence", "🧬",
            listOf("ai_consciousness", "bio_luminescence"), 2.0f, 25),
        EffectCombo("cosmic_dragon", "Cosmic Dragon", "Supernova + Dragon's Breath", "🐉",
            listOf("supernova", "dragon_breath"), 2.5f, 35),
        EffectCombo("time_warp", "Time Warp Combo", "Time Warp + Déjà Vu", "⏳",
            listOf("time_warp", "deja_vu"), 2.0f, 40),
        EffectCombo("matrix_biology", "Matrix Biology", "Digital Rain + Candy Ecosystem", "🌿",
            listOf("matrix_rain", "candy_ecosystem"), 3.0f, 45),
        EffectCombo("ultimate_power", "ULTIMATE POWER", "Black Hole + Transcendence", "💀",
            listOf("black_hole", "transcendence"), 10.0f, 100)
    )
    
    fun getAvailableCombos(unlockedEffects: List<String>, level: Int): List<EffectCombo> {
        return ALL_COMBOS.filter { combo ->
            combo.unlockLevel <= level && combo.effectIds.all { it in unlockedEffects }
        }
    }
}

class EffectsLevelManager private constructor(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }
    
    private val PROGRESS_KEY = stringPreferencesKey("player_progress")
    private val DAILY_CHALLENGE_KEY = stringPreferencesKey("daily_challenge")
    private val CHALLENGE_PROGRESS_KEY = stringPreferencesKey("challenge_progress")
    
    // State Flows
    private val _progress = MutableStateFlow(PlayerProgress())
    val progress: StateFlow<PlayerProgress> = _progress.asStateFlow()
    
    private val _currentChallenge = MutableStateFlow<DailyChallenge?>(null)
    val currentChallenge: StateFlow<DailyChallenge?> = _currentChallenge.asStateFlow()
    
    private val _availableUnlocks = MutableStateFlow<List<Effect2026>>(emptyList())
    val availableUnlocks: StateFlow<List<Effect2026>> = _availableUnlocks.asStateFlow()
    
    private val _newAchievements = MutableStateFlow<List<Achievement>>(emptyList())
    val newAchievements: StateFlow<List<Achievement>> = _newAchievements.asStateFlow()
    
    init {
        loadProgress()
        generateDailyChallenge()
    }
    
    // ═════════════════════════════════════════════════════════════════
    // PROGRESS MANAGEMENT
    // ═════════════════════════════════════════════════════════════════
    
    private fun loadProgress() {
        scope.launch {
            context.levelDataStore.data.collect { prefs ->
                prefs[PROGRESS_KEY]?.let { jsonStr ->
                    try {
                        _progress.value = json.decodeFromString(jsonStr)
                        updateAvailableUnlocks()
                    } catch (e: Exception) {
                        // Use defaults
                    }
                }
                
                prefs[DAILY_CHALLENGE_KEY]?.let { jsonStr ->
                    try {
                        _currentChallenge.value = json.decodeFromString(jsonStr)
                        // Check if expired
                        if (_currentChallenge.value?.expiresAt ?: 0 < System.currentTimeMillis()) {
                            generateDailyChallenge()
                        }
                    } catch (e: Exception) {
                        generateDailyChallenge()
                    }
                }
            }
        }
    }
    
    private fun saveProgress() {
        scope.launch {
            context.levelDataStore.edit { prefs ->
                prefs[PROGRESS_KEY] = json.encodeToString(_progress.value)
            }
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // XP & LEVELING
    // ═════════════════════════════════════════════════════════════════
    
    fun addXp(amount: Int, source: String = ""): Boolean {
        val current = _progress.value
        val newTotalXp = current.currentXp + amount
        val xpForNextLevel = getXpForLevel(current.currentLevel + 1)
        
        var newLevel = current.currentLevel
        var remainingXp = newTotalXp
        var leveledUp = false
        
        // Check for level ups
        while (remainingXp >= getXpForLevel(newLevel + 1) && newLevel < Effects2026Catalog.MAX_LEVEL) {
            remainingXp -= getXpForLevel(newLevel + 1)
            newLevel++
            leveledUp = true
            onLevelUp(newLevel)
        }
        
        _progress.value = current.copy(
            currentLevel = newLevel,
            currentXp = remainingXp,
            totalXpEarned = current.totalXpEarned + amount
        )
        
        saveProgress()
        checkAchievements()
        
        return leveledUp
    }
    
    private fun getXpForLevel(level: Int): Int {
        // Exponential XP curve
        return when {
            level <= 10 -> level * 100
            level <= 25 -> 1000 + (level - 10) * 200
            level <= 50 -> 4000 + (level - 25) * 500
            level <= 75 -> 16500 + (level - 50) * 1000
            level <= 99 -> 41500 + (level - 75) * 2000
            level == 100 -> 100000 // Max level
            else -> Int.MAX_VALUE
        }
    }
    
    fun getXpProgress(): Pair<Int, Int> {
        val current = _progress.value
        val currentLevelXp = getXpForLevel(current.currentLevel)
        val nextLevelXp = getXpForLevel(current.currentLevel + 1)
        return Pair(current.currentXp, nextLevelXp - currentLevelXp)
    }
    
    fun getXpToNextLevel(): Int {
        val (current, needed) = getXpProgress()
        return needed - current
    }
    
    private fun onLevelUp(newLevel: Int) {
        // Unlock effects at this level
        updateAvailableUnlocks()
        
        // Bonus XP for streaks
        val streakBonus = _progress.value.dailyStreak * 10
        if (streakBonus > 0) {
            _progress.value = _progress.value.copy(
                currentXp = _progress.value.currentXp + streakBonus
            )
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // EFFECT UNLOCKS
    // ═════════════════════════════════════════════════════════════════
    
    fun canUnlockEffect(effect: Effect2026): Boolean {
        val progress = _progress.value
        if (effect.levelRequired > progress.currentLevel) return false
        if (progress.effectsUnlocked.contains(effect.id)) return false
        
        effect.unlockRequirements?.let { req ->
            if (progress.totalXpEarned < req.xpNeeded) return false
            if (!progress.effectsUnlocked.containsAll(req.effectsOwned)) return false
            if (progress.dailyStreak < req.streakDays) return false
            if (!progress.achievementsUnlocked.containsAll(req.achievementsNeeded)) return false
        }
        
        return true
    }
    
    fun unlockEffect(effectId: String): Boolean {
        val effect = Effects2026Catalog.getById(effectId) ?: return false
        if (!canUnlockEffect(effect)) return false
        
        val current = _progress.value
        _progress.value = current.copy(
            effectsUnlocked = current.effectsUnlocked + effectId
        )
        
        saveProgress()
        updateAvailableUnlocks()
        
        // Award XP for unlocking
        addXp(effect.xpValue, "unlock")
        
        return true
    }
    
    private fun updateAvailableUnlocks() {
        val progress = _progress.value
        _availableUnlocks.value = Effects2026Catalog.ALL_EFFECTS_2026.filter { effect ->
            canUnlockEffect(effect)
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // EFFECT USAGE
    // ═════════════════════════════════════════════════════════════════
    
    fun recordEffectUse(effectId: String, durationSeconds: Int = 0) {
        val current = _progress.value
        val currentCount = current.effectsUsed[effectId] ?: 0
        
        _progress.value = current.copy(
            effectsUsed = current.effectsUsed + (effectId to currentCount + 1),
            timeSpentMinutes = current.timeSpentMinutes + (durationSeconds / 60)
        )
        
        saveProgress()
        
        // Award XP based on effect
        Effects2026Catalog.getById(effectId)?.let { effect ->
            val xp = (effect.xpValue * 0.1f).toInt().coerceAtLeast(5)
            addXp(xp, "usage")
        }
        
        // Check challenge progress
        checkChallengeProgress(effectId)
    }
    
    fun recordCombo(comboId: String) {
        val current = _progress.value
        _progress.value = current.copy(combosPerformed = current.combosPerformed + 1)
        
        EffectCombosCatalog.ALL_COMBOS.find { it.id == comboId }?.let { combo ->
            val bonusXp = (combo.xpMultiplier * 50).toInt()
            addXp(bonusXp, "combo")
        }
        
        saveProgress()
    }
    
    // ═════════════════════════════════════════════════════════════════
    // ACHIEVEMENTS
    // ═════════════════════════════════════════════════════════════════
    
    private fun checkAchievements() {
        val progress = _progress.value
        val newUnlocks = mutableListOf<Achievement>()
        
        AchievementsCatalog.ALL_ACHIEVEMENTS.forEach { achievement ->
            if (!progress.achievementsUnlocked.contains(achievement.id)) {
                if (isAchievementComplete(achievement, progress)) {
                    newUnlocks.add(achievement)
                    _progress.value = progress.copy(
                        achievementsUnlocked = progress.achievementsUnlocked + achievement.id
                    )
                    addXp(achievement.xpReward, "achievement")
                }
            }
        }
        
        if (newUnlocks.isNotEmpty()) {
            _newAchievements.value = newUnlocks
            saveProgress()
        }
    }
    
    private fun isAchievementComplete(achievement: Achievement, progress: PlayerProgress): Boolean {
        return when (val req = achievement.requirement) {
            is Achievement.AchievementRequirement.LevelReached -> progress.currentLevel >= req.level
            is Achievement.AchievementRequirement.EffectsOwned -> progress.effectsUnlocked.size >= req.count
            is Achievement.AchievementRequirement.EffectUsed -> {
                progress.effectsUsed[req.effectId] ?: 0 >= req.times
            }
            is Achievement.AchievementRequirement.StreakReached -> progress.dailyStreak >= req.days
            is Achievement.AchievementRequirement.TotalXp -> progress.totalXpEarned >= req.amount
            is Achievement.AchievementRequirement.ComboPerformed -> progress.combosPerformed >= req.count
            is Achievement.AchievementRequirement.TimeSpent -> progress.timeSpentMinutes >= req.minutes
            is Achievement.AchievementRequirement.SpecificEffects -> {
                progress.effectsUnlocked.containsAll(req.effectIds)
            }
        }
    }
    
    fun clearNewAchievements() {
        _newAchievements.value = emptyList()
    }
    
    // ═════════════════════════════════════════════════════════════════
    // DAILY CHALLENGES
    // ═════════════════════════════════════════════════════════════════
    
    private fun generateDailyChallenge() {
        val challenges = listOf(
            DailyChallenge(
                id = "daily_use",
                description = "Use any effect 5 times",
                emoji = "🎯",
                xpReward = 100,
                requirement = DailyChallenge.ChallengeRequirement.UseAnyEffect(5),
                expiresAt = System.currentTimeMillis() + 24 * 60 * 60 * 1000
            ),
            DailyChallenge(
                id = "daily_try_new",
                description = "Try 3 different effects",
                emoji = "🎨",
                xpReward = 150,
                requirement = DailyChallenge.ChallengeRequirement.TryNewEffect(3),
                expiresAt = System.currentTimeMillis() + 24 * 60 * 60 * 1000
            ),
            DailyChallenge(
                id = "daily_combo",
                description = "Perform 2 combos",
                emoji = "⚡",
                xpReward = 200,
                requirement = DailyChallenge.ChallengeRequirement.CreateCombo(2),
                expiresAt = System.currentTimeMillis() + 24 * 60 * 60 * 1000
            )
        )
        
        _currentChallenge.value = challenges.random()
        
        scope.launch {
            context.levelDataStore.edit { prefs ->
                _currentChallenge.value?.let {
                    prefs[DAILY_CHALLENGE_KEY] = json.encodeToString(it)
                }
            }
        }
    }
    
    private fun checkChallengeProgress(effectId: String) {
        val challenge = _currentChallenge.value ?: return
        
        val completed = when (val req = challenge.requirement) {
            is DailyChallenge.ChallengeRequirement.UseEffect -> effectId == req.effectId
            is DailyChallenge.ChallengeRequirement.UseAnyEffect -> true
            else -> false
        }
        
        if (completed) {
            // Track progress in datastore
            // Award XP when complete
        }
    }
    
    fun completeDailyChallenge(): Boolean {
        val challenge = _currentChallenge.value ?: return false
        
        addXp(challenge.xpReward, "daily_challenge")
        generateDailyChallenge()
        return true
    }
    
    // ═════════════════════════════════════════════════════════════════
    // STREAK MANAGEMENT
    // ═════════════════════════════════════════════════════════════════
    
    fun checkDailyStreak() {
        val current = _progress.value
        val now = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000
        
        val daysSinceLastLogin = (now - current.lastLoginDate) / dayInMillis
        
        val newStreak = when {
            daysSinceLastLogin <= 1 -> current.dailyStreak + 1
            else -> 1 // Reset streak
        }
        
        _progress.value = current.copy(
            dailyStreak = newStreak.toInt(),
            longestStreak = maxOf(current.longestStreak, newStreak.toInt()),
            lastLoginDate = now
        )
        
        // Streak bonus XP
        if (newStreak > 1) {
            addXp((newStreak * 10).toInt(), "streak")
        }
        
        saveProgress()
        checkAchievements()
    }
    
    // ═════════════════════════════════════════════════════════════════
    // COMPANION
    // ═════════════════════════════════════════════════════════════════
    
    companion object {
        @Volatile
        private var instance: EffectsLevelManager? = null
        
        fun getInstance(context: Context): EffectsLevelManager {
            return instance ?: synchronized(this) {
                instance ?: EffectsLevelManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
        
        fun destroy() {
            instance = null
        }
    }
}
