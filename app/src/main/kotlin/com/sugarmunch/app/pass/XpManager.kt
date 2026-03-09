package com.sugarmunch.app.pass

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sugarmunch.app.features.AchievementManager
import com.sugarmunch.app.shop.ShopManager
import kotlinx.coroutines.flow.*
import java.util.*
import java.util.concurrent.TimeUnit

private val Context.xpDataStore: DataStore<Preferences> by preferencesDataStore(name = "sugar_pass_xp")

/**
 * XP Manager - Handles all XP earning for Sugar Pass
 * 
 * XP Sources:
 * - Daily Login: 50-500 XP (streak based)
 * - App Installs: 25-100 XP per install
 * - Effects Used: 10 XP per use (cooldown)
 * - Themes Changed: 15 XP per change
 * - Achievements: 50-500 XP based on rarity
 * - Sugar Pass Bonus: Up to +50% XP multiplier
 * 
 * Daily/Weekly Caps prevent abuse
 */
class XpManager private constructor(private val context: Context) {

    private val dataStore = context.xpDataStore
    private val shopManager = ShopManager.getInstance(context)
    private val achievementManager = AchievementManager.getInstance(context)

    // ═════════════════════════════════════════════════════════════
    // XP VALUES
    // ═════════════════════════════════════════════════════════════
    companion object {
        // Base XP amounts
        const val XP_DAILY_LOGIN_BASE = 50
        const val XP_DAILY_LOGIN_STREAK_BONUS = 25 // Per day of streak
        const val XP_DAILY_LOGIN_MAX = 500
        
        const val XP_APP_INSTALL = 50
        const val XP_APP_INSTALL_BONUS = 25 // Premium apps
        
        const val XP_EFFECT_USED = 10
        const val XP_EFFECT_USED_COOLDOWN_MINUTES = 5
        
        const val XP_THEME_CHANGED = 15
        const val XP_THEME_CHANGED_COOLDOWN_MINUTES = 10
        
        const val XP_ACHIEVEMENT_COMMON = 50
        const val XP_ACHIEVEMENT_UNCOMMON = 100
        const val XP_ACHIEVEMENT_RARE = 200
        const val XP_ACHIEVEMENT_EPIC = 350
        const val XP_ACHIEVEMENT_LEGENDARY = 500
        
        const val XP_SHOP_PURCHASE = 25
        const val XP_SHARE_APP = 30
        const val XP_REVIEW_APP = 40
        const val XP_COMPLETE_ONBOARDING = 100
        const val XP_REFERRAL = 200
        
        /** Sugar Crystals earned per indie app try (install or first open). */
        const val CRYSTALS_PER_INDIE_APP_TRY = 50
        
        // Caps
        const val DAILY_XP_CAP = 5000
        const val WEEKLY_XP_CAP = 25000
        
        // Special events
        const val XP_WEEKEND_MULTIPLIER = 1.25f
        const val XP_EVENT_MULTIPLIER = 2.0f
    }

    // ═════════════════════════════════════════════════════════════
    // PREFERENCES KEYS
    // ═════════════════════════════════════════════════════════════
    private object Keys {
        // XP Tracking
        val TOTAL_XP = intPreferencesKey("total_xp")
        val CURRENT_TIER = intPreferencesKey("current_tier")
        
        // Daily tracking
        val DAILY_XP_EARNED = intPreferencesKey("daily_xp_earned")
        val DAILY_XP_DATE = longPreferencesKey("daily_xp_date")
        
        // Weekly tracking
        val WEEKLY_XP_EARNED = intPreferencesKey("weekly_xp_earned")
        val WEEKLY_XP_WEEK = intPreferencesKey("weekly_xp_week")
        
        // Source tracking (for analytics)
        val XP_FROM_LOGIN = intPreferencesKey("xp_from_login")
        val XP_FROM_INSTALLS = intPreferencesKey("xp_from_installs")
        val XP_FROM_EFFECTS = intPreferencesKey("xp_from_effects")
        val XP_FROM_THEMES = intPreferencesKey("xp_from_themes")
        val XP_FROM_ACHIEVEMENTS = intPreferencesKey("xp_from_achievements")
        val XP_FROM_SHOP = intPreferencesKey("xp_from_shop")
        val XP_FROM_OTHER = intPreferencesKey("xp_from_other")
        
