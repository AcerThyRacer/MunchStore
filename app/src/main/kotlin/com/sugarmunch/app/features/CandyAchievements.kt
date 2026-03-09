package com.sugarmunch.app.features

import com.sugarmunch.app.features.model.*

/**
 * SugarMunch Extreme Achievement System - 30+ NEW Achievements
 * Expanded from 50 to 80+ total achievements
 * New categories: SUGAR_EXTREME, PARTICLE_PHYSICIST, CARD_STYLIST, CANDY_COLLECTOR
 */
object CandyAchievements {

    val NEW_ACHIEVEMENTS = listOf(
        // ═════════════════════════════════════════════════════════════
        // SUGAR_EXTREME - Theme Explorer Achievements (5 NEW)
        // ═════════════════════════════════════════════════════════════
        Achievement(
            id = "first_theme_extreme",
            name = "First Taste",
            description = "Equip your first extreme sugar theme",
            icon = "🍬",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.COMMON,
            requirementType = "equip_extreme_theme",
            requirementValue = 1,
            order = 51
        ),
        Achievement(
            id = "theme_collector_extreme",
            name = "Candy Connoisseur",
            description = "Equip 5 different extreme themes",
            icon = "🍫",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "equip_extreme_themes",
            requirementValue = 5,
            order = 52
        ),
        Achievement(
            id = "theme_master_extreme",
            name = "Sugar Master",
            description = "Equip 15 different extreme themes",
            icon = "🎨",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.RARE,
            requirementType = "equip_extreme_themes",
            requirementValue = 15,
            order = 53
        ),
        Achievement(
            id = "theme_legend_extreme",
            name = "Candy Legend",
            description = "Equip all 30+ extreme themes",
            icon = "👑",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.LEGENDARY,
            requirementType = "equip_extreme_themes",
            requirementValue = 30,
            order = 54
        ),
        Achievement(
            id = "sugar_connoisseur",
            name = "Sugar Connoisseur",
            description = "Create and save a custom theme",
            icon = "🏆",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.EPIC,
            requirementType = "create_custom_theme",
            requirementValue = 1,
            order = 55
        ),

        // ═════════════════════════════════════════════════════════════
        // CARD_STYLIST - Card Style Achievements (5 NEW)
        // ═════════════════════════════════════════════════════════════
        Achievement(
            id = "card_newbie",
            name = "Card Newbie",
            description = "Use 3 different card styles",
            icon = "🃏",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.COMMON,
            requirementType = "use_card_styles",
            requirementValue = 3,
            order = 56
        ),
        Achievement(
            id = "card_designer",
            name = "Card Designer",
            description = "Use 10 different card styles",
            icon = "🎴",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "use_card_styles",
            requirementValue = 10,
            order = 57
        ),
        Achievement(
            id = "card_virtuoso",
            name = "Card Virtuoso",
            description = "Customize card colors for 5 styles",
            icon = "🖌️",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.RARE,
            requirementType = "customize_card_colors",
            requirementValue = 5,
            order = 58
        ),
        Achievement(
            id = "card_artist",
            name = "Card Artist",
            description = "Create a custom card configuration",
            icon = "🎭",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.EPIC,
            requirementType = "create_card_config",
            requirementValue = 1,
            order = 59
        ),
        Achievement(
            id = "card_god",
            name = "Card God",
            description = "Unlock all card style effects",
            icon = "✨",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.LEGENDARY,
            requirementType = "unlock_card_effects",
            requirementValue = 20,
            order = 60
        ),

        // ═════════════════════════════════════════════════════════════
        // PARTICLE_PHYSICIST - Particle System Achievements (5 NEW)
        // ═════════════════════════════════════════════════════════════
        Achievement(
            id = "particle_observer",
            name = "Particle Observer",
            description = "Enable particle effects for the first time",
            icon = "⚛️",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.COMMON,
            requirementType = "enable_particles",
            requirementValue = 1,
            order = 61
        ),
        Achievement(
            id = "particle_researcher",
            name = "Particle Researcher",
            description = "Try 5 different particle types",
            icon = "🔬",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "try_particle_types",
            requirementValue = 5,
            order = 62
        ),
        Achievement(
            id = "particle_engineer",
            name = "Particle Engineer",
            description = "Customize particle physics settings",
            icon = "⚙️",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.RARE,
            requirementType = "customize_particle_physics",
            requirementValue = 1,
            order = 63
        ),
        Achievement(
            id = "particle_wizard",
            name = "Particle Wizard",
            description = "Set particle count to maximum",
            icon = "🧙",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.EPIC,
            requirementType = "max_particle_count",
            requirementValue = 1,
            order = 64
        ),
        Achievement(
            id = "particle_god",
            name = "Particle God",
            description = "Unlock rainbow particle effects",
            icon = "🌈",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.LEGENDARY,
            requirementType = "unlock_rainbow_particles",
            requirementValue = 1,
            order = 65
        ),

        // ═════════════════════════════════════════════════════════════
        // SUGAR_RUSH - Effect Achievements (5 NEW)
        // ═════════════════════════════════════════════════════════════
        Achievement(
            id = "first_rush",
            name = "First Rush",
            description = "Enable Sugar Rush mode for the first time",
            icon = "⚡",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "enable_sugar_rush",
            requirementValue = 1,
            order = 66
        ),
        Achievement(
            id = "rush_addict",
            name = "Rush Addict",
            description = "Have 10 Sugar Rush sessions",
            icon = "🔋",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.RARE,
            requirementType = "sugar_rush_sessions",
            requirementValue = 10,
            order = 67
        ),
        Achievement(
            id = "rush_master",
            name = "Rush Master",
            description = "Customize all intensity settings",
            icon = "🎛️",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.EPIC,
            requirementType = "customize_all_intensities",
            requirementValue = 1,
            order = 68
        ),
        Achievement(
            id = "rush_legend",
            name = "Rush Legend",
            description = "Achieve nuclear overload mode",
            icon = "☢️",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.LEGENDARY,
            requirementType = "nuclear_overload",
            requirementValue = 1,
            order = 69
        ),
        Achievement(
            id = "rush_divine",
            name = "Rush Divine",
            description = "Achieve maximum sweetness level",
            icon = "💫",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.LEGENDARY,
            requirementType = "maximum_sweetness",
            requirementValue = 1,
            order = 70
        ),

        // ═════════════════════════════════════════════════════════════
        // CANDY_COLLECTOR - Collection Achievements (5 NEW)
        // ═════════════════════════════════════════════════════════════
        Achievement(
            id = "candy_first",
            name = "Candy First",
            description = "Collect your first candy badge",
            icon = "🍭",
            category = AchievementCategory.COLLECTOR,
            rarity = AchievementRarity.COMMON,
            requirementType = "collect_badges",
            requirementValue = 1,
            order = 71
        ),
        Achievement(
            id = "candy_hunter",
            name = "Candy Hunter",
            description = "Collect 10 candy badges",
            icon = "🎯",
            category = AchievementCategory.COLLECTOR,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "collect_badges",
            requirementValue = 10,
            order = 72
        ),
        Achievement(
            id = "candy_master",
            name = "Candy Master",
            description = "Collect 25 candy badges",
            icon = "🏅",
            category = AchievementCategory.COLLECTOR,
            rarity = AchievementRarity.RARE,
            requirementType = "collect_badges",
            requirementValue = 25,
            order = 73
        ),
        Achievement(
            id = "candy_champion",
            name = "Candy Champion",
            description = "Collect 50 candy badges",
            icon = "🏆",
            category = AchievementCategory.COLLECTOR,
            rarity = AchievementRarity.EPIC,
            requirementType = "collect_badges",
            requirementValue = 50,
            order = 74
        ),
        Achievement(
            id = "candy_immortal",
            name = "Candy Immortal",
            description = "Collect all 80+ achievements",
            icon = "💎",
            category = AchievementCategory.MASTER,
            rarity = AchievementRarity.LEGENDARY,
            requirementType = "all_achievements",
            requirementValue = 80,
            order = 75
        ),

        // ═════════════════════════════════════════════════════════════
        // SPECIAL - Special Achievements (5 NEW)
        // ═════════════════════════════════════════════════════════════
        Achievement(
            id = "candy_factory_graduate",
            name = "Candy Factory Graduate",
            description = "Complete the Candy Factory onboarding",
            icon = "🎓",
            category = AchievementCategory.EXPLORER,
            rarity = AchievementRarity.COMMON,
            requirementType = "complete_onboarding",
            requirementValue = 1,
            order = 76
        ),
        Achievement(
            id = "customization_king",
            name = "Customization King",
            description = "Fully customize your app experience",
            icon = "🤴",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.EPIC,
            requirementType = "full_customization",
            requirementValue = 1,
            order = 77
        ),
        Achievement(
            id = "effect_master",
            name = "Effect Master",
            description = "Activate all 15 sugar effects",
            icon = "🎪",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.LEGENDARY,
            requirementType = "activate_all_sugar_effects",
            requirementValue = 15,
            order = 78
        ),
        Achievement(
            id = "theme_creator",
            name = "Theme Creator",
            description = "Export and import a custom theme",
            icon = "📤",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.RARE,
            requirementType = "export_import_theme",
            requirementValue = 1,
            order = 79
        ),
        Achievement(
            id = "sugar_legend",
            name = "Sugar Legend",
            description = "Unlock all badges and become a legend",
            icon = "🌟",
            category = AchievementCategory.MASTER,
            rarity = AchievementRarity.LEGENDARY,
            requirementType = "unlock_all_badges",
            requirementValue = 1,
            order = 80
        )
    )

