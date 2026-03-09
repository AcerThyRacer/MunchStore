package com.sugarmunch.app.events

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.Month

/**
 * SugarMunch Seasonal Events System - Event Definitions
 * 
 * Events are time-limited celebrations with exclusive content:
 * - Special themes and effects
 * - Event challenges with unique rewards
 * - Limited-time currency and shop items
 * - Community leaderboards
 */

enum class EventType {
    SEASONAL,      // Recurring yearly events (Halloween, Winter, etc.)
    SPECIAL,       // One-time special events
    ANNIVERSARY,   // App birthday celebration
    COMMUNITY,     // Community-driven events
    COLLABORATION  // Partner collaborations
}

enum class EventStatus {
    UPCOMING,      // Event scheduled but not started
    ACTIVE,        // Event currently running
    ENDING_SOON,   // Less than 24 hours remaining
    ENDED          // Event concluded
}

sealed class EventRewardType {
    data class ThemeReward(val themeId: String, val themeName: String) : EventRewardType()
    data class EffectReward(val effectId: String, val effectName: String) : EventRewardType()
    data class BadgeReward(val badgeId: String, val badgeName: String, val badgeIcon: String) : EventRewardType()
    data class CurrencyReward(val amount: Int) : EventRewardType()
    data class ShopItemReward(val itemId: String, val itemName: String) : EventRewardType()
    data class TitleReward(val titleId: String, val titleText: String) : EventRewardType()
}

data class EventReward(
    val id: String,
    val name: String,
    val description: String,
    val type: EventRewardType,
    val icon: String,
    val requiredPoints: Int = 0,
    val isPremium: Boolean = false,
    val isSecret: Boolean = false
)

data class SeasonalEvent(
    val id: String,
    val name: String,
    val description: String,
    val shortDescription: String,
    val type: EventType,
    val startMonth: Month,
    val startDay: Int,
    val durationDays: Int,
    val theme: EventTheme,
    val lore: EventLore,
    val challenges: List<String> = emptyList(),
    val rewards: List<EventReward> = emptyList(),
    val accentColor: Color,
    val isYearly: Boolean = true,
    val yearOverride: Int? = null
) {
    fun getStartDate(currentYear: Int = LocalDate.now().year): LocalDate {
        val year = yearOverride ?: currentYear
        return LocalDate.of(year, startMonth, startDay)
    }
    
    fun getEndDate(currentYear: Int = LocalDate.now().year): LocalDate {
        return getStartDate(currentYear).plusDays(durationDays.toLong())
    }
    
    fun isActiveOn(date: LocalDate = LocalDate.now()): Boolean {
        val year = yearOverride ?: date.year
        val start = getStartDate(year)
        val end = getEndDate(year)
        return !date.isBefore(start) && !date.isAfter(end.minusDays(1))
    }
    
    fun getStatus(date: LocalDate = LocalDate.now()): EventStatus {
        val year = yearOverride ?: date.year
        val start = getStartDate(year)
        val end = getEndDate(year)
        
        return when {
            date.isBefore(start) -> EventStatus.UPCOMING
            date.isAfter(end.minusDays(1)) -> EventStatus.ENDED
            end.minusDays(1).isBefore(date.plusDays(1)) -> EventStatus.ENDING_SOON
            else -> EventStatus.ACTIVE
        }
    }
    
    fun daysUntilStart(date: LocalDate = LocalDate.now()): Long {
        val year = yearOverride ?: date.year
        val start = getStartDate(year)
        return if (date.isBefore(start)) {
            start.toEpochDay() - date.toEpochDay()
        } else if (isYearly) {
            getStartDate(year + 1).toEpochDay() - date.toEpochDay()
        } else {
            -1
        }
    }
    
    fun daysRemaining(date: LocalDate = LocalDate.now()): Long {
        val year = yearOverride ?: date.year
        val end = getEndDate(year)
        return if (!date.isAfter(end.minusDays(1))) {
            end.toEpochDay() - date.toEpochDay()
        } else {
            0
        }
    }
}

