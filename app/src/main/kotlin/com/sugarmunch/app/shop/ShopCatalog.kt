package com.sugarmunch.app.shop

import com.sugarmunch.app.features.model.Rarity

/**
 * Sugar Shop Catalog - All available items
 * 50+ exclusive items across 8 categories
 */
object ShopCatalog {

    // ═════════════════════════════════════════════════════════════
    // EXCLUSIVE THEMES (10 items)
    // ═════════════════════════════════════════════════════════════
    val EXCLUSIVE_THEMES = listOf(
        ShopItem(
            id = "theme_golden_candy",
            name = "Golden Candy",
            description = "Pure gold and caramel luxury",
            type = ShopItemType.THEME,
            cost = 500,
            rarity = Rarity.EPIC,
            icon = "🌟",
            previewColor = "#FFD700",
            requirements = ShopRequirements(minLevel = 5)
        ),
        ShopItem(
            id = "theme_diamond_dust",
            name = "Diamond Dust",
            description = "Sparkling ice crystal elegance",
            type = ShopItemType.THEME,
            cost = 750,
            rarity = Rarity.LEGENDARY,
            icon = "💎",
            previewColor = "#B9F2FF",
            requirements = ShopRequirements(minLevel = 10, achievementCount = 20)
        ),
        ShopItem(
            id = "theme_neon_cyber",
            name = "Cyber Candy",
            description = "Cyberpunk neon aesthetic",
            type = ShopItemType.THEME,
            cost = 600,
            rarity = Rarity.RARE,
            icon = "🌆",
            previewColor = "#FF00FF",
            availability = ShopItemAvailability.LIMITED_TIME
        ),
        ShopItem(
            id = "theme_sakura",
            name = "Sakura Dreams",
            description = "Cherry blossom serenity",
            type = ShopItemType.THEME,
            cost = 400,
            rarity = Rarity.UNCOMMON,
            icon = "🌸",
            previewColor = "#FFB7C5",
            availability = ShopItemAvailability.SEASONAL
        ),
        ShopItem(
            id = "theme_volcanic",
            name = "Volcanic Heat",
            description = "Molten lava and embers",
            type = ShopItemType.THEME,
            cost = 550,
            rarity = Rarity.RARE,
            icon = "🌋",
            previewColor = "#FF4500",
            requirements = ShopRequirements(totalInstalls = 50)
        ),
        ShopItem(
            id = "theme_aurora",
            name = "Aurora Borealis",
            description = "Northern lights dancing",
            type = ShopItemType.THEME,
            cost = 800,
            rarity = Rarity.LEGENDARY,
            icon = "✨",
            previewColor = "#00CED1",
            availability = ShopItemAvailability.EXCLUSIVE,
            requirements = ShopRequirements(minLevel = 15)
        ),
        ShopItem(
            id = "theme_retro_arcade",
            name = "Retro Arcade",
            description = "8-bit gaming nostalgia",
            type = ShopItemType.THEME,
            cost = 450,
            rarity = Rarity.UNCOMMON,
            icon = "🕹️",
            previewColor = "#FF6B35"
        ),
        ShopItem(
            id = "theme_midnight_garden",
            name = "Midnight Garden",
            description = "Bioluminescent flora",
            type = ShopItemType.THEME,
            cost = 500,
            rarity = Rarity.RARE,
            icon = "🦋",
            previewColor = "#4B0082"
        ),
        ShopItem(
            id = "theme_cosmic_swirl",
            name = "Cosmic Swirl",
            description = "Galaxy spirals and stars",
            type = ShopItemType.THEME,
            cost = 650,
            rarity = Rarity.EPIC,
            icon = "🌌",
            previewColor = "#4B0082",
            requirements = ShopRequirements(streakDays = 30)
        ),
        ShopItem(
            id = "theme_rainbow_unicorn",
            name = "Unicorn Magic",
            description = "Maximum sparkle power",
            type = ShopItemType.THEME,
            cost = 1000,
            rarity = Rarity.LEGENDARY,
            icon = "🦄",
            previewColor = "#FF69B4",
            availability = ShopItemAvailability.EXCLUSIVE,
            requirements = ShopRequirements(minLevel = 20, achievementCount = 40)
        )
    )