    // ═════════════════════════════════════════════════════════════════
    // HELPER FUNCTIONS
    // ═════════════════════════════════════════════════════════════════

    fun getById(id: String): Achievement? = NEW_ACHIEVEMENTS.find { it.id == id }

    fun getByCategory(category: AchievementCategory): List<Achievement> =
        NEW_ACHIEVEMENTS.filter { it.category == category }

    fun getByRarity(rarity: AchievementRarity): List<Achievement> =
        NEW_ACHIEVEMENTS.filter { it.rarity == rarity }

    fun getHiddenAchievements(): List<Achievement> =
        NEW_ACHIEVEMENTS.filter { it.hidden }

    fun getVisibleAchievements(): List<Achievement> =
        NEW_ACHIEVEMENTS.filter { !it.hidden }

    fun getNewAchievements(): List<Achievement> = NEW_ACHIEVEMENTS

    /**
     * Get all achievements including base AchievementSystem achievements
     */
    fun getAllAchievements(): List<Achievement> {
        return AchievementSystem.ALL_ACHIEVEMENTS + NEW_ACHIEVEMENTS
    }

    /**
     * Get achievements by order
     */
    fun getOrderedAchievements(): List<Achievement> {
        return getAllAchievements().sortedBy { it.order }
    }

    /**
     * Get next achievement for a user based on progress
     */
    fun getNextAchievement(
        unlockedIds: Set<String>,
        category: AchievementCategory? = null
    ): Achievement? {
        val achievements = category?.let { getByCategory(it) } ?: NEW_ACHIEVEMENTS
        return achievements
            .filter { it.id !in unlockedIds }
            .sortedBy { it.order }
            .firstOrNull()
    }

    /**
     * Calculate completion percentage
     */
    fun calculateCompletion(unlockedIds: Set<String>): Float {
        val total = getAllAchievements().size
        val unlocked = unlockedIds.count { id ->
            getAllAchievements().any { it.id == id }
        }
        return unlocked.toFloat() / total
    }
}
