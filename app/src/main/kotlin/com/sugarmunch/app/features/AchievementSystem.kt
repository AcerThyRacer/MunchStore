package com.sugarmunch.app.features

import com.sugarmunch.app.features.model.*

/**
 * SugarMunch Achievement System - 50+ Achievements
 */
object AchievementSystem {
    
    val ALL_ACHIEVEMENTS = listOf(
        // ═════════════════════════════════════════════════════════════
        // INSTALLER - App Installation Achievements (10)
        // ═════════════════════════════════════════════════════════════
        Achievement(
            id = "first_bite",
            name = "First Bite",
            description = "Install your first app",
            icon = "🍬",
            category = AchievementCategory.INSTALLER,
            rarity = AchievementRarity.COMMON,
            requirementType = "install_apps",
            requirementValue = 1,
            order = 1
        ),
        Achievement(
            id = "candy_collector",
            name = "Candy Collector",
            description = "Install 5 apps",
            icon = "🍭",
            category = AchievementCategory.INSTALLER,
            rarity = AchievementRarity.COMMON,
            requirementType = "install_apps",
            requirementValue = 5,
            order = 2
        ),
        Achievement(
            id = "sugar_rush",
            name = "Sugar Rush",
            description = "Install 10 apps",
            icon = "🍫",
            category = AchievementCategory.INSTALLER,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "install_apps",
            requirementValue = 10,
            order = 3
        ),
        Achievement(
            id = "sweet_tooth",
            name = "Sweet Tooth",
            description = "Install 25 apps",
            icon = "🧁",
            category = AchievementCategory.INSTALLER,
            rarity = AchievementRarity.RARE,
            requirementType = "install_apps",
            requirementValue = 25,
            order = 4
        ),
        Achievement(
            id = "candy_hoarder",
            name = "Candy Hoarder",
            description = "Install 50 apps",
            icon = "🍰",
            category = AchievementCategory.INSTALLER,
            rarity = AchievementRarity.EPIC,
            requirementType = "install_apps",
            requirementValue = 50,
            order = 5
        ),
        Achievement(
            id = "ultimate_candy_king",
            name = "Ultimate Candy King",
            description = "Install 100 apps",
            icon = "👑",
            category = AchievementCategory.INSTALLER,
            rarity = AchievementRarity.LEGENDARY,
            requirementType = "install_apps",
            requirementValue = 100,
            order = 6
        ),
        Achievement(
            id = "sugartube_fan",
            name = "Sugartube Fan",
            description = "Install Sugartube",
            icon = "📺",
            category = AchievementCategory.INSTALLER,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "install_specific",
            requirementValue = 1,
            order = 7
        ),
        Achievement(
            id = "night_owl",
            name = "Night Owl",
            description = "Install an app after midnight",
            icon = "🦉",
            category = AchievementCategory.INSTALLER,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "install_time",
            requirementValue = 1,
            hidden = true,
            order = 8
        ),
        Achievement(
            id = "early_bird",
            name = "Early Bird",
            description = "Install an app before 6 AM",
            icon = "🐦",
            category = AchievementCategory.INSTALLER,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "install_time",
            requirementValue = 1,
            hidden = true,
            order = 9
        ),
        Achievement(
            id = "installer_streak",
            name = "On a Roll",
            description = "Install 3 apps in one day",
            icon = "🔥",
            category = AchievementCategory.INSTALLER,
            rarity = AchievementRarity.RARE,
            requirementType = "install_streak",
            requirementValue = 3,
            order = 10
        ),
        
        // ═════════════════════════════════════════════════════════════
        // EXPLORER - Discovery Achievements (8)
        // ═════════════════════════════════════════════════════════════
        Achievement(
            id = "window_shopper",
            name = "Window Shopper",
            description = "Browse 10 apps",
            icon = "🛒",
            category = AchievementCategory.EXPLORER,
            rarity = AchievementRarity.COMMON,
            requirementType = "browse_apps",
            requirementValue = 10,
            order = 11
        ),
        Achievement(
            id = "curious_cat",
            name = "Curious Cat",
            description = "Browse 50 apps",
            icon = "🐱",
            category = AchievementCategory.EXPLORER,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "browse_apps",
            requirementValue = 50,
            order = 12
        ),
        Achievement(
            id = "explorer",
            name = "Explorer",
            description = "Browse 200 apps",
            icon = "🧭",
            category = AchievementCategory.EXPLORER,
            rarity = AchievementRarity.RARE,
            requirementType = "browse_apps",
            requirementValue = 200,
            order = 13
        ),
        Achievement(
            id = "category_hopper",
            name = "Category Hopper",
            description = "Browse all categories",
            icon = "📂",
            category = AchievementCategory.EXPLORER,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "browse_categories",
            requirementValue = 5,
            order = 14
        ),
        Achievement(
            id = "search_master",
            name = "Search Master",
            description = "Perform 20 searches",
            icon = "🔍",
            category = AchievementCategory.EXPLORER,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "search_count",
            requirementValue = 20,
            order = 15
        ),
        Achievement(
            id = "detail_oriented",
            name = "Detail Oriented",
            description = "View 30 app details",
            icon = "🔎",
            category = AchievementCategory.EXPLORER,
            rarity = AchievementRarity.RARE,
            requirementType = "view_details",
            requirementValue = 30,
            order = 16
        ),
        Achievement(
            id = "night_mode_explorer",
            name = "Darkness Walker",
            description = "Browse for 10 minutes in dark mode",
            icon = "🌙",
            category = AchievementCategory.EXPLORER,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "dark_mode_time",
            requirementValue = 10,
            hidden = true,
            order = 17
        ),
        Achievement(
            id = "marathon_browser",
            name = "Marathon Browser",
            description = "Spend 1 hour browsing apps",
            icon = "⏱️",
            category = AchievementCategory.EXPLORER,
            rarity = AchievementRarity.RARE,
            requirementType = "browse_time",
            requirementValue = 60,
            order = 18
        ),
        
        // ═════════════════════════════════════════════════════════════
        // CUSTOMIZER - Theme & Effect Achievements (12)
        // ═════════════════════════════════════════════════════════════
        Achievement(
            id = "first_theme",
            name = "First Paint",
            description = "Change your theme for the first time",
            icon = "🎨",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.COMMON,
            requirementType = "change_theme",
            requirementValue = 1,
            order = 19
        ),
        Achievement(
            id = "theme_collector",
            name = "Theme Collector",
            description = "Try 5 different themes",
            icon = "🖌️",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "try_themes",
            requirementValue = 5,
            order = 20
        ),
        Achievement(
            id = "chameleon",
            name = "Chameleon",
            description = "Try 15 different themes",
            icon = "🦎",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.RARE,
            requirementType = "try_themes",
            requirementValue = 15,
            order = 21
        ),
        Achievement(
            id = "trippy_explorer",
            name = "Trippy Explorer",
            description = "Use a trippy theme",
            icon = "🌀",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "use_trippy_theme",
            requirementValue = 1,
            hidden = true,
            order = 22
        ),
        Achievement(
            id = "intensity_freak",
            name = "Intensity Freak",
            description = "Set theme intensity to maximum",
            icon = "🔥",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "max_intensity",
            requirementValue = 1,
            order = 23
        ),
        Achievement(
            id = "effect_enthusiast",
            name = "Effect Enthusiast",
            description = "Enable your first effect",
            icon = "✨",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.COMMON,
            requirementType = "enable_effect",
            requirementValue = 1,
            order = 24
        ),
        Achievement(
            id = "effect_collector",
            name = "Effect Collector",
            description = "Enable 5 different effects",
            icon = "🎭",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "enable_effects",
            requirementValue = 5,
            order = 25
        ),
        Achievement(
            id = "effect_master",
            name = "Effect Master",
            description = "Enable 10 different effects",
            icon = "🎪",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.RARE,
            requirementType = "enable_effects",
            requirementValue = 10,
            order = 26
        ),
        Achievement(
            id = "combo_king",
            name = "Combo King",
            description = "Use an effect preset with 3+ effects",
            icon = "🎯",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "use_preset",
            requirementValue = 1,
            order = 27
        ),
        Achievement(
            id = "sugarrush_mode",
            name = "SugarRush Mode",
            description = "Use the SugarRush preset",
            icon = "🚀",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.RARE,
            requirementType = "use_sugarrush",
            requirementValue = 1,
            order = 28
        ),
        Achievement(
            id = "maximum_overdrive",
            name = "MAXIMUM OVERDRIVE",
            description = "Use maximum intensity for 5 minutes",
            icon = "⚡",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.EPIC,
            requirementType = "max_intensity_time",
            requirementValue = 5,
            order = 29
        ),
        Achievement(
            id = "trippy_master",
            name = "Trippy Master",
            description = "Use Acid Trip theme + Acid Trip preset",
            icon = "🌈",
            category = AchievementCategory.CUSTOMIZER,
            rarity = AchievementRarity.LEGENDARY,
            requirementType = "full_acid",
            requirementValue = 1,
            hidden = true,
            order = 30
        ),
        
        // ═════════════════════════════════════════════════════════════
        // SOCIAL - Social Achievements (8)
        // ═════════════════════════════════════════════════════════════
        Achievement(
            id = "social_butterfly",
            name = "Social Butterfly",
            description = "Share an app with a friend",
            icon = "🦋",
            category = AchievementCategory.SOCIAL,
            rarity = AchievementRarity.COMMON,
            requirementType = "share_app",
            requirementValue = 1,
            order = 31
        ),
        Achievement(
            id = "sharer",
            name = "Sharer's Spirit",
            description = "Share 10 apps",
            icon = "📤",
            category = AchievementCategory.SOCIAL,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "share_apps",
            requirementValue = 10,
            order = 32
        ),
        Achievement(
            id = "first_friend",
            name = "First Friend",
            description = "Make your first friend",
            icon = "🤝",
            category = AchievementCategory.SOCIAL,
            rarity = AchievementRarity.COMMON,
            requirementType = "make_friend",
            requirementValue = 1,
            order = 33
        ),
        Achievement(
            id = "popular_kid",
            name = "Popular Kid",
            description = "Get 10 friends",
            icon = "⭐",
            category = AchievementCategory.SOCIAL,
            rarity = AchievementRarity.RARE,
            requirementType = "make_friends",
            requirementValue = 10,
            order = 34
        ),
        Achievement(
            id = "rater",
            name = "Rater",
            description = "Rate your first app",
            icon = "⭐",
            category = AchievementCategory.SOCIAL,
            rarity = AchievementRarity.COMMON,
            requirementType = "rate_app",
            requirementValue = 1,
            order = 35
        ),
        Achievement(
            id = "critic",
            name = "Critic",
            description = "Rate 10 apps",
            icon = "📝",
            category = AchievementCategory.SOCIAL,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "rate_apps",
            requirementValue = 10,
            order = 36
        ),
        Achievement(
            id = "reviewer",
            name = "Reviewer",
            description = "Write 5 reviews",
            icon = "✍️",
            category = AchievementCategory.SOCIAL,
            rarity = AchievementRarity.RARE,
            requirementType = "write_reviews",
            requirementValue = 5,
            order = 37
        ),
        Achievement(
            id = "community_hero",
            name = "Community Hero",
            description = "Create a public collection",
            icon = "🦸",
            category = AchievementCategory.SOCIAL,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "create_collection",
            requirementValue = 1,
            order = 38
        ),
        
        // ═════════════════════════════════════════════════════════════
        // COLLECTOR - Favorites & Collections (6)
        // ═════════════════════════════════════════════════════════════
        Achievement(
            id = "first_favorite",
            name = "First Favorite",
            description = "Favorite your first app",
            icon = "❤️",
            category = AchievementCategory.COLLECTOR,
            rarity = AchievementRarity.COMMON,
            requirementType = "favorite_app",
            requirementValue = 1,
            order = 39
        ),
        Achievement(
            id = "collector",
            name = "Collector",
            description = "Favorite 10 apps",
            icon = "💎",
            category = AchievementCategory.COLLECTOR,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "favorite_apps",
            requirementValue = 10,
            order = 40
        ),
        Achievement(
            id = "hoarder",
            name = "Hoarder",
            description = "Favorite 25 apps",
            icon = "👜",
            category = AchievementCategory.COLLECTOR,
            rarity = AchievementRarity.RARE,
            requirementType = "favorite_apps",
            requirementValue = 25,
            order = 41
        ),
        Achievement(
            id = "wishful_thinking",
            name = "Wishful Thinking",
            description = "Add an app to your wishlist",
            icon = "🌠",
            category = AchievementCategory.COLLECTOR,
            rarity = AchievementRarity.COMMON,
            requirementType = "wishlist_app",
            requirementValue = 1,
            order = 42
        ),
        Achievement(
            id = "dreamer",
            name = "Dreamer",
            description = "Have 10 apps in your wishlist",
            icon = "💭",
            category = AchievementCategory.COLLECTOR,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "wishlist_apps",
            requirementValue = 10,
            order = 43
        ),
        Achievement(
            id = "curator",
            name = "Curator",
            description = "Create 3 collections",
            icon = "🗂️",
            category = AchievementCategory.COLLECTOR,
            rarity = AchievementRarity.RARE,
            requirementType = "create_collections",
            requirementValue = 3,
            order = 44
        ),
        
        // ═════════════════════════════════════════════════════════════
        // MASTER - Completionist Achievements (6)
        // ═════════════════════════════════════════════════════════════
        Achievement(
            id = "daily_user",
            name = "Daily User",
            description = "Use the app 7 days in a row",
            icon = "📅",
            category = AchievementCategory.MASTER,
            rarity = AchievementRarity.UNCOMMON,
            requirementType = "streak_days",
            requirementValue = 7,
            order = 45
        ),
        Achievement(
            id = "dedicated",
            name = "Dedicated",
            description = "Use the app 30 days in a row",
            icon = "📆",
            category = AchievementCategory.MASTER,
            rarity = AchievementRarity.RARE,
            requirementType = "streak_days",
            requirementValue = 30,
            order = 46
        ),
        Achievement(
            id = "veteran",
            name = "Veteran",
            description = "Have a 100 day streak",
            icon = "🏆",
            category = AchievementCategory.MASTER,
            rarity = AchievementRarity.EPIC,
            requirementType = "streak_days",
            requirementValue = 100,
            order = 47
        ),
        Achievement(
            id = "legend",
            name = "Legend",
            description = "Have a 365 day streak",
            icon = "🗿",
            category = AchievementCategory.MASTER,
            rarity = AchievementRarity.LEGENDARY,
            requirementType = "streak_days",
            requirementValue = 365,
            order = 48
        ),
        Achievement(
            id = "achievement_hunter",
            name = "Achievement Hunter",
            description = "Unlock 25 achievements",
            icon = "🎯",
            category = AchievementCategory.MASTER,
            rarity = AchievementRarity.RARE,
            requirementType = "unlock_achievements",
            requirementValue = 25,
            order = 49
        ),
        Achievement(
            id = "completionist",
            name = "Completionist",
            description = "Unlock all achievements",
            icon = "💯",
            category = AchievementCategory.MASTER,
            rarity = AchievementRarity.LEGENDARY,
            requirementType = "unlock_all",
            requirementValue = 50,
            order = 50
        )
    )
    
    fun getById(id: String): Achievement? = ALL_ACHIEVEMENTS.find { it.id == id }
    
    fun getByCategory(category: AchievementCategory): List<Achievement> = 
        ALL_ACHIEVEMENTS.filter { it.category == category }
    
    fun getByRarity(rarity: AchievementRarity): List<Achievement> =
        ALL_ACHIEVEMENTS.filter { it.rarity == rarity }
    
    fun getHiddenAchievements(): List<Achievement> =
        ALL_ACHIEVEMENTS.filter { it.hidden }
    
    fun getVisibleAchievements(): List<Achievement> =
        ALL_ACHIEVEMENTS.filter { !it.hidden }
}