    // ═════════════════════════════════════════════════════════════
    // PREMIUM EFFECTS (8 items)
    // ═════════════════════════════════════════════════════════════
    val PREMIUM_EFFECTS = listOf(
        ShopItem(
            id = "effect_dragon_breath",
            name = "Dragon Breath",
            description = "Fiery particles that follow your touch",
            type = ShopItemType.EFFECT,
            cost = 300,
            rarity = Rarity.RARE,
            icon = "🐉",
            previewColor = "#FF4500",
            requirements = ShopRequirements(minLevel = 3)
        ),
        ShopItem(
            id = "effect_stardust",
            name = "Stardust Trail",
            description = "Leave a trail of glittering stars",
            type = ShopItemType.EFFECT,
            cost = 400,
            rarity = Rarity.EPIC,
            icon = "✨",
            previewColor = "#FFD700",
            requirements = ShopRequirements(minLevel = 8)
        ),
        ShopItem(
            id = "effect_bubbles",
            name = "Bubble Pop",
            description = "Floating bubbles that pop on tap",
            type = ShopItemType.EFFECT,
            cost = 200,
            rarity = Rarity.UNCOMMON,
            icon = "🫧",
            previewColor = "#87CEEB"
        ),
        ShopItem(
            id = "effect_lightning",
            name = "Thunder Strike",
            description = "Electric surges on interactions",
            type = ShopItemType.EFFECT,
            cost = 500,
            rarity = Rarity.EPIC,
            icon = "⚡",
            previewColor = "#FFFF00",
            requirements = ShopRequirements(totalInstalls = 25)
        ),
        ShopItem(
            id = "effect_heart_rain",
            name = "Heart Shower",
            description = "Romantic falling hearts",
            type = ShopItemType.EFFECT,
            cost = 250,
            rarity = Rarity.UNCOMMON,
            icon = "💕",
            previewColor = "#FF69B4",
            availability = ShopItemAvailability.SEASONAL
        ),
        ShopItem(
            id = "effect_matrix",
            name = "Matrix Code",
            description = "Digital rain effect",
            type = ShopItemType.EFFECT,
            cost = 450,
            rarity = Rarity.RARE,
            icon = "💻",
            previewColor = "#00FF00"
        ),
        ShopItem(
            id = "effect_snowfall",
            name = "Winter Snow",
            description = "Gentle snowflakes falling",
            type = ShopItemType.EFFECT,
            cost = 200,
            rarity = Rarity.UNCOMMON,
            icon = "❄️",
            previewColor = "#FFFFFF",
            availability = ShopItemAvailability.SEASONAL
        ),
        ShopItem(
            id = "effect_galaxy_spiral",
            name = "Galaxy Spiral",
            description = "Spinning galaxy arms",
            type = ShopItemType.EFFECT,
            cost = 600,
            rarity = Rarity.LEGENDARY,
            icon = "🌀",
            previewColor = "#9400D3",
            requirements = ShopRequirements(minLevel = 12)
        )
    )