data class EventTheme(
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundGradient: List<Color>,
    val particleType: EventParticleType,
    val particleColors: List<Color>,
    val overlayEffect: OverlayEffect? = null
)

data class EventLore(
    val story: String,
    val intro: String,
    val conclusion: String,
    val characters: List<EventCharacter> = emptyList()
)

data class EventCharacter(
    val name: String,
    val emoji: String,
    val description: String,
    val role: String
)

enum class EventParticleType {
    FLOATING_CANDIES,
    FALLING_LEAVES,
    SNOWFLAKES,
    HEARTS,
    FLOWER_PETALS,
    SUN_RAYS,
    GHOSTS,
    PUMPKINS,
    ORNAMENTS,
    CONFETTI
}

enum class OverlayEffect {
    SPOOKY_MIST,
    WINTER_FROST,
    LOVE_GLOW,
    SPRING_BLOOM,
    SUMMER_HEAT,
    ANNIVERSARY_SPARKLE
}

object SeasonalEvents {
    
    val HALLOWEEN = SeasonalEvent(
        id = "halloween_spooktacular",
        name = "Candy Spooktacular",
        description = "The veil between worlds grows thin, and the Candy Kingdom becomes haunted! Ghosts, goblins, and ghoulish treats await in this spine-tingling celebration.",
        shortDescription = "Spooky candy adventures await!",
        type = EventType.SEASONAL,
        startMonth = Month.OCTOBER,
        startDay = 25,
        durationDays = 7,
        theme = EventTheme(
            primaryColor = Color(0xFFFF6B35),
            secondaryColor = Color(0xFF8B4513),
            backgroundGradient = listOf(Color(0xFF2D1B2E), Color(0xFF1A0F1A), Color(0xFF0D080D)),
            particleType = EventParticleType.GHOSTS,
            particleColors = listOf(Color(0xFFFF6B35), Color(0xFF9C27B0), Color(0xFF00BCD4)),
            overlayEffect = OverlayEffect.SPOOKY_MIST
        ),
        lore = EventLore(
            story = "Every October, the ancient Candy Spirits awaken from their sugary slumber. They bring with them phantom flavors and spectral sweets that can only be tasted during this haunted week.",
            intro = "The spirits are stirring... Are you brave enough to collect their ghostly treats?",
            conclusion = "The spirits return to rest, but they'll be back next year with even spookier surprises!",
            characters = listOf(
                EventCharacter("Phantom Frost", "👻", "A friendly ghost who loves peppermint", "Guide"),
                EventCharacter("Count Chocolate", "🧛", "The ruler of the Candy Crypt", "Boss"),
                EventCharacter("Witch Hazel", "🧙", "Brews magical candy potions", "Merchant")
            )
        ),
        challenges = listOf(
            "halloween_install_5",
            "halloween_spooky_theme",
            "halloween_night_install",
            "halloween_candy_share",
            "halloween_ghost_hunt"
        ),
        rewards = listOf(
            EventReward(
                id = "halloween_theme",
                name = "Spooky Candy",
                description = "A hauntingly beautiful dark theme",
                type = EventRewardType.ThemeReward("spooky_candy", "Spooky Candy"),
                icon = "🎃",
                requiredPoints = 100
            ),
            EventReward(
                id = "ghost_effect",
                name = "Ghostly Presence",
                description = "Ethereal ghost particles follow your taps",
                type = EventRewardType.EffectReward("ghost_particles", "Ghostly Presence"),
                icon = "👻",
                requiredPoints = 250
            ),
            EventReward(
                id = "halloween_badge",
                name = "Spirit Walker",
                description = "Proof you survived the Candy Spooktacular",
                type = EventRewardType.BadgeReward("spirit_walker", "Spirit Walker", "🎖️"),
                icon = "🎖️",
                requiredPoints = 500
            ),
            EventReward(
                id = "halloween_title",
                name = "Candy Ghoul",
                description = "Exclusive Halloween title",
                type = EventRewardType.TitleReward("candy_ghoul", "Candy Ghoul"),
                icon = "🏷️",
                requiredPoints = 750,
                isSecret = true
            )
        ),
        accentColor = Color(0xFFFF6B35)
    )
    
