package com.sugarmunch.app.pass

import androidx.compose.ui.graphics.Color
import com.sugarmunch.app.features.model.AchievementRarity

/**
 * Sugar Pass Rewards - 100 Tiers of Epic Loot
 * 
 * Reward Structure:
 * - Every tier has FREE rewards
 * - Premium track unlocks 2x+ value
 * - Legendary tiers every 10 levels with exclusive items
 * - Ultimate rewards at tier 50 and 100
 */

enum class RewardTrack {
    FREE,       // Available to all users
    PREMIUM,    // Sugar Pass Premium holders
    LEGENDARY   // Special milestone rewards
}

enum class PassRewardType {
    SUGAR_POINTS,       // Currency
    XP_BOOST,           // XP multiplier
    THEME,              // Exclusive themes
    EFFECT,             // Exclusive effects
    BADGE,              // Profile badges
    ICON,               // App icons
    PARTICLE_STYLE,     // Particle effects
    TITLE,              // Custom titles
    EMOJI_PACK,         // Exclusive emojis
    FEATURE_UNLOCK,     // Unlock app features
    CURRENCY_BOOST      // Sugar points multiplier
}

data class PassReward(
    val tier: Int,
    val track: RewardTrack,
    val type: PassRewardType,
    val name: String,
    val description: String,
    val icon: String, // Emoji or reference
    val value: Int = 0, // Amount for currency/XP
    val itemId: String? = null, // For unlockable items
    val rarity: AchievementRarity = AchievementRarity.COMMON,
    val isExclusive: Boolean = false, // Never returns after season ends
    val previewColor: Color = Color(0xFFFFB6C1)
) {
    val isFree: Boolean get() = track == RewardTrack.FREE
    val isPremium: Boolean get() = track == RewardTrack.PREMIUM || track == RewardTrack.LEGENDARY
}

/**
 * Tier categories for visual organization
 */
enum class TierCategory(val title: String, val color: Color, val description: String) {
    STARTER("Sweet Beginnings", Color(0xFFFFB6C1), "Start your Sugar Pass journey!"),
    APPRENTICE("Candy Apprentice", Color(0xFF98FF98), "Learning the sweet ways"),
    ENTHUSIAST("Sugar Enthusiast", Color(0xFFFFFACD), "Getting serious about sweets"),
    COLLECTOR("Candy Collector", Color(0xFFDEB887), "Building your collection"),
    CONNOISSEUR("Sugar Connoisseur", Color(0xFFB5DEFF), "Refined taste in treats"),
    MASTER("Candy Master", Color(0xFFE6B3FF), "Mastery of all things sweet"),
    LEGEND("Sweet Legend", Color(0xFFFFD700), "Legendary status achieved"),
    ULTIMATE("Sugar Deity", Color(0xFFFF1493), "Transcendence through sugar")
}

/**
 * Get category for any tier
 */
fun getTierCategory(tier: Int): TierCategory = when (tier) {
    in 1..12 -> TierCategory.STARTER
    in 13..24 -> TierCategory.APPRENTICE
    in 25..36 -> TierCategory.ENTHUSIAST
    in 37..48 -> TierCategory.COLLECTOR
    in 49..60 -> TierCategory.CONNOISSEUR
    in 61..75 -> TierCategory.MASTER
    in 76..99 -> TierCategory.LEGEND
    100 -> TierCategory.ULTIMATE
    else -> TierCategory.STARTER
}

/**
 * XP required for each tier
 * Formula: Base + (tier * multiplier)^exponent
 */
fun getXpForTier(tier: Int): Int {
    if (tier <= 1) return 0
    val base = 100
    val multiplier = 1.08
    val tierProgress = tier - 1
    return (base * Math.pow(multiplier, tierProgress.toDouble())).toInt() + (tierProgress * 50)
}

/**
 * Get cumulative XP needed to reach a tier
 */
fun getCumulativeXpForTier(tier: Int): Int {
    return (2..tier).sumOf { getXpForTier(it) }
}

/**
 * Calculate tier from total XP
 */
fun calculateTierFromXp(totalXp: Int): Int {
    var cumulative = 0
    for (tier in 2..100) {
        cumulative += getXpForTier(tier)
        if (cumulative > totalXp) return tier - 1
    }
    return 100
}

/**
 * Get XP progress within current tier
 */
fun getTierProgress(currentXp: Int): Pair<Int, Int> {
    val currentTier = calculateTierFromXp(currentXp)
    val cumulativeBefore = getCumulativeXpForTier(currentTier)
    val xpInTier = currentXp - cumulativeBefore
    val xpNeeded = getXpForTier(currentTier + 1)
    return Pair(xpInTier, xpNeeded)
}

/**
 * All 100 tiers of rewards
 */
object SugarPassRewards {
    