    // ═════════════════════════════════════════════════════════════
    // PROFILE BADGES (10 items)
    // ═════════════════════════════════════════════════════════════
    val PROFILE_BADGES = listOf(
        ShopItem(
            id = "badge_sugar_newbie",
            name = "Sugar Newbie",
            description = "Just getting started",
            type = ShopItemType.BADGE,
            cost = 50,
            rarity = Rarity.COMMON,
            icon = "🍬",
            previewColor = "#FFB6C1"
        ),
        ShopItem(
            id = "badge_candy_crusher",
            name = "Candy Crusher",
            description = "Serious about sweets",
            type = ShopItemType.BADGE,
            cost = 150,
            rarity = Rarity.UNCOMMON,
            icon = "🍭",
            previewColor = "#FF69B4",
            requirements = ShopRequirements(minLevel = 5)
        ),
        ShopItem(
            id = "badge_chocolate_king",
            name = "Chocolate King",
            description = "Rich taste in apps",
            type = ShopItemType.BADGE,
            cost = 300,
            rarity = Rarity.RARE,
            icon = "🍫",
            previewColor = "#8B4513",
            requirements = ShopRequirements(totalInstalls = 20)
        ),
        ShopItem(
            id = "badge_gummy_bear",
            name = "Gummy Bear",
            description = "Bouncy and fun",
            type = ShopItemType.BADGE,
            cost = 200,
            rarity = Rarity.UNCOMMON,
            icon = "🧸",
            previewColor = "#FF1493"
        ),
        ShopItem(
            id = "badge_legendary",
            name = "Legend",
            description = "Reach level 20",
            type = ShopItemType.BADGE,
            cost = 500,
            rarity = Rarity.LEGENDARY,
            icon = "👑",
            previewColor = "#FFD700",
            requirements = ShopRequirements(minLevel = 20)
        ),
        ShopItem(
            id = "badge_night_owl",
            name = "Night Owl",
            description = "Midnight installer",
            type = ShopItemType.BADGE,
            cost = 250,
            rarity = Rarity.RARE,
            icon = "🦉",
            previewColor = "#483D8B"
        ),
        ShopItem(
            id = "badge_collector",
            name = "Master Collector",
            description = "Collect 30 achievements",
            type = ShopItemType.BADGE,
            cost = 400,
            rarity = Rarity.EPIC,
            icon = "🏆",
            previewColor = "#FFA500",
            requirements = ShopRequirements(achievementCount = 30)
        ),
        ShopItem(
            id = "badge_socialite",
            name = "Socialite",
            description = "Popular in the community",
            type = ShopItemType.BADGE,
            cost = 350,
            rarity = Rarity.RARE,
            icon = "🌟",
            previewColor = "#FF1493",
            requirements = ShopRequirements(achievementIds = listOf("popular_kid"))
        ),
        ShopItem(
            id = "badge_early_adopter",
            name = "Early Adopter",
            description = "Founding member",
            type = ShopItemType.BADGE,
            cost = 0,
            rarity = Rarity.LEGENDARY,
            icon = "🚀",
            previewColor = "#1E90FF",
            availability = ShopItemAvailability.EXCLUSIVE,
            limitedQuantity = 1000
        ),
        ShopItem(
            id = "badge_completionist",
            name = "Completionist",
            description = "Unlock everything",
            type = ShopItemType.BADGE,
            cost = 1000,
            rarity = Rarity.LEGENDARY,
            icon = "💯",
            previewColor = "#9400D3",
            requirements = ShopRequirements(achievementIds = listOf("completionist"))
        )
    )

    // ═════════════════════════════════════════════════════════════
    // APP ICONS (8 items)
    // ═════════════════════════════════════════════════════════════
    val APP_ICONS = listOf(
        ShopItem(
            id = "icon_classic",
            name = "Classic Candy",
            description = "The original SugarMunch icon",
            type = ShopItemType.ICON,
            cost = 0,
            rarity = Rarity.COMMON,
            icon = "🍬",
            previewColor = "#FF69B4"
        ),
        ShopItem(
            id = "icon_lollipop",
            name = "Lollipop",
            description = "Sweet and colorful",
            type = ShopItemType.ICON,
            cost = 100,
            rarity = Rarity.UNCOMMON,
            icon = "🍭",
            previewColor = "#FF1493"
        ),
        ShopItem(
            id = "icon_chocolate",
            name = "Chocolate Bar",
            description = "Rich and smooth",
            type = ShopItemType.ICON,
            cost = 100,
            rarity = Rarity.UNCOMMON,
            icon = "🍫",
            previewColor = "#8B4513"
        ),
        ShopItem(
            id = "icon_cake",
            name = "Birthday Cake",
            description = "Celebrate every day",
            type = ShopItemType.ICON,
            cost = 200,
            rarity = Rarity.RARE,
            icon = "🎂",
            previewColor = "#FFB6C1"
        ),
        ShopItem(
            id = "icon_ice_cream",
            name = "Ice Cream",
            description = "Cool and refreshing",
            type = ShopItemType.ICON,
            cost = 150,
            rarity = Rarity.UNCOMMON,
            icon = "🍦",
            previewColor = "#87CEEB"
        ),
        ShopItem(
            id = "icon_donut",
            name = "Glazed Donut",
            description = "Sweet perfection",
            type = ShopItemType.ICON,
            cost = 150,
            rarity = Rarity.UNCOMMON,
            icon = "🍩",
            previewColor = "#D2691E"
        ),
        ShopItem(
            id = "icon_crown",
            name = "Royal Crown",
            description = "King of candy",
            type = ShopItemType.ICON,
            cost = 500,
            rarity = Rarity.EPIC,
            icon = "👑",
            previewColor = "#FFD700",
            requirements = ShopRequirements(minLevel = 15)
        ),
        ShopItem(
            id = "icon_crystal_ball",
            name = "Crystal Ball",
            description = "See the future of apps",
            type = ShopItemType.ICON,
            cost = 400,
            rarity = Rarity.RARE,
            icon = "🔮",
            previewColor = "#9370DB"
        )
    )