    val WINTER_WONDERLAND = SeasonalEvent(
        id = "winter_wonderland",
        name = "Winter Wonderland",
        description = "The Candy Kingdom transforms into a frozen paradise! Snow falls on gingerbread houses and candy cane forests in this magical winter celebration.",
        shortDescription = "Experience the frosty magic!",
        type = EventType.SEASONAL,
        startMonth = Month.DECEMBER,
        startDay = 20,
        durationDays = 14,
        theme = EventTheme(
            primaryColor = Color(0xFFB5DEFF),
            secondaryColor = Color(0xFFFFFFFF),
            backgroundGradient = listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB), Color(0xFF90CAF9)),
            particleType = EventParticleType.SNOWFLAKES,
            particleColors = listOf(Color.White, Color(0xFFB5DEFF), Color(0xFFE1F5FE)),
            overlayEffect = OverlayEffect.WINTER_FROST
        ),
        lore = EventLore(
            story = "As winter's chill embraces the kingdom, the Frost Sugar Fairies awaken. They weave magical snow from crystallized sugar, creating a wonderland of frozen treats.",
            intro = "Winter has arrived! Join the Frost Fairies in their frozen celebration!",
            conclusion = "As the snow melts, the fairies return to their slumber. Thank you for celebrating with us!",
            characters = listOf(
                EventCharacter("Frostine", "❄️", "The Sugarplum Fairy of Winter", "Guide"),
                EventCharacter("Gingerbread King", "🎄", "Ruler of the Cookie Castle", "Boss"),
                EventCharacter("Peppermint Penguin", "🐧", "Loves sliding on ice candy", "Companion")
            )
        ),
        challenges = listOf(
            "winter_daily_login",
            "winter_frozen_theme",
            "winter_share_warmth",
            "winter_snowflake_collect",
            "winter_gift_giver"
        ),
        rewards = listOf(
            EventReward(
                id = "winter_theme",
                name = "Frosted Candy",
                description = "Cool blue winter wonderland theme",
                type = EventRewardType.ThemeReward("frosted_candy", "Frosted Candy"),
                icon = "❄️",
                requiredPoints = 100
            ),
            EventReward(
                id = "snow_effect",
                name = "Blizzard Rush",
                description = "Gentle snow falls as you browse",
                type = EventRewardType.EffectReward("snowfall", "Blizzard Rush"),
                icon = "🌨️",
                requiredPoints = 250
            ),
            EventReward(
                id = "winter_badge",
                name = "Ice Master",
                description = "Conquered the Winter Wonderland",
                type = EventRewardType.BadgeReward("ice_master", "Ice Master", "🧊"),
                icon = "🧊",
                requiredPoints = 500
            ),
            EventReward(
                id = "winter_currency",
                name = "Sugar Gems",
                description = "500 bonus Sugar Gems",
                type = EventRewardType.CurrencyReward(500),
                icon = "💎",
                requiredPoints = 300
            )
        ),
        accentColor = Color(0xFFB5DEFF)
    )
    
    val VALENTINES_SWEETHEART = SeasonalEvent(
        id = "valentines_sweetheart",
        name = "Valentine's Sweetheart",
        description = "Love is in the air! The Candy Kingdom blooms with heart-shaped chocolates, rose-flavored treats, and endless romance.",
        shortDescription = "Share the love with sweet treats!",
        type = EventType.SEASONAL,
        startMonth = Month.FEBRUARY,
        startDay = 10,
        durationDays = 7,
        theme = EventTheme(
            primaryColor = Color(0xFFFF1744),
            secondaryColor = Color(0xFFFF8A80),
            backgroundGradient = listOf(Color(0xFFFFEBEE), Color(0xFFFCE4EC), Color(0xFFF8BBD9)),
            particleType = EventParticleType.HEARTS,
            particleColors = listOf(Color(0xFFFF1744), Color(0xFFE91E63), Color(0xFFF48FB1)),
            overlayEffect = OverlayEffect.LOVE_GLOW
        ),
        lore = EventLore(
            story = "Cupid has visited the Candy Kingdom, leaving trails of heart-shaped sugar crystals. The Love Fairies spread romance through every lollipop and chocolate truffle.",
            intro = "Love is sweet! Help spread joy throughout the kingdom!",
            conclusion = "The love you shared will bloom in hearts all year round!",
            characters = listOf(
                EventCharacter("Candy Cupid", "💘", "Spreads love with sugar arrows", "Guide"),
                EventCharacter("Rose Cream", "🌹", "Creates rose-flavored delights", "Ally"),
                EventCharacter("Choco-Heart", "🍫", "A chocolate heart come to life", "Companion")
            )
        ),
        challenges = listOf(
            "valentine_share_love",
            "valentine_romantic_theme",
            "valentine_chocolate_collect",
            "valentine_friend_bonus",
            "valentine_daily_gift"
        ),
        rewards = listOf(
            EventReward(
                id = "valentine_theme",
                name = "Sweetheart",
                description = "Romantic pink and red theme",
                type = EventRewardType.ThemeReward("sweetheart", "Sweetheart"),
                icon = "💝",
                requiredPoints = 100
            ),
            EventReward(
                id = "heart_effect",
                name = "Love Burst",
                description = "Hearts burst on every interaction",
                type = EventRewardType.EffectReward("love_burst", "Love Burst"),
                icon = "💖",
                requiredPoints = 250
            ),
            EventReward(
                id = "valentine_badge",
                name = "Sweetheart",
                description = "A true romantic at heart",
                type = EventRewardType.BadgeReward("sweetheart_badge", "Sweetheart", "💕"),
                icon = "💕",
                requiredPoints = 400
            )
        ),
        accentColor = Color(0xFFFF1744)
    )
    
    val SPRING_BLOSSOM = SeasonalEvent(
        id = "spring_blossom",
        name = "Spring Blossom",
        description = "The frost melts away as the Candy Garden awakens! Flower-shaped candies bloom, and the air fills with the scent of fresh sugar blossoms.",
        shortDescription = "Watch the candy garden bloom!",
        type = EventType.SEASONAL,
        startMonth = Month.APRIL,
        startDay = 1,
        durationDays = 14,
        theme = EventTheme(
            primaryColor = Color(0xFF81C784),
            secondaryColor = Color(0xFFF8BBD0),
            backgroundGradient = listOf(Color(0xFFE8F5E9), Color(0xFFF1F8E9), Color(0xFFF9FBE7)),
            particleType = EventParticleType.FLOWER_PETALS,
            particleColors = listOf(Color(0xFFF8BBD0), Color(0xFFE1BEE7), Color(0xFFFFCCBC)),
            overlayEffect = OverlayEffect.SPRING_BLOOM
        ),
        lore = EventLore(
            story = "The ancient Sugar Tree blooms once more, its branches heavy with candy flowers. The Blossom Sprites dance from petal to petal, awakening the kingdom from winter's sleep.",
            intro = "Spring has sprung! Join the Blossom Festival and watch nature's candy come alive!",
            conclusion = "The blossoms may fade, but their sweetness lingers in every heart!",
            characters = listOf(
                EventCharacter("Petal", "🌸", "A cheerful cherry blossom sprite", "Guide"),
                EventCharacter("Honey Bee", "🐝", "Collects nectar from candy flowers", "Helper"),
                EventCharacter("Spring Bunny", "🐰", "Hides candy eggs throughout the kingdom", "Trickster")
            )
        ),
        challenges = listOf(
            "spring_daily_bloom",
            "spring_garden_theme",
            "spring_bunny_hunt",
            "spring_pollen_collect",
            "spring_friendship_grow"
        ),
        rewards = listOf(
            EventReward(
                id = "spring_theme",
                name = "Blossom",
                description = "Fresh spring garden theme",
                type = EventRewardType.ThemeReward("blossom", "Blossom"),
                icon = "🌸",
                requiredPoints = 100
            ),
            EventReward(
                id = "petal_effect",
                name = "Petal Dance",
                description = "Flower petals drift across your screen",
                type = EventRewardType.EffectReward("petal_dance", "Petal Dance"),
                icon = "🌺",
                requiredPoints = 250
            ),
            EventReward(
                id = "spring_badge",
                name = "Garden Keeper",
                description = "Nurtured the Candy Garden to full bloom",
                type = EventRewardType.BadgeReward("garden_keeper", "Garden Keeper", "🌷"),
                icon = "🌷",
                requiredPoints = 500
            )
        ),
        accentColor = Color(0xFF81C784)
    )
    
    val SUMMER_SPLASH = SeasonalEvent(
        id = "summer_splash",
        name = "Summer Splash",
        description = "The Candy Beach opens for the season! Cool off with frozen treats, splash in the soda waves, and enjoy the tropical sugar paradise.",
        shortDescription = "Dive into summer fun!",
        type = EventType.SEASONAL,
        startMonth = Month.JULY,
        startDay = 1,
        durationDays = 21,
        theme = EventTheme(
            primaryColor = Color(0xFF29B6F6),
            secondaryColor = Color(0xFFFFF176),
            backgroundGradient = listOf(Color(0xFFE1F5FE), Color(0xFFB3E5FC), Color(0xFF81D4FA)),
            particleType = EventParticleType.SUN_RAYS,
            particleColors = listOf(Color(0xFFFFF176), Color(0xFFFFEE58), Color(0xFFFFFF8D)),
            overlayEffect = OverlayEffect.SUMMER_HEAT
        ),
        lore = EventLore(
            story = "The Great Soda Ocean warms under the summer sun. Tropical fruit candies ripen on the beach, and the Melted Ice Cream River flows freely. It's time for the grand beach party!",
            intro = "Surf's up! Grab your sunscreen and join the sweetest beach party ever!",
            conclusion = "The sun sets on another amazing summer. Until next year, keep the beach vibes alive!",
            characters = listOf(
                EventCharacter("Sunny", "☀️", "The cheerful sun spirit", "Guide"),
                EventCharacter("Popsicle Penguin", "🍦", "Loves to surf the cream waves", "Surfer"),
                EventCharacter("Tropical Tina", "🌺", "Makes the best fruit candies", "Vendor")
            )
        ),
        challenges = listOf(
            "summer_beach_theme",
            "summer_splash_party",
            "summer_ice_cream_collect",
            "summer_tropical_discover",
            "summer_streak_heat"
        ),
        rewards = listOf(
            EventReward(
                id = "summer_theme",
                name = "Tropical Candy",
                description = "Bright tropical beach theme",
                type = EventRewardType.ThemeReward("tropical_candy", "Tropical Candy"),
                icon = "🏖️",
                requiredPoints = 100
            ),
            EventReward(
                id = "wave_effect",
                name = "Ocean Breeze",
                description = "Gentle waves ripple across your screen",
                type = EventRewardType.EffectReward("ocean_breeze", "Ocean Breeze"),
                icon = "🌊",
                requiredPoints = 250
            ),
            EventReward(
                id = "summer_badge",
                name = "Beach Boss",
                description = "Ruled the summer waves",
                type = EventRewardType.BadgeReward("beach_boss", "Beach Boss", "🏄"),
                icon = "🏄",
                requiredPoints = 500
            )
        ),
        accentColor = Color(0xFF29B6F6)
    )
    
    fun getAnniversaryEvent(appBirthday: LocalDate): SeasonalEvent {
        val yearsActive = LocalDate.now().year - appBirthday.year
        return SeasonalEvent(
            id = "anniversary_${yearsActive}",
            name = "${yearsActive}th Anniversary Celebration",
            description = "Join us in celebrating ${yearsActive} sweet years of SugarMunch! Special rewards, exclusive content, and a massive thank you to our amazing community!",
            shortDescription = "Celebrating ${yearsActive} sweet years!",
            type = EventType.ANNIVERSARY,
            startMonth = appBirthday.month,
            startDay = appBirthday.dayOfMonth,
            durationDays = 7,
            theme = EventTheme(
                primaryColor = Color(0xFFFFD700),
                secondaryColor = Color(0xFFFF6B6B),
                backgroundGradient = listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3), Color(0xFFFFE082)),
                particleType = EventParticleType.CONFETTI,
                particleColors = listOf(Color(0xFFFFD700), Color(0xFFFF6B6B), Color(0xFF4ECDC4)),
                overlayEffect = OverlayEffect.ANNIVERSARY_SPARKLE
            ),
            lore = EventLore(
                story = "On this day, ${yearsActive} years ago, the first candy was unwrapped in our kingdom. Today, we celebrate every moment, every friend, and every sweet memory we've shared together.",
                intro = "Happy Anniversary! Let's celebrate our sweet journey together!",
                conclusion = "Thank you for ${yearsActive} amazing years! Here's to many more!",
                characters = listOf(
                    EventCharacter("Birthday Cake", "🎂", "The celebration master", "Host"),
                    EventCharacter("Party Popper", "🎉", "Brings the celebration energy", "Mascot"),
                    EventCharacter("Sugar King", "👑", "The founder of Candy Kingdom", "Special Guest")
                )
            ),
            challenges = listOf(
                "anni_party_theme",
                "anni_share_memory",
                "anni_invite_friend",
                "anni_collect_all",
                "anni_special_wish"
            ),
            rewards = listOf(
                EventReward(
                    id = "anniversary_theme",
                    name = "Celebration",
                    description = "Festive gold and rainbow theme",
                    type = EventRewardType.ThemeReward("celebration", "Celebration"),
                    icon = "🎊",
                    requiredPoints = 100
                ),
                EventReward(
                    id = "confetti_effect",
                    name = "Party Time",
                    description = "Confetti explosion on every action",
                    type = EventRewardType.EffectReward("party_time", "Party Time"),
                    icon = "🎊",
                    requiredPoints = 200
                ),
                EventReward(
                    id = "anniversary_badge",
                    name = "Anniversary VIP",
                    description = "Celebrated with us since the beginning",
                    type = EventRewardType.BadgeReward("anni_vip", "Anniversary VIP", "🎖️"),
                    icon = "🎖️",
                    requiredPoints = 400
                ),
                EventReward(
                    id = "anniversary_currency",
                    name = "Birthday Bonus",
                    description = "1000 Sugar Gems bonus!",
                    type = EventRewardType.CurrencyReward(1000),
                    icon = "💎",
                    requiredPoints = 300
                )
            ),
            accentColor = Color(0xFFFFD700),
            yearOverride = LocalDate.now().year
        )
    }
    
    val ALL_EVENTS: List<SeasonalEvent> by lazy {
        listOf(HALLOWEEN, WINTER_WONDERLAND, VALENTINES_SWEETHEART, SPRING_BLOSSOM, SUMMER_SPLASH)
    }
    
    fun getActiveEvents(date: LocalDate = LocalDate.now()): List<SeasonalEvent> {
        return ALL_EVENTS.filter { it.isActiveOn(date) }
    }
    
    fun getUpcomingEvents(date: LocalDate = LocalDate.now(), daysAhead: Long = 30): List<SeasonalEvent> {
        return ALL_EVENTS.filter { event ->
            val daysUntil = event.daysUntilStart(date)
            daysUntil in 0..daysAhead && !event.isActiveOn(date)
        }.sortedBy { it.daysUntilStart(date) }
    }
    
    fun getEventById(id: String): SeasonalEvent? {
        return ALL_EVENTS.find { it.id == id }
    }
    
    fun getEventForDate(date: LocalDate): SeasonalEvent? {
        return ALL_EVENTS.find { it.isActiveOn(date) }
    }
}