    private val allRewards = mutableListOf<PassReward>().apply {
        // ═════════════════════════════════════════════════════════════
        // TIERS 1-12: SWEET BEGINNINGS
        // ═════════════════════════════════════════════════════════════
        addAll(listOf(
            PassReward(1, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Welcome Gift", "Starting Sugar Points", "🍬", 100, rarity = AchievementRarity.COMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(1, RewardTrack.PREMIUM, PassRewardType.XP_BOOST, "XP Boost", "+10% XP for 24 hours", "⚡", 10, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFD700)),
            
            PassReward(2, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Daily Bonus", "Extra Sugar Points", "🍬", 150, rarity = AchievementRarity.COMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(2, RewardTrack.PREMIUM, PassRewardType.BADGE, "Rookie Badge", "Show you're new but eager", "🌟", itemId = "badge_rookie", rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFF98FF98)),
            
            PassReward(3, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Sugar Rush", "Sweet reward", "🍭", 200, rarity = AchievementRarity.COMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(3, RewardTrack.PREMIUM, PassRewardType.THEME, "Pastel Dreams", "Soft pastel color theme", "🎨", itemId = "theme_pastel_dreams", rarity = AchievementRarity.RARE, previewColor = Color(0xFFE6B3FF)),
            
            PassReward(4, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Candy Stash", "Building your collection", "🍫", 250, rarity = AchievementRarity.COMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(4, RewardTrack.PREMIUM, PassRewardType.EMOJI_PACK, "Sweet Emojis", "Exclusive emoji pack", "😋", itemId = "emojis_sweet", rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFFACD)),
            
            PassReward(5, RewardTrack.FREE, PassRewardType.XP_BOOST, "Learning Boost", "+5% XP permanently", "📚", 5, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFF98FF98)),
            PassReward(5, RewardTrack.PREMIUM, PassRewardType.EFFECT, "Sparkle Trail", "Sparkly particle effect", "✨", itemId = "effect_sparkle_trail", rarity = AchievementRarity.RARE, previewColor = Color(0xFFFFD700)),
            
            PassReward(6, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Weekend Treat", "Weekend bonus", "🧁", 300, rarity = AchievementRarity.COMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(6, RewardTrack.PREMIUM, PassRewardType.SUGAR_POINTS, "Premium Sugar", "Bonus points", "🍬", 500, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFD700)),
            
            PassReward(7, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Weekly Bonus", "Week milestone reward", "🎁", 350, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFF98FF98)),
            PassReward(7, RewardTrack.PREMIUM, PassRewardType.ICON, "Candy Icon", "Sweet app icon", "🍬", itemId = "icon_candy", rarity = AchievementRarity.RARE, previewColor = Color(0xFFFFB6C1)),
            
            PassReward(8, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Steady Progress", "Keep going!", "🍭", 200, rarity = AchievementRarity.COMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(8, RewardTrack.PREMIUM, PassRewardType.TITLE, "Sweet Tooth", "Custom profile title", "🦷", itemId = "title_sweet_tooth", rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFDEB887)),
            
            PassReward(9, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Sugar Cube", "Little but sweet", "🧊", 250, rarity = AchievementRarity.COMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(9, RewardTrack.PREMIUM, PassRewardType.CURRENCY_BOOST, "Coin Multiplier", "+15% Sugar Points for 3 days", "💰", 15, rarity = AchievementRarity.RARE, previewColor = Color(0xFFFFD700)),
            
            PassReward(10, RewardTrack.FREE, PassRewardType.THEME, "Candy Stripe", "Classic candy theme", "🍭", itemId = "theme_candy_stripe", rarity = AchievementRarity.RARE, previewColor = Color(0xFFFF1493)),
            PassReward(10, RewardTrack.LEGENDARY, PassRewardType.EFFECT, "Legendary Glow", "Legendary aura effect", "👑", itemId = "effect_legendary_glow", isExclusive = true, rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700)),
            
            PassReward(11, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Bonus Round", "Extra points", "🎯", 300, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(11, RewardTrack.PREMIUM, PassRewardType.BADGE, "Dedicated", "You're committed", "🔥", itemId = "badge_dedicated", rarity = AchievementRarity.RARE, previewColor = Color(0xFFFF4500)),
            
            PassReward(12, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Dozen Treats", "A dozen worth of sugar", "🍩", 400, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFDEB887)),
            PassReward(12, RewardTrack.PREMIUM, PassRewardType.FEATURE_UNLOCK, "Custom Themes", "Unlock custom theme creation", "🎨", itemId = "feature_custom_themes", rarity = AchievementRarity.EPIC, previewColor = Color(0xFF9370DB))
        ))
        
        // ═════════════════════════════════════════════════════════════
        // TIERS 13-24: CANDY APPRENTICE
        // ═════════════════════════════════════════════════════════════
        addAll(listOf(
            PassReward(13, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Baker's Dozen", "Lucky 13", "🥐", 350, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFDEB887)),
            PassReward(13, RewardTrack.PREMIUM, PassRewardType.PARTICLE_STYLE, "Falling Sprinkles", "Sprinkle particle effect", "🌈", itemId = "particles_sprinkles", rarity = AchievementRarity.RARE, previewColor = Color(0xFFFF1493)),
            
            PassReward(14, RewardTrack.FREE, PassRewardType.XP_BOOST, "Level Up", "+10% XP permanently", "⬆️", 10, rarity = AchievementRarity.RARE, previewColor = Color(0xFF98FF98)),
            PassReward(14, RewardTrack.PREMIUM, PassRewardType.THEME, "Mint Fresh", "Cool mint color scheme", "🌿", itemId = "theme_mint_fresh", rarity = AchievementRarity.RARE, previewColor = Color(0xFF98FF98)),
            
            PassReward(15, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Halfway There", "Mid-tier milestone", "🎯", 450, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(15, RewardTrack.PREMIUM, PassRewardType.BADGE, "Apprentice", "Learning the craft", "📜", itemId = "badge_apprentice", rarity = AchievementRarity.RARE, previewColor = Color(0xFFDEB887)),
            
            PassReward(16, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Sweet Sixteen", "Special bonus", "🎂", 500, rarity = AchievementRarity.RARE, previewColor = Color(0xFFFFB6C1)),
            PassReward(16, RewardTrack.PREMIUM, PassRewardType.EFFECT, "Candy Rain", "Make it rain candy", "🍬", itemId = "effect_candy_rain", rarity = AchievementRarity.EPIC, previewColor = Color(0xFFFF1493)),
            
            PassReward(17, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Seventeen Treats", "Lucky number", "🍀", 400, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFF98FF98)),
            PassReward(17, RewardTrack.PREMIUM, PassRewardType.SUGAR_POINTS, "Premium Haul", "Big point boost", "💎", 800, rarity = AchievementRarity.RARE, previewColor = Color(0xFFFFD700)),
            
            PassReward(18, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Adulting", "All grown up", "🎈", 450, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(18, RewardTrack.PREMIUM, PassRewardType.ICON, "Royal Icon", "Crown your apps", "👑", itemId = "icon_royal", rarity = AchievementRarity.EPIC, previewColor = Color(0xFFFFD700)),
            
            PassReward(19, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Almost 20", "Getting close", "🔜", 500, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(19, RewardTrack.PREMIUM, PassRewardType.TITLE, "Candy Maker", "Show your skills", "👨‍🍳", itemId = "title_candy_maker", rarity = AchievementRarity.RARE, previewColor = Color(0xFFDEB887)),
            
            PassReward(20, RewardTrack.FREE, PassRewardType.THEME, "Chocolate Dream", "Rich chocolate theme", "🍫", itemId = "theme_chocolate", rarity = AchievementRarity.RARE, previewColor = Color(0xFF8B4513)),
            PassReward(20, RewardTrack.LEGENDARY, PassRewardType.EFFECT, "Golden Shower", "Legendary gold effect", "🌟", itemId = "effect_golden_shower", isExclusive = true, rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700)),
            
            PassReward(21, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "21 Jump Street", "Legal sweetness", "🎰", 550, rarity = AchievementRarity.RARE, previewColor = Color(0xFFFFD700)),
            PassReward(21, RewardTrack.PREMIUM, PassRewardType.EMOJI_PACK, "Foodie Pack", "Delicious emojis", "🍕", itemId = "emojis_foodie", rarity = AchievementRarity.RARE, previewColor = Color(0xFFDEB887)),
            
            PassReward(22, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Double Deuce", "Twice the fun", "🎲", 500, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(22, RewardTrack.PREMIUM, PassRewardType.FEATURE_UNLOCK, "Priority Queue", "Skip download queues", "🏃", itemId = "feature_priority", rarity = AchievementRarity.EPIC, previewColor = Color(0xFF9370DB)),
            
            PassReward(23, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Jordan's Number", "MJ would be proud", "🏀", 550, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(23, RewardTrack.PREMIUM, PassRewardType.BADGE, "Consistent", "Showing up daily", "📅", itemId = "badge_consistent", rarity = AchievementRarity.RARE, previewColor = Color(0xFF98FF98)),
            
            PassReward(24, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Two Dozen", "24 treats", "🎁", 600, rarity = AchievementRarity.RARE, previewColor = Color(0xFFDEB887)),
            PassReward(24, RewardTrack.PREMIUM, PassRewardType.THEME, "Neon Nights", "Cyberpunk candy", "🌃", itemId = "theme_neon_nights", rarity = AchievementRarity.EPIC, previewColor = Color(0xFF00CED1))
        ))
        
        // ═════════════════════════════════════════════════════════════
        // TIERS 25-36: SUGAR ENTHUSIAST
        // ═════════════════════════════════════════════════════════════
        addAll(listOf(
            PassReward(25, RewardTrack.FREE, PassRewardType.XP_BOOST, "Quarter Century", "+15% XP permanently", "🎯", 15, rarity = AchievementRarity.EPIC, previewColor = Color(0xFFFFD700)),
            PassReward(25, RewardTrack.PREMIUM, PassRewardType.SUGAR_POINTS, "Silver Celebration", "Big milestone reward", "🥈", 1000, rarity = AchievementRarity.EPIC, previewColor = Color(0xFFC0C0C0)),
            
            PassReward(26, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Sweet Progress", "Keep climbing", "🧗", 600, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(26, RewardTrack.PREMIUM, PassRewardType.EFFECT, "Heart Burst", "Love explosion effect", "💕", itemId = "effect_heart_burst", rarity = AchievementRarity.RARE, previewColor = Color(0xFFFF1493)),
            
            PassReward(27, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "27 Club", "Elite status", "🎸", 650, rarity = AchievementRarity.RARE, previewColor = Color(0xFF9370DB)),
            PassReward(27, RewardTrack.PREMIUM, PassRewardType.BADGE, "Enthusiast", "True sugar lover", "❤️", itemId = "badge_enthusiast", rarity = AchievementRarity.RARE, previewColor = Color(0xFFFF1493)),
            
            PassReward(28, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Lucky 28", "Good fortune", "🎋", 700, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFF98FF98)),
            PassReward(28, RewardTrack.PREMIUM, PassRewardType.TITLE, "Sugar Fiend", "Can't get enough", "😈", itemId = "title_sugar_fiend", rarity = AchievementRarity.RARE, previewColor = Color(0xFF9370DB)),
            
            PassReward(29, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Almost 30", "The big three-oh approaches", "🚀", 750, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(29, RewardTrack.PREMIUM, PassRewardType.ICON, "Diamond Icon", "Shine bright", "💎", itemId = "icon_diamond", rarity = AchievementRarity.EPIC, previewColor = Color(0xFF00CED1)),
            
            PassReward(30, RewardTrack.FREE, PassRewardType.THEME, "Sunset Glow", "Warm sunset colors", "🌅", itemId = "theme_sunset", rarity = AchievementRarity.RARE, previewColor = Color(0xFFFFA500)),
            PassReward(30, RewardTrack.LEGENDARY, PassRewardType.EFFECT, "Dragon's Breath", "Epic fire effect", "🐉", itemId = "effect_dragon_breath", isExclusive = true, rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFF4500)),
            
            PassReward(31, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Thirty One", "Prime position", "🔢", 800, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(31, RewardTrack.PREMIUM, PassRewardType.PARTICLE_STYLE, "Star Field", "Space particles", "🌌", itemId = "particles_stars", rarity = AchievementRarity.EPIC, previewColor = Color(0xFF4B0082)),
            
            PassReward(32, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "32 Flavors", "Variety pack", "🍦", 850, rarity = AchievementRarity.RARE, previewColor = Color(0xFFFFF0F5)),
            PassReward(32, RewardTrack.PREMIUM, PassRewardType.CURRENCY_BOOST, "Sugar Rush", "+25% points for a week", "🚀", 25, rarity = AchievementRarity.EPIC, previewColor = Color(0xFFFF1493)),
            
            PassReward(33, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Triple Three", "Magic number", "🎱", 900, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFF9370DB)),
            PassReward(33, RewardTrack.PREMIUM, PassRewardType.BADGE, "Veteran", "Seasoned veteran", "🎖️", itemId = "badge_veteran", rarity = AchievementRarity.EPIC, previewColor = Color(0xFFDAA520)),
            
            PassReward(34, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Rule 34", "Internet famous", "📱", 950, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFF00CED1)),
            PassReward(34, RewardTrack.PREMIUM, PassRewardType.THEME, "Ocean Deep", "Underwater vibes", "🌊", itemId = "theme_ocean", rarity = AchievementRarity.RARE, previewColor = Color(0xFF4682B4)),
            
            PassReward(35, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Mid-30s", "Still young", "🎂", 1000, rarity = AchievementRarity.RARE, previewColor = Color(0xFFFFB6C1)),
            PassReward(35, RewardTrack.PREMIUM, PassRewardType.EMOJI_PACK, "Space Pack", "Cosmic emojis", "🚀", itemId = "emojis_space", rarity = AchievementRarity.RARE, previewColor = Color(0xFF4B0082)),
            
            PassReward(36, RewardTrack.FREE, PassRewardType.XP_BOOST, "Three Six", "Perfect square", "📐", 20, rarity = AchievementRarity.EPIC, previewColor = Color(0xFF98FF98)),
            PassReward(36, RewardTrack.PREMIUM, PassRewardType.FEATURE_UNLOCK, "Beta Access", "Early feature access", "🔬", itemId = "feature_beta", rarity = AchievementRarity.EPIC, previewColor = Color(0xFF9370DB))
        ))
        
        // ═════════════════════════════════════════════════════════════
        // TIERS 37-48: CANDY COLLECTOR
        // ═════════════════════════════════════════════════════════════
        addAll(listOf(
            PassReward(37, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Lucky 37", "Casino winner", "🎰", 1000, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFD700)),
            PassReward(37, RewardTrack.PREMIUM, PassRewardType.EFFECT, "Lightning Strike", "Electric effect", "⚡", itemId = "effect_lightning", rarity = AchievementRarity.EPIC, previewColor = Color(0xFFFFD700)),
            
            PassReward(38, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Special 38", "Unique tier", "🦄", 1050, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFE6B3FF)),
            PassReward(38, RewardTrack.PREMIUM, PassRewardType.BADGE, "Collector", "Gotta catch 'em all", "📚", itemId = "badge_collector", rarity = AchievementRarity.EPIC, previewColor = Color(0xFF9370DB)),
            
            PassReward(39, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Almost 40", "Round number coming", "🔜", 1100, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(39, RewardTrack.PREMIUM, PassRewardType.TITLE, "Hoarder", "Can't stop collecting", "🐿️", itemId = "title_hoarder", rarity = AchievementRarity.RARE, previewColor = Color(0xFF8B4513)),
            
            PassReward(40, RewardTrack.FREE, PassRewardType.THEME, "Forest Green", "Nature theme", "🌲", itemId = "theme_forest", rarity = AchievementRarity.RARE, previewColor = Color(0xFF228B22)),
            PassReward(40, RewardTrack.LEGENDARY, PassRewardType.EFFECT, "Phoenix Rise", "Rebirth effect", "🔥", itemId = "effect_phoenix", isExclusive = true, rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFF4500)),
            
            PassReward(41, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Forty One", "Prime time", "🎯", 1150, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(41, RewardTrack.PREMIUM, PassRewardType.SUGAR_POINTS, "Premium Stack", "Huge bonus", "🏔️", 1500, rarity = AchievementRarity.EPIC, previewColor = Color(0xFFFFD700)),
            
            PassReward(42, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Life Universe", "Answer to everything", "🌌", 1200, rarity = AchievementRarity.EPIC, previewColor = Color(0xFF4B0082)),
            PassReward(42, RewardTrack.PREMIUM, PassRewardType.ICON, "Galaxy Icon", "Cosmic app icon", "🌠", itemId = "icon_galaxy", rarity = AchievementRarity.EPIC, previewColor = Color(0xFF4B0082)),
            
            PassReward(43, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "43 Steps", "Keep walking", "🚶", 1250, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(43, RewardTrack.PREMIUM, PassRewardType.THEME, "Volcanic", "Hot lava theme", "🌋", itemId = "theme_volcanic", rarity = AchievementRarity.RARE, previewColor = Color(0xFFDC143C)),
            
            PassReward(44, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Double Four", "Syncopated", "🎵", 1300, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFF9370DB)),
            PassReward(44, RewardTrack.PREMIUM, PassRewardType.BADGE, "Elite", "Top tier player", "🏆", itemId = "badge_elite", rarity = AchievementRarity.EPIC, previewColor = Color(0xFFFFD700)),
            
            PassReward(45, RewardTrack.FREE, PassRewardType.XP_BOOST, "Mid-40s", "+20% XP permanently", "📈", 20, rarity = AchievementRarity.EPIC, previewColor = Color(0xFF98FF98)),
            PassReward(45, RewardTrack.PREMIUM, PassRewardType.PARTICLE_STYLE, "Confetti Cannon", "Celebration time", "🎊", itemId = "particles_confetti", rarity = AchievementRarity.RARE, previewColor = Color(0xFFFF1493)),
            
            PassReward(46, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Forty Six", "Steady climb", "🧗", 1350, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFDEB887)),
            PassReward(46, RewardTrack.PREMIUM, PassRewardType.EMOJI_PACK, "Fantasy Pack", "Mythical emojis", "🐉", itemId = "emojis_fantasy", rarity = AchievementRarity.EPIC, previewColor = Color(0xFF9370DB)),
            
            PassReward(47, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "AK-47", "Legendary number", "🔫", 1400, rarity = AchievementRarity.RARE, previewColor = Color(0xFF8B4513)),
            PassReward(47, RewardTrack.PREMIUM, PassRewardType.TITLE, "Completionist", "Almost there", "📊", itemId = "title_completionist", rarity = AchievementRarity.EPIC, previewColor = Color(0xFF9370DB)),
            
            PassReward(48, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Four Dozen", "Baker's special", "🍞", 1450, rarity = AchievementRarity.RARE, previewColor = Color(0xFFDEB887)),
            PassReward(48, RewardTrack.PREMIUM, PassRewardType.FEATURE_UNLOCK, "Custom Effects", "Create your own effects", "⚗️", itemId = "feature_custom_effects", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFF1493))
        ))
        
        // ═════════════════════════════════════════════════════════════
        // TIERS 49-60: SUGAR CONNOISSEUR
        // ═════════════════════════════════════════════════════════════
        addAll(listOf(
            PassReward(49, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Almost 50", "The halfway point", "⏳", 1500, rarity = AchievementRarity.EPIC, previewColor = Color(0xFFFFD700)),
            PassReward(49, RewardTrack.PREMIUM, PassRewardType.EFFECT, "Aurora Borealis", "Northern lights", "🌌", itemId = "effect_aurora", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFF00CED1)),
            
            PassReward(50, RewardTrack.FREE, PassRewardType.THEME, "Golden Age", "Luxury theme", "👑", itemId = "theme_golden", rarity = AchievementRarity.EPIC, previewColor = Color(0xFFFFD700)),
            PassReward(50, RewardTrack.LEGENDARY, PassRewardType.BADGE, "Halfway Hero", "50 tiers complete!", "🎯", itemId = "badge_halfway", isExclusive = true, rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700)),
            
            PassReward(51, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Fifty One", "Past the peak", "🏔️", 1600, rarity = AchievementRarity.EPIC, previewColor = Color(0xFF9370DB)),
            PassReward(51, RewardTrack.PREMIUM, PassRewardType.CURRENCY_BOOST, "Point Tsunami", "+50% points for 2 weeks", "🌊", 50, rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFF00CED1)),
            
            PassReward(52, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Card Deck", "Full house", "🃏", 1650, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFF8B4513)),
            PassReward(52, RewardTrack.PREMIUM, PassRewardType.THEME, "Royal Purple", "Regal elegance", "💜", itemId = "theme_royal", rarity = AchievementRarity.EPIC, previewColor = Color(0xFF800080)),
            
            PassReward(53, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Prime 53", "Indivisible", "🔢", 1700, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFF9370DB)),
            PassReward(53, RewardTrack.PREMIUM, PassRewardType.BADGE, "Connoisseur", "Refined taste", "🍷", itemId = "badge_connoisseur", rarity = AchievementRarity.EPIC, previewColor = Color(0xFF800080)),
            
            PassReward(54, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Factorial", "Math whiz", "🧮", 1750, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFF98FF98)),
            PassReward(54, RewardTrack.PREMIUM, PassRewardType.ICON, "Crown Icon", "King of apps", "👑", itemId = "icon_crown", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700)),
            
            PassReward(55, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Speed Limit", "Going fast", "🏎️", 1800, rarity = AchievementRarity.RARE, previewColor = Color(0xFFFF4500)),
            PassReward(55, RewardTrack.PREMIUM, PassRewardType.EFFECT, "Time Warp", "Slow motion effect", "⏰", itemId = "effect_timewarp", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFF9370DB)),
            
            PassReward(56, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Fifty Six", "Steady growth", "📊", 1850, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFDEB887)),
            PassReward(56, RewardTrack.PREMIUM, PassRewardType.TITLE, "Big Shot", "Important person", "💼", itemId = "title_bigshot", rarity = AchievementRarity.RARE, previewColor = Color(0xFF8B4513)),
            
            PassReward(57, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Heinz Variety", "57 varieties", "🥫", 1900, rarity = AchievementRarity.RARE, previewColor = Color(0xFFDC143C)),
            PassReward(57, RewardTrack.PREMIUM, PassRewardType.EMOJI_PACK, "VIP Pack", "Exclusive emojis", "🎩", itemId = "emojis_vip", rarity = AchievementRarity.EPIC, previewColor = Color(0xFF800080)),
            
            PassReward(58, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Fifty Eight", "Almost there", "🔜", 1950, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(58, RewardTrack.PREMIUM, PassRewardType.FEATURE_UNLOCK, "VIP Lounge", "Exclusive area access", "🚪", itemId = "feature_vip", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700)),
            
            PassReward(59, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Prime 59", "Almost 60", "🔢", 2000, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFF9370DB)),
            PassReward(59, RewardTrack.PREMIUM, PassRewardType.PARTICLE_STYLE, "Nebula Cloud", "Space dust", "🌌", itemId = "particles_nebula", rarity = AchievementRarity.EPIC, previewColor = Color(0xFF4B0082)),
            
            PassReward(60, RewardTrack.FREE, PassRewardType.THEME, "Ruby Red", "Precious gem theme", "💎", itemId = "theme_ruby", rarity = AchievementRarity.EPIC, previewColor = Color(0xFFDC143C)),
            PassReward(60, RewardTrack.LEGENDARY, PassRewardType.EFFECT, "Supernova", "Stellar explosion", "💥", itemId = "effect_supernova", isExclusive = true, rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFF4500))
        ))
        
        // ═════════════════════════════════════════════════════════════
        // TIERS 61-75: CANDY MASTER
        // ═════════════════════════════════════════════════════════════
        addAll(listOf(
            PassReward(61, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Sixty One", "Into the 60s", "🎂", 2100, rarity = AchievementRarity.EPIC, previewColor = Color(0xFFFFB6C1)),
            PassReward(61, RewardTrack.PREMIUM, PassRewardType.BADGE, "Master", "True mastery", "🥋", itemId = "badge_master", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFF000000)),
            
            PassReward(62, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Sixty Two", "Master tier", "🎯", 2150, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(62, RewardTrack.PREMIUM, PassRewardType.XP_BOOST, "Master XP", "+25% XP permanently", "⭐", 25, rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700)),
            
            PassReward(63, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Sixty Three", "Master class", "🎓", 2200, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFDEB887)),
            PassReward(63, RewardTrack.PREMIUM, PassRewardType.THEME, "Masterpiece", "Artistic theme", "🎨", itemId = "theme_masterpiece", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFF9370DB)),
            
            PassReward(64, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Sixty Four", "Nintendo number", "🎮", 2250, rarity = AchievementRarity.RARE, previewColor = Color(0xFFDC143C)),
            PassReward(64, RewardTrack.PREMIUM, PassRewardType.SUGAR_POINTS, "Master Hoard", "Massive bonus", "🏦", 2500, rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700)),
            
            PassReward(65, RewardTrack.FREE, PassRewardType.XP_BOOST, "Retirement Age", "Senior benefits", "👴", 25, rarity = AchievementRarity.EPIC, previewColor = Color(0xFF8B4513)),
            PassReward(65, RewardTrack.PREMIUM, PassRewardType.EFFECT, "Midas Touch", "Everything turns gold", "👆", itemId = "effect_midas", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700)),
            
            PassReward(66, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Route 66", "Historic highway", "🛣️", 2300, rarity = AchievementRarity.RARE, previewColor = Color(0xFF8B4513)),
            PassReward(66, RewardTrack.PREMIUM, PassRewardType.ICON, "Master Icon", "Elite app icon", "🎖️", itemId = "icon_master", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700)),
            
            PassReward(67, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Sixty Seven", "Prime master", "🔢", 2350, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFF9370DB)),
            PassReward(67, RewardTrack.PREMIUM, PassRewardType.TITLE, "Grandmaster", "Above masters", "👑", itemId = "title_grandmaster", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700)),
            
            PassReward(68, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Sixty Eight", "Nearly 70", "🔜", 2400, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(68, RewardTrack.PREMIUM, PassRewardType.EMOJI_PACK, "Master Pack", "Elite emojis", "🦁", itemId = "emojis_master", rarity = AchievementRarity.EPIC, previewColor = Color(0xFFFFD700)),
            
            PassReward(69, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Nice", "Reddit number", "😏", 2450, rarity = AchievementRarity.EPIC, previewColor = Color(0xFFFF1493)),
            PassReward(69, RewardTrack.PREMIUM, PassRewardType.BADGE, "Internet Legend", "Meme master", "🐸", itemId = "badge_internet", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFF00CED1)),
            
            PassReward(70, RewardTrack.FREE, PassRewardType.THEME, "Platinum", "Rare metal theme", "⚪", itemId = "theme_platinum", rarity = AchievementRarity.EPIC, previewColor = Color(0xFFE5E4E2)),
            PassReward(70, RewardTrack.LEGENDARY, PassRewardType.FEATURE_UNLOCK, "Creator Tools", "Create & sell items", "🛠️", itemId = "feature_creator", isExclusive = true, rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFF1493)),
            
            PassReward(71, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Seventy One", "Into the 70s", "🎂", 2500, rarity = AchievementRarity.EPIC, previewColor = Color(0xFFFFB6C1)),
            PassReward(71, RewardTrack.PREMIUM, PassRewardType.EFFECT, "Black Hole", "Gravity well effect", "🕳️", itemId = "effect_blackhole", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFF000000)),
            
            PassReward(72, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Seventy Two", "Tutti frutti", "🍓", 2550, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFF1493)),
            PassReward(72, RewardTrack.PREMIUM, PassRewardType.PARTICLE_STYLE, "Butterflies", "Nature's beauty", "🦋", itemId = "particles_butterflies", rarity = AchievementRarity.EPIC, previewColor = Color(0xFFFF1493)),
            
            PassReward(73, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Seventy Three", "Sheldon number", "🔬", 2600, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFF98FF98)),
            PassReward(73, RewardTrack.PREMIUM, PassRewardType.THEME, "Crystal Cave", "Shimmering crystals", "💎", itemId = "theme_crystal", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFF00CED1)),
            
            PassReward(74, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Seventy Four", "So close", "🔜", 2650, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(74, RewardTrack.PREMIUM, PassRewardType.BADGE, "Unstoppable", "Can't be stopped", "🚂", itemId = "badge_unstoppable", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFDC143C)),
            
            PassReward(75, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Three Quarters", "75% complete!", "📊", 3000, rarity = AchievementRarity.EPIC, previewColor = Color(0xFFFFD700)),
            PassReward(75, RewardTrack.PREMIUM, PassRewardType.CURRENCY_BOOST, "Point Monsoon", "+75% points for a month", "🌧️", 75, rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFF00CED1))
        ))
        
        // ═════════════════════════════════════════════════════════════
        // TIERS 76-99: SWEET LEGEND
        // ═════════════════════════════════════════════════════════════
        addAll(listOf(
            PassReward(76, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Seventy Six", "Legend tier", "🎸", 2700, rarity = AchievementRarity.EPIC, previewColor = Color(0xFFDC143C)),
            PassReward(76, RewardTrack.PREMIUM, PassRewardType.TITLE, "Living Legend", "Famous status", "🌟", itemId = "title_legend", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700)),
            
            PassReward(77, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Seventy Seven", "Lucky sevens", "🎰", 2750, rarity = AchievementRarity.EPIC, previewColor = Color(0xFFFFD700)),
            PassReward(77, RewardTrack.PREMIUM, PassRewardType.ICON, "Legend Icon", "Mythical app icon", "🦄", itemId = "icon_legend", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFE6B3FF)),
            
            PassReward(78, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Seventy Eight", "RPM speed", "🎵", 2800, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFF8B4513)),
            PassReward(78, RewardTrack.PREMIUM, PassRewardType.EFFECT, "Unicorn Magic", "Rainbow sparkles", "🦄", itemId = "effect_unicorn", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFF1493)),
            
            PassReward(79, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Seventy Nine", "Gold number", "🥇", 2850, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFD700)),
            PassReward(79, RewardTrack.PREMIUM, PassRewardType.BADGE, "Immortal", "Forever remembered", "👻", itemId = "badge_immortal", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFF9370DB)),
            
            PassReward(80, RewardTrack.FREE, PassRewardType.THEME, "Diamond", "Hardest theme", "💎", itemId = "theme_diamond", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFB9F2FF)),
            PassReward(80, RewardTrack.LEGENDARY, PassRewardType.FEATURE_UNLOCK, "Hall of Fame", "Permanent recognition", "🏛️", itemId = "feature_halloffame", isExclusive = true, rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700)),
            
            PassReward(81, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Eighty One", "9 squared", "🔢", 2900, rarity = AchievementRarity.EPIC, previewColor = Color(0xFF9370DB)),
            PassReward(81, RewardTrack.PREMIUM, PassRewardType.SUGAR_POINTS, "Legend's Hoard", "Massive reward", "🏴‍☠️", 3500, rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700)),
            
            PassReward(82, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Eighty Two", "Almost done", "🔜", 2950, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(82, RewardTrack.PREMIUM, PassRewardType.EMOJI_PACK, "Legend Pack", "Ultra rare emojis", "🐉", itemId = "emojis_legend", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700)),
            
            PassReward(83, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Eighty Three", "Prime legend", "🔢", 3000, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFF9370DB)),
            PassReward(83, RewardTrack.PREMIUM, PassRewardType.THEME, "Northern Lights", "Aurora theme", "🌌", itemId = "theme_aurora", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFF00CED1)),
            
            PassReward(84, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Eighty Four", "Orwell's number", "📚", 3050, rarity = AchievementRarity.RARE, previewColor = Color(0xFF8B4513)),
            PassReward(84, RewardTrack.PREMIUM, PassRewardType.EFFECT, "Matrix Rain", "Code falling", "💻", itemId = "effect_matrix", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFF00FF00)),
            
            PassReward(85, RewardTrack.FREE, PassRewardType.XP_BOOST, "Eighty Five", "Speed demon", "🏎️", 30, rarity = AchievementRarity.EPIC, previewColor = Color(0xFFFF4500)),
            PassReward(85, RewardTrack.PREMIUM, PassRewardType.PARTICLE_STYLE, "Fireflies", "Summer night", "✨", itemId = "particles_fireflies", rarity = AchievementRarity.EPIC, previewColor = Color(0xFFFFD700)),
            
            PassReward(86, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Eighty Six", "Dismissed", "🚫", 3100, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFDC143C)),
            PassReward(86, RewardTrack.PREMIUM, PassRewardType.BADGE, "Mythical", "Beyond legendary", "🐉", itemId = "badge_mythical", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFF00CED1)),
            
            PassReward(87, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Eighty Seven", "Lucky number", "🍀", 3150, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFF98FF98)),
            PassReward(87, RewardTrack.PREMIUM, PassRewardType.TITLE, "Mythical Beast", "Untouchable", "🐲", itemId = "title_mythical", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFF4500)),
            
            PassReward(88, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Eighty Eight", "Double fortune", "🧧", 3200, rarity = AchievementRarity.EPIC, previewColor = Color(0xFFFFD700)),
            PassReward(88, RewardTrack.PREMIUM, PassRewardType.ICON, "God Tier Icon", "Divine app icon", "☀️", itemId = "icon_god", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700)),
            
            PassReward(89, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Eighty Nine", "Almost 90", "🔜", 3250, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(89, RewardTrack.PREMIUM, PassRewardType.FEATURE_UNLOCK, "Developer Access", "Beta everything", "🔧", itemId = "feature_dev", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFF9370DB)),
            
            PassReward(90, RewardTrack.FREE, PassRewardType.THEME, "Cosmic", "Space theme", "🌌", itemId = "theme_cosmic", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFF4B0082)),
            PassReward(90, RewardTrack.LEGENDARY, PassRewardType.EFFECT, "Galaxy Collapse", "Universal end", "🌠", itemId = "effect_galaxy_collapse", isExclusive = true, rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFF4B0082)),
            
            PassReward(91, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Ninety One", "Final stretch", "🏁", 3300, rarity = AchievementRarity.EPIC, previewColor = Color(0xFFDC143C)),
            PassReward(91, RewardTrack.PREMIUM, PassRewardType.SUGAR_POINTS, "Final Bonus", "Huge reward", "🎁", 4000, rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700)),
            
            PassReward(92, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Ninety Two", "Atomic number", "⚛️", 3350, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFF98FF98)),
            PassReward(92, RewardTrack.PREMIUM, PassRewardType.BADGE, "Final Boss", "Last challenge", "👹", itemId = "badge_finalboss", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFDC143C)),
            
            PassReward(93, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Ninety Three", "Almost there", "🔜", 3400, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(93, RewardTrack.PREMIUM, PassRewardType.THEME, "Final Form", "Ultimate theme", "⚡", itemId = "theme_final", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFF4500)),
            
            PassReward(94, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Ninety Four", "Plutonium", "☢️", 3450, rarity = AchievementRarity.RARE, previewColor = Color(0xFF00FF00)),
            PassReward(94, RewardTrack.PREMIUM, PassRewardType.EFFECT, "Reality Tear", "Break the fabric", "🌀", itemId = "effect_reality", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFF9370DB)),
            
            PassReward(95, RewardTrack.FREE, PassRewardType.XP_BOOST, "Ninety Five", "Almost perfect", "📊", 35, rarity = AchievementRarity.EPIC, previewColor = Color(0xFF98FF98)),
            PassReward(95, RewardTrack.PREMIUM, PassRewardType.TITLE, "Almost There", "So close", "🏃", itemId = "title_almost", rarity = AchievementRarity.EPIC, previewColor = Color(0xFFFFD700)),
            
            PassReward(96, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Ninety Six", "K-Cartridge", "🎮", 3500, rarity = AchievementRarity.RARE, previewColor = Color(0xFFDC143C)),
            PassReward(96, RewardTrack.PREMIUM, PassRewardType.CURRENCY_BOOST, "Point Apocalypse", "+100% points for a month", "💰", 100, rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700)),
            
            PassReward(97, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Ninety Seven", "Prime 97", "🔢", 3550, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFF9370DB)),
            PassReward(97, RewardTrack.PREMIUM, PassRewardType.BADGE, "Penultimate", "Second to last", "🥈", itemId = "badge_penultimate", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFC0C0C0)),
            
            PassReward(98, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Ninety Eight", "Almost 100", "🔜", 3600, rarity = AchievementRarity.UNCOMMON, previewColor = Color(0xFFFFB6C1)),
            PassReward(98, RewardTrack.PREMIUM, PassRewardType.PARTICLE_STYLE, "Ascension", "Rising up", "☁️", itemId = "particles_ascension", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFFFFF)),
            
            PassReward(99, RewardTrack.FREE, PassRewardType.SUGAR_POINTS, "Ninety Nine", "One to go", "🎯", 4000, rarity = AchievementRarity.EPIC, previewColor = Color(0xFFFFD700)),
            PassReward(99, RewardTrack.PREMIUM, PassRewardType.TITLE, "The Chosen One", "Destined for greatness", "⚡", itemId = "title_chosen", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700))
        ))
        
        // ═════════════════════════════════════════════════════════════
        // TIER 100: ULTIMATE - SUGAR DEITY
        // ═════════════════════════════════════════════════════════════
        addAll(listOf(
            PassReward(100, RewardTrack.FREE, PassRewardType.THEME, "Immortal", "Eternal glory theme", "👑", itemId = "theme_immortal", rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700)),
            PassReward(100, RewardTrack.LEGENDARY, PassRewardType.BADGE, "Sugar Deity", "You ARE the sugar", "☀️", itemId = "badge_deity", isExclusive = true, rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700)),
            PassReward(100, RewardTrack.LEGENDARY, PassRewardType.EFFECT, "God Mode", "Ultimate power", "⚡", itemId = "effect_godmode", isExclusive = true, rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700)),
            PassReward(100, RewardTrack.LEGENDARY, PassRewardType.FEATURE_UNLOCK, "Everything", "All future content free", "🔓", itemId = "feature_everything", isExclusive = true, rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700)),
            PassReward(100, RewardTrack.LEGENDARY, PassRewardType.SUGAR_POINTS, "Infinite Sugar", "Unlimited wealth", "💰", 10000, isExclusive = true, rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700)),
            PassReward(100, RewardTrack.LEGENDARY, PassRewardType.TITLE, "Sugar Deity", "Bow before me", "👑", itemId = "title_deity", isExclusive = true, rarity = AchievementRarity.LEGENDARY, previewColor = Color(0xFFFFD700))
        ))
    }
    
    // ═════════════════════════════════════════════════════════════
    // PUBLIC API
    // ═════════════════════════════════════════════════════════════
    
    fun getAllRewards(): List<PassReward> = allRewards.toList()
    
    fun getRewardsForTier(tier: Int): List<PassReward> {
        return allRewards.filter { it.tier == tier }
    }
    
    fun getFreeRewards(): List<PassReward> {
        return allRewards.filter { it.track == RewardTrack.FREE }
    }
    
    fun getPremiumRewards(): List<PassReward> {
        return allRewards.filter { it.track != RewardTrack.FREE }
    }
    
    fun getLegendaryRewards(): List<PassReward> {
        return allRewards.filter { it.track == RewardTrack.LEGENDARY }
    }
    
    fun getExclusiveRewards(): List<PassReward> {
        return allRewards.filter { it.isExclusive }
    }
    
    fun getRewardForTier(tier: Int, track: RewardTrack): PassReward? {
        return allRewards.find { it.tier == tier && it.track == track }
    }
    
    /**
     * Get total value of free track
     */
    fun getFreeTrackTotalValue(): Int {
        return allRewards
            .filter { it.track == RewardTrack.FREE && it.type == PassRewardType.SUGAR_POINTS }
            .sumOf { it.value }
    }
    
    /**
     * Get total value of premium track
     */
    fun getPremiumTrackTotalValue(): Int {
        return allRewards
            .filter { it.track != RewardTrack.FREE && it.type == PassRewardType.SUGAR_POINTS }
            .sumOf { it.value }
    }
    
    /**
     * Count rewards by type
     */
    fun getRewardTypeCounts(): Map<PassRewardType, Int> {
        return allRewards.groupingBy { it.type }.eachCount()
    }
    
    /**
     * Get rewards preview for a range of tiers
     */
    fun getRewardsPreview(startTier: Int, endTier: Int): List<PassReward> {
        return allRewards.filter { it.tier in startTier..endTier }
    }
    
    /**
     * Get the next legendary tier after current
     */
    fun getNextLegendaryTier(currentTier: Int): Int? {
        val legendaryTiers = allRewards
            .filter { it.track == RewardTrack.LEGENDARY }
            .map { it.tier }
            .distinct()
            .sorted()
        return legendaryTiers.find { it > currentTier }
    }
    
    /**
     * Get total XP needed to reach max tier
     */
    fun getTotalXpToMax(): Int = getCumulativeXpForTier(100)
    
    /**
     * Calculate progress percentage to max tier
     */
    fun getProgressToMax(currentXp: Int): Float {
        val total = getTotalXpToMax()
        return (currentXp.toFloat() / total).coerceIn(0f, 1f)
    }
}