    // ═════════════════════════════════════════════════════════════
    // BOOSTS (6 items)
    // ═════════════════════════════════════════════════════════════
    val BOOSTS = listOf(
        ShopItem(
            id = "boost_sugar_x2",
            name = "Sugar Rush",
            description = "2x Sugar Points for 24 hours",
            type = ShopItemType.BOOST,
            cost = 150,
            rarity = Rarity.UNCOMMON,
            icon = "⚡",
            previewColor = "#FFD700"
        ),
        ShopItem(
            id = "boost_sugar_x3",
            name = "Sugar Overload",
            description = "3x Sugar Points for 24 hours",
            type = ShopItemType.BOOST,
            cost = 350,
            rarity = Rarity.RARE,
            icon = "🚀",
            previewColor = "#FF4500"
        ),
        ShopItem(
            id = "boost_xp_x2",
            name = "Learning Curve",
            description = "2x XP gain for 24 hours",
            type = ShopItemType.BOOST,
            cost = 200,
            rarity = Rarity.UNCOMMON,
            icon = "📚",
            previewColor = "#32CD32"
        ),
        ShopItem(
            id = "boost_discount_20",
            name = "Sweet Deal",
            description = "20% off all shop items for 24 hours",
            type = ShopItemType.BOOST,
            cost = 250,
            rarity = Rarity.RARE,
            icon = "🏷️",
            previewColor = "#FF69B4"
        ),
        ShopItem(
            id = "boost_vip_access",
            name = "VIP Access",
            description = "Access to exclusive items for 7 days",
            type = ShopItemType.BOOST,
            cost = 500,
            rarity = Rarity.EPIC,
            icon = "🎫",
            previewColor = "#FFD700",
            requirements = ShopRequirements(minLevel = 10)
        ),
        ShopItem(
            id = "boost_weekend_warrior",
            name = "Weekend Warrior",
            description = "All boosts active for 48 hours",
            type = ShopItemType.BOOST,
            cost = 800,
            rarity = Rarity.LEGENDARY,
            icon = "🔥",
            previewColor = "#FF1493",
            availability = ShopItemAvailability.LIMITED_TIME
        )
    )

    // ═════════════════════════════════════════════════════════════
    // FEATURE UNLOCKS (4 items)
    // ═════════════════════════════════════════════════════════════
    val FEATURES = listOf(
        ShopItem(
            id = "feature_custom_themes",
            name = "Theme Creator",
            description = "Create your own custom themes",
            type = ShopItemType.FEATURE,
            cost = 1000,
            rarity = Rarity.EPIC,
            icon = "🎨",
            previewColor = "#FF69B4",
            requirements = ShopRequirements(minLevel = 10, achievementCount = 15)
        ),
        ShopItem(
            id = "feature_effect_mixer",
            name = "Effect Mixer",
            description = "Combine multiple effects together",
            type = ShopItemType.FEATURE,
            cost = 800,
            rarity = Rarity.RARE,
            icon = "🎛️",
            previewColor = "#9370DB",
            requirements = ShopRequirements(minLevel = 8)
        ),
        ShopItem(
            id = "feature_priority_downloads",
            name = "Priority Queue",
            description = "Skip the download queue",
            type = ShopItemType.FEATURE,
            cost = 600,
            rarity = Rarity.RARE,
            icon = "⏩",
            previewColor = "#00CED1",
            requirements = ShopRequirements(minLevel = 5)
        ),
        ShopItem(
            id = "feature_beta_access",
            name = "Beta Tester",
            description = "Early access to new apps",
            type = ShopItemType.FEATURE,
            cost = 500,
            rarity = Rarity.UNCOMMON,
            icon = "🧪",
            previewColor = "#32CD32",
            requirements = ShopRequirements(totalInstalls = 10)
        )
    )