        // Cooldown tracking
        val LAST_EFFECT_XP_TIME = longPreferencesKey("last_effect_xp_time")
        val LAST_THEME_XP_TIME = longPreferencesKey("last_theme_xp_time")
        
        // Streak
        val LOGIN_STREAK = intPreferencesKey("login_streak")
        val LAST_LOGIN_DATE = longPreferencesKey("last_login_date")
        
        // History
        val XP_HISTORY = stringPreferencesKey("xp_history")
        
        // Sugar Crystals (earned for trying indie apps, spendable in pass/shop)
        val SUGAR_CRYSTALS = intPreferencesKey("sugar_crystals")
    }

    // ═════════════════════════════════════════════════════════════
    // STATE FLOWS
    // ═════════════════════════════════════════════════════════════
    
    val totalXp: Flow<Int> = dataStore.data.map { it[Keys.TOTAL_XP] ?: 0 }
    val currentTier: Flow<Int> = dataStore.data.map { it[Keys.CURRENT_TIER] ?: 1 }
    
    val dailyXpEarned: Flow<Int> = dataStore.data.map { prefs ->
        val today = getTodayStart()
        val lastDate = prefs[Keys.DAILY_XP_DATE] ?: 0
        if (lastDate < today) 0 else prefs[Keys.DAILY_XP_EARNED] ?: 0
    }
    
    val weeklyXpEarned: Flow<Int> = dataStore.data.map { prefs ->
        val currentWeek = getCurrentWeek()
        val lastWeek = prefs[Keys.WEEKLY_XP_WEEK] ?: 0
        if (lastWeek < currentWeek) 0 else prefs[Keys.WEEKLY_XP_EARNED] ?: 0
    }
    
    val loginStreak: Flow<Int> = dataStore.data.map { it[Keys.LOGIN_STREAK] ?: 0 }
    
    /** Sugar Crystals balance (earn by trying indie apps, spend in pass/shop). */
    val sugarCrystals: Flow<Int> = dataStore.data.map { it[Keys.SUGAR_CRYSTALS] ?: 0 }
    
    val xpBreakdown: Flow<XpBreakdown> = dataStore.data.map { prefs ->
        XpBreakdown(
            fromLogin = prefs[Keys.XP_FROM_LOGIN] ?: 0,
            fromInstalls = prefs[Keys.XP_FROM_INSTALLS] ?: 0,
            fromEffects = prefs[Keys.XP_FROM_EFFECTS] ?: 0,
            fromThemes = prefs[Keys.XP_FROM_THEMES] ?: 0,
            fromAchievements = prefs[Keys.XP_FROM_ACHIEVEMENTS] ?: 0,
            fromShop = prefs[Keys.XP_FROM_SHOP] ?: 0,
            fromOther = prefs[Keys.XP_FROM_OTHER] ?: 0
        )
    }

    // ═════════════════════════════════════════════════════════════
    // XP CALCULATION
    // ═════════════════════════════════════════════════════════════
    
    /**
     * Get current XP multiplier from active boosts
     */
    suspend fun getXpMultiplier(): Float {
        var multiplier = 1.0f
        
        // Weekend bonus
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            multiplier *= XP_WEEKEND_MULTIPLIER
        }
        
        // Check for active XP boosts from inventory/shop
        // This would integrate with ShopManager's active boosts
        // For now, we'll add a base premium pass bonus if they have premium
        // This would be checked against SugarPassManager
        