    // ═════════════════════════════════════════════════════════════
    // BUNDLES (4 items)
    // ═════════════════════════════════════════════════════════════
    val BUNDLES = listOf(
        ShopItem(
            id = "bundle_starter",
            name = "Starter Pack",
            description = "Perfect for new users!",
            type = ShopItemType.BUNDLE,
            cost = 200,
            rarity = Rarity.UNCOMMON,
            icon = "🎁",
            previewColor = "#FF69B4",
            bundleItems = listOf("icon_lollipop", "boost_sugar_x2", "badge_sugar_newbie"),
            isNew = true
        ),
        ShopItem(
            id = "bundle_theme_lover",
            name = "Theme Enthusiast",
            description = "3 exclusive themes + badge",
            type = ShopItemType.BUNDLE,
            cost = 1200,
            rarity = Rarity.RARE,
            icon = "🎨",
            previewColor = "#9370DB",
            bundleItems = listOf("theme_retro_arcade", "theme_midnight_garden", "theme_neon_cyber", "badge_candy_crusher"),
            discount = 25
        ),
        ShopItem(
            id = "bundle_effect_master",
            name = "Effect Master",
            description = "All premium effects + mixer",
            type = ShopItemType.BUNDLE,
            cost = 2000,
            rarity = Rarity.EPIC,
            icon = "✨",
            previewColor = "#FFD700",
            bundleItems = listOf(
                "effect_dragon_breath", "effect_stardust", "effect_bubbles",
                "effect_lightning", "effect_matrix", "feature_effect_mixer"
            ),
            discount = 30,
            requirements = ShopRequirements(minLevel = 5)
        ),
        ShopItem(
            id = "bundle_ultimate",
            name = "Ultimate Bundle",
            description = "Everything you need!",
            type = ShopItemType.BUNDLE,
            cost = 5000,
            rarity = Rarity.LEGENDARY,
            icon = "💎",
            previewColor = "#FF1493",
            bundleItems = listOf(
                "theme_golden_candy", "theme_diamond_dust", "effect_galaxy_spiral",
                "badge_legendary", "icon_crown", "boost_weekend_warrior",
                "feature_custom_themes", "feature_priority_downloads"
            ),
            discount = 40,
            requirements = ShopRequirements(minLevel = 15, achievementCount = 25),
            availability = ShopItemAvailability.LIMITED_TIME
        )
    )

    // Get all items
    fun getAllItems(): List<ShopItem> {
        return EXCLUSIVE_THEMES + PREMIUM_EFFECTS + PROFILE_BADGES + 
               APP_ICONS + BOOSTS + FEATURES + BUNDLES
    }

    // Get items by type
    fun getItemsByType(type: ShopItemType): List<ShopItem> {
        return getAllItems().filter { it.type == type }
    }

    // Get item by ID
    fun getItemById(id: String): ShopItem? {
        return getAllItems().find { it.id == id }
    }

    // Get featured items (random selection)
    fun getFeaturedItems(count: Int = 4): List<ShopItem> {
        return getAllItems()
            .filter { it.rarity.ordinal >= Rarity.RARE.ordinal }
            .shuffled()
            .take(count)
    }

    // Get new items
    fun getNewItems(): List<ShopItem> {
        return getAllItems().filter { it.isNew }
    }

    // Calculate bundle value
    fun calculateBundleValue(bundle: ShopItem): Int {
        if (bundle.bundleItems == null) return 0
        return bundle.bundleItems.mapNotNull { getItemById(it)?.cost }.sum()
    }
}