        return multiplier
    }
    
    /**
     * Calculate bonus XP based on various factors
     */
    suspend fun calculateBonusXp(baseXp: Int): Int {
        val multiplier = getXpMultiplier()
        return (baseXp * multiplier).toInt()
    }
    
    /**
     * Get remaining daily XP cap
     */
    suspend fun getRemainingDailyCap(): Int {
        val earned = dailyXpEarned.first()
        return (DAILY_XP_CAP - earned).coerceAtLeast(0)
    }
    
    /**
     * Get remaining weekly XP cap
     */
    suspend fun getRemainingWeeklyCap(): Int {
        val earned = weeklyXpEarned.first()
        return (WEEKLY_XP_CAP - earned).coerceAtLeast(0)
    }

    // ═════════════════════════════════════════════════════════════
    // ADD XP METHODS
    // ═════════════════════════════════════════════════════════════
    
    /**
     * Main method to add XP - handles caps and multipliers
     */
    suspend fun addXp(amount: Int, source: XpSource, description: String = ""): XpResult {
        val multiplier = getXpMultiplier()
        val baseAmount = amount
        val multipliedAmount = (amount * multiplier).toInt()
        
        // Check caps
        val remainingDaily = getRemainingDailyCap()
        val remainingWeekly = getRemainingWeeklyCap()
        val finalAmount = minOf(multipliedAmount, remainingDaily, remainingWeekly)
        
        if (finalAmount <= 0) {
            return XpResult.CAPPED
        }
        
        // Update XP
        dataStore.edit { prefs ->
            val currentTotal = prefs[Keys.TOTAL_XP] ?: 0
            prefs[Keys.TOTAL_XP] = currentTotal + finalAmount
            
            // Update tier
            val newTier = calculateTierFromXp(currentTotal + finalAmount)
            prefs[Keys.CURRENT_TIER] = newTier
            
            // Update daily tracking
            val today = getTodayStart()
            val lastDailyDate = prefs[Keys.DAILY_XP_DATE] ?: 0
            if (lastDailyDate < today) {
                prefs[Keys.DAILY_XP_EARNED] = finalAmount
                prefs[Keys.DAILY_XP_DATE] = today
            } else {
                prefs[Keys.DAILY_XP_EARNED] = (prefs[Keys.DAILY_XP_EARNED] ?: 0) + finalAmount
            }
            
            // Update weekly tracking
            val currentWeek = getCurrentWeek()
            val lastWeek = prefs[Keys.WEEKLY_XP_WEEK] ?: 0
            if (lastWeek < currentWeek) {
                prefs[Keys.WEEKLY_XP_EARNED] = finalAmount
                prefs[Keys.WEEKLY_XP_WEEK] = currentWeek
            } else {
                prefs[Keys.WEEKLY_XP_EARNED] = (prefs[Keys.WEEKLY_XP_EARNED] ?: 0) + finalAmount
            }
            
            // Update source tracking
            when (source) {
                XpSource.DAILY_LOGIN -> prefs[Keys.XP_FROM_LOGIN] = (prefs[Keys.XP_FROM_LOGIN] ?: 0) + finalAmount
                XpSource.APP_INSTALL -> prefs[Keys.XP_FROM_INSTALLS] = (prefs[Keys.XP_FROM_INSTALLS] ?: 0) + finalAmount
                XpSource.EFFECT_USED -> prefs[Keys.XP_FROM_EFFECTS] = (prefs[Keys.XP_FROM_EFFECTS] ?: 0) + finalAmount
                XpSource.THEME_CHANGED -> prefs[Keys.XP_FROM_THEMES] = (prefs[Keys.XP_FROM_THEMES] ?: 0) + finalAmount
                XpSource.ACHIEVEMENT -> prefs[Keys.XP_FROM_ACHIEVEMENTS] = (prefs[Keys.XP_FROM_ACHIEVEMENTS] ?: 0) + finalAmount
                XpSource.SHOP_PURCHASE -> prefs[Keys.XP_FROM_SHOP] = (prefs[Keys.XP_FROM_SHOP] ?: 0) + finalAmount
                else -> prefs[Keys.XP_FROM_OTHER] = (prefs[Keys.XP_FROM_OTHER] ?: 0) + finalAmount
            }
            
            // Add to history
            addToHistory(prefs, XpHistoryEntry(System.currentTimeMillis(), finalAmount, source, description))
        }
        
        // Check for tier up
        val oldTier = calculateTierFromXp(totalXp.first() - finalAmount)
        val newTier = calculateTierFromXp(totalXp.first())
        val tierUp = if (newTier > oldTier) newTier else null
        
        return XpResult.SUCCESS(
            amount = finalAmount,
            baseAmount = baseAmount,
            multiplier = multiplier,
            newTotal = totalXp.first(),
            tierUp = tierUp,
            capped = finalAmount < multipliedAmount
        )
    }
    
    /**
     * Award XP for daily login
     */
    suspend fun addDailyLoginXp(): XpResult {
        val streak = updateLoginStreak()
        val baseXp = (XP_DAILY_LOGIN_BASE + (streak * XP_DAILY_LOGIN_STREAK_BONUS))
            .coerceAtMost(XP_DAILY_LOGIN_MAX)
        return addXp(baseXp, XpSource.DAILY_LOGIN, "Day $streak streak")
    }
    
    /**
     * Award XP for app install
     */
    suspend fun addAppInstallXp(isPremium: Boolean = false): XpResult {
        val baseXp = if (isPremium) XP_APP_INSTALL + XP_APP_INSTALL_BONUS else XP_APP_INSTALL
        return addXp(baseXp, XpSource.APP_INSTALL)
    }
    
    /**
     * Award XP for using an effect (with cooldown)
     */
    suspend fun addEffectUsedXp(): XpResult {
        val now = System.currentTimeMillis()
        val lastTime = dataStore.data.first()[Keys.LAST_EFFECT_XP_TIME] ?: 0
        
        if (now - lastTime < TimeUnit.MINUTES.toMillis(XP_EFFECT_USED_COOLDOWN_MINUTES.toLong())) {
            return XpResult.ON_COOLDOWN
        }
        
        dataStore.edit { it[Keys.LAST_EFFECT_XP_TIME] = now }
        return addXp(XP_EFFECT_USED, XpSource.EFFECT_USED)
    }
    
    /**
     * Award XP for changing theme (with cooldown)
     */
    suspend fun addThemeChangedXp(): XpResult {
        val now = System.currentTimeMillis()
        val lastTime = dataStore.data.first()[Keys.LAST_THEME_XP_TIME] ?: 0
        
        if (now - lastTime < TimeUnit.MINUTES.toMillis(XP_THEME_CHANGED_COOLDOWN_MINUTES.toLong())) {
            return XpResult.ON_COOLDOWN
        }
        
        dataStore.edit { it[Keys.LAST_THEME_XP_TIME] = now }
        return addXp(XP_THEME_CHANGED, XpSource.THEME_CHANGED)
    }
    
    /**
     * Award XP for achievement unlock
     */
    suspend fun addAchievementXp(rarity: com.sugarmunch.app.features.model.AchievementRarity): XpResult {
        val baseXp = when (rarity) {
            com.sugarmunch.app.features.model.AchievementRarity.COMMON -> XP_ACHIEVEMENT_COMMON
            com.sugarmunch.app.features.model.AchievementRarity.UNCOMMON -> XP_ACHIEVEMENT_UNCOMMON
            com.sugarmunch.app.features.model.AchievementRarity.RARE -> XP_ACHIEVEMENT_RARE
            com.sugarmunch.app.features.model.AchievementRarity.EPIC -> XP_ACHIEVEMENT_EPIC
            com.sugarmunch.app.features.model.AchievementRarity.LEGENDARY -> XP_ACHIEVEMENT_LEGENDARY
        }
        return addXp(baseXp, XpSource.ACHIEVEMENT, rarity.name)
    }
    
    /**
     * Award XP for shop purchase
     */
    suspend fun addShopPurchaseXp(): XpResult {
        return addXp(XP_SHOP_PURCHASE, XpSource.SHOP_PURCHASE)
    }
    
    /**
     * Award XP for sharing app
     */
    suspend fun addShareXp(): XpResult {
        return addXp(XP_SHARE_APP, XpSource.SHARE)
    }
    
    /**
     * Award XP for reviewing app
     */
    suspend fun addReviewXp(): XpResult {
        return addXp(XP_REVIEW_APP, XpSource.REVIEW)
    }
    
    /**
     * Award XP for completing onboarding
     */
    suspend fun addOnboardingCompleteXp(): XpResult {
        return addXp(XP_COMPLETE_ONBOARDING, XpSource.ONBOARDING)
    }
    
    /**
     * Award XP for referral
     */
    suspend fun addReferralXp(): XpResult {
        return addXp(XP_REFERRAL, XpSource.REFERRAL)
    }

    /**
     * Award Sugar Crystals for trying an indie app (install or first open).
     * Call when user installs/opens an app marked as indie (e.g. from a trail or manifest flag).
     */
    suspend fun addSugarCrystalsForIndieAppTry(appId: String): Int {
        dataStore.edit { prefs ->
            val current = prefs[Keys.SUGAR_CRYSTALS] ?: 0
            prefs[Keys.SUGAR_CRYSTALS] = current + CRYSTALS_PER_INDIE_APP_TRY
        }
        return CRYSTALS_PER_INDIE_APP_TRY
    }

    /**
     * Add Sugar Crystals (e.g. from missions or rewards).
     */
    suspend fun addSugarCrystals(amount: Int) {
        if (amount <= 0) return
        dataStore.edit { prefs ->
            val current = prefs[Keys.SUGAR_CRYSTALS] ?: 0
            prefs[Keys.SUGAR_CRYSTALS] = current + amount
        }
    }

    /**
     * Spend Sugar Crystals. Returns true if balance was sufficient.
     */
    suspend fun spendSugarCrystals(amount: Int): Boolean {
        if (amount <= 0) return true
        val current = sugarCrystals.first()
        if (current < amount) return false
        dataStore.edit { prefs ->
            prefs[Keys.SUGAR_CRYSTALS] = current - amount
        }
        return true
    }

    // ═════════════════════════════════════════════════════════════
    // PROGRESS & STATS
    // ═════════════════════════════════════════════════════════════
    
    /**
     * Get detailed progress info
     */
    fun getProgressFlow(): Flow<TierProgress> = combine(totalXp, currentTier) { xp, tier ->
        val (xpInTier, xpNeeded) = getTierProgress(xp)
        val progress = if (xpNeeded > 0) xpInTier.toFloat() / xpNeeded else 1f
        
        TierProgress(
            currentTier = tier,
            totalXp = xp,
            xpInCurrentTier = xpInTier,
            xpNeededForNext = xpNeeded,
            progressPercent = progress,
            isMaxTier = tier >= 100
        )
    }
    
    /**
     * Get estimated time to reach a tier
     */
    suspend fun getEstimatedDaysToTier(targetTier: Int): Int? {
        val currentXp = totalXp.first()
        val targetXp = getCumulativeXpForTier(targetTier)
        val xpNeeded = targetXp - currentXp
        
        if (xpNeeded <= 0) return 0
        
        // Estimate based on average daily XP (assumes 500-1000 per day)
        val avgDailyXp = 750
        return (xpNeeded / avgDailyXp) + 1
    }
    
    /**
     * Get XP sources breakdown
     */
    suspend fun getXpSources(): Map<XpSource, Int> {
        val prefs = dataStore.data.first()
        return mapOf(
            XpSource.DAILY_LOGIN to (prefs[Keys.XP_FROM_LOGIN] ?: 0),
            XpSource.APP_INSTALL to (prefs[Keys.XP_FROM_INSTALLS] ?: 0),
            XpSource.EFFECT_USED to (prefs[Keys.XP_FROM_EFFECTS] ?: 0),
            XpSource.THEME_CHANGED to (prefs[Keys.XP_FROM_THEMES] ?: 0),
            XpSource.ACHIEVEMENT to (prefs[Keys.XP_FROM_ACHIEVEMENTS] ?: 0),
            XpSource.SHOP_PURCHASE to (prefs[Keys.XP_FROM_SHOP] ?: 0),
            XpSource.OTHER to (prefs[Keys.XP_FROM_OTHER] ?: 0)
        )
    }

    // ═════════════════════════════════════════════════════════════
    // HISTORY
    // ═════════════════════════════════════════════════════════════
    
    fun getXpHistory(): Flow<List<XpHistoryEntry>> = dataStore.data.map { prefs ->
        parseHistory(prefs[Keys.XP_HISTORY] ?: "")
    }
    
    fun getDailyProgress(): Flow<DailyProgress> = combine(dailyXpEarned, totalXp) { earned, _ ->
        DailyProgress(
            earnedToday = earned,
            cap = DAILY_XP_CAP,
            remaining = (DAILY_XP_CAP - earned).coerceAtLeast(0),
            percentComplete = earned.toFloat() / DAILY_XP_CAP
        )
    }

    // ═════════════════════════════════════════════════════════════
    // HELPERS
    // ═════════════════════════════════════════════════════════════
    
    private suspend fun updateLoginStreak(): Int {
        val today = getTodayStart()
        val yesterday = today - TimeUnit.DAYS.toMillis(1)
        
        return dataStore.edit { prefs ->
            val lastLogin = prefs[Keys.LAST_LOGIN_DATE] ?: 0
            val currentStreak = prefs[Keys.LOGIN_STREAK] ?: 0
            
            val newStreak = when {
                lastLogin >= today -> currentStreak // Already logged in today
                lastLogin >= yesterday -> currentStreak + 1 // Continue streak
                else -> 1 // Reset streak
            }
            
            prefs[Keys.LOGIN_STREAK] = newStreak
            prefs[Keys.LAST_LOGIN_DATE] = today
            newStreak
        }
    }
    
    private fun getTodayStart(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    
    private fun getCurrentWeek(): Int {
        return Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
    }
    
    private fun addToHistory(prefs: MutablePreferences, entry: XpHistoryEntry) {
        val current = parseHistory(prefs[Keys.XP_HISTORY] ?: "")
        val updated = (current + entry).takeLast(100) // Keep last 100
        prefs[Keys.XP_HISTORY] = serializeHistory(updated)
    }
    
    private fun parseHistory(data: String): List<XpHistoryEntry> {
        if (data.isEmpty()) return emptyList()
        return data.split(";").mapNotNull { entry ->
            val parts = entry.split(",")
            if (parts.size >= 3) {
                XpHistoryEntry(
                    timestamp = parts[0].toLongOrNull() ?: 0,
                    amount = parts[1].toIntOrNull() ?: 0,
                    source = XpSource.valueOf(parts[2]),
                    description = parts.getOrElse(3) { "" }
                )
            } else null
        }
    }
    
    private fun serializeHistory(history: List<XpHistoryEntry>): String {
        return history.joinToString(";") { 
            "${it.timestamp},${it.amount},${it.source},${it.description}" 
        }
    }

    // ═════════════════════════════════════════════════════════════
    // SINGLETON
    // ═════════════════════════════════════════════════════════════
    
    companion object {
        @Volatile
        private var instance: XpManager? = null

        fun getInstance(context: Context): XpManager {
            return instance ?: synchronized(this) {
                instance ?: XpManager(context.applicationContext).also { 
                    instance = it 
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// DATA CLASSES
// ═════════════════════════════════════════════════════════════

enum class XpSource {
    DAILY_LOGIN,
    APP_INSTALL,
    EFFECT_USED,
    THEME_CHANGED,
    ACHIEVEMENT,
    SHOP_PURCHASE,
    SHARE,
    REVIEW,
    ONBOARDING,
    REFERRAL,
    EVENT,
    OTHER
}

sealed class XpResult {
    data class SUCCESS(
        val amount: Int,
        val baseAmount: Int,
        val multiplier: Float,
        val newTotal: Int,
        val tierUp: Int? = null,
        val capped: Boolean = false
    ) : XpResult()
    
    object CAPPED : XpResult()
    object ON_COOLDOWN : XpResult()
}

data class XpBreakdown(
    val fromLogin: Int,
    val fromInstalls: Int,
    val fromEffects: Int,
    val fromThemes: Int,
    val fromAchievements: Int,
    val fromShop: Int,
    val fromOther: Int
) {
    val total: Int get() = fromLogin + fromInstalls + fromEffects + fromThemes + 
                          fromAchievements + fromShop + fromOther
}

data class XpHistoryEntry(
    val timestamp: Long,
    val amount: Int,
    val source: XpSource,
    val description: String = ""
)

data class TierProgress(
    val currentTier: Int,
    val totalXp: Int,
    val xpInCurrentTier: Int,
    val xpNeededForNext: Int,
    val progressPercent: Float,
    val isMaxTier: Boolean
) {
    val xpToNextTier: Int get() = (xpNeededForNext - xpInCurrentTier).coerceAtLeast(0)
}

data class DailyProgress(
    val earnedToday: Int,
    val cap: Int,
    val remaining: Int,
    val percentComplete: Float
)
