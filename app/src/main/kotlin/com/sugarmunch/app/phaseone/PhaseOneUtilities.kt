package com.sugarmunch.app.phaseone

import androidx.compose.ui.graphics.Color
import com.sugarmunch.app.data.AppEntry
import com.sugarmunch.app.data.CandyTrailEntry

data class UtilityFeatureHighlight(
    val title: String,
    val description: String,
    val iconName: String
)

data class UtilityModePreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val recommendedThemeId: String
)

data class UtilityCustomization(
    val selectedModeId: String,
    val visualIntensity: Int = 88,
    val ribbonsEnabled: Boolean = true,
    val trailBadgesEnabled: Boolean = true,
    val autoThemeSync: Boolean = true
)

data class PhaseOneUtilitySpec(
    val fallbackApp: AppEntry,
    val emojiIcon: String,
    val shortTagline: String,
    val heroColors: List<Color>,
    val featureHighlights: List<UtilityFeatureHighlight>,
    val customizationModes: List<UtilityModePreset>,
    val recommendedThemeIds: List<String>,
    val studioTitle: String,
    val supportsNearbyShare: Boolean = false
) {
    val appId: String get() = fallbackApp.id

    fun defaultCustomization(): UtilityCustomization = UtilityCustomization(
        selectedModeId = customizationModes.first().id
    )

    fun currentMode(customization: UtilityCustomization): UtilityModePreset =
        customizationModes.find { it.id == customization.selectedModeId } ?: customizationModes.first()
}

object PhaseOneUtilities {
    private const val PREVIEW_ROOT = "file:///android_asset/previews"

    val SUGAR_FILES = PhaseOneUtilitySpec(
        fallbackApp = AppEntry(
            id = "sugarfiles",
            name = "SugarFiles",
            packageName = "com.sugarmunch.sugarfiles",
            description = "A candy-coated file vault with themed folders, stash badges, and fast trail drops.",
            downloadUrl = "https://github.com/your-username/SugarMunch/releases/download/sugarfiles-v1.0.0/sugarfiles.apk",
            version = "1.0.0",
            source = "Inspired by Material Files: https://github.com/zhanghai/MaterialFiles",
            category = "Utilities",
            accentColor = "#6C63FF",
            badge = "Phase 1",
            featured = true,
            sortOrder = 0,
            previewUrl = "$PREVIEW_ROOT/sugarfiles.html",
            components = listOf("file-manager", "vault", "trail-drops")
        ),
        emojiIcon = "\uD83D\uDCC2",
        shortTagline = "Private file vault",
        heroColors = listOf(
            Color(0xFF6C63FF),
            Color(0xFF3B82F6),
            Color(0xFF7DD3FC)
        ),
        featureHighlights = listOf(
            UtilityFeatureHighlight(
                title = "Vault grids",
                description = "Show folders as glossy candy stacks with smart badge highlights.",
                iconName = "folder"
            ),
            UtilityFeatureHighlight(
                title = "Trail drops",
                description = "Pin favorite paths to Candy Trails for one-tap launches later.",
                iconName = "star"
            ),
            UtilityFeatureHighlight(
                title = "Glow sync",
                description = "Match vault colors to your active SugarMunch theme in one tap.",
                iconName = "palette"
            )
        ),
        customizationModes = listOf(
            UtilityModePreset(
                id = "vault-glow",
                title = "Vault Glow",
                subtitle = "Glossy folders and rich neon edges",
                description = "Turns every folder into a glowing candy shelf with bright vault labels.",
                recommendedThemeId = "sugar_files_glow"
            ),
            UtilityModePreset(
                id = "sherbet-stacks",
                title = "Sherbet Stacks",
                subtitle = "Soft cards, stacked chips, and calm labels",
                description = "A softer layout for browsing screenshots, downloads, and stash folders.",
                recommendedThemeId = "sunrise_sherbet"
            ),
            UtilityModePreset(
                id = "frost-labels",
                title = "Frost Labels",
                subtitle = "Minimal chrome with cool mint accents",
                description = "Cuts visual noise while keeping badges and candy-glass depth.",
                recommendedThemeId = "midnight_mint"
            )
        ),
        recommendedThemeIds = listOf(
            "sugar_files_glow",
            "sunrise_sherbet",
            "midnight_mint"
        ),
        studioTitle = "Vault Studio"
    )

    val TAFFY_SEND = PhaseOneUtilitySpec(
        fallbackApp = AppEntry(
            id = "taffysend",
            name = "TaffySend",
            packageName = "com.sugarmunch.taffysend",
            description = "Stretchy local sharing with candy beams, QR pairing, and quick nearby drops.",
            downloadUrl = "https://github.com/your-username/SugarMunch/releases/download/taffysend-v1.0.0/taffysend.apk",
            version = "1.0.0",
            source = "Inspired by LocalSend: https://github.com/localsend/localsend",
            category = "Utilities",
            accentColor = "#FF8A65",
            badge = "Fresh",
            featured = true,
            sortOrder = 1,
            previewUrl = "$PREVIEW_ROOT/taffysend.html",
            components = listOf("local-sharing", "nearby", "qr-pairing")
        ),
        emojiIcon = "\uD83D\uDCF6",
        shortTagline = "Sweet local sharing",
        heroColors = listOf(
            Color(0xFFFF8A65),
            Color(0xFFFF4D8D),
            Color(0xFFFFC857)
        ),
        featureHighlights = listOf(
            UtilityFeatureHighlight(
                title = "Candy beams",
                description = "Show active transfers as thick ribbons with pulse and glow controls.",
                iconName = "auto_awesome"
            ),
            UtilityFeatureHighlight(
                title = "QR pairing",
                description = "Pair devices with bright burst previews and shortcut-friendly flows.",
                iconName = "qr"
            ),
            UtilityFeatureHighlight(
                title = "Nearby drops",
                description = "Jump into SugarMunch's existing share lab whenever you want to blast files.",
                iconName = "wifi"
            )
        ),
        customizationModes = listOf(
            UtilityModePreset(
                id = "ribbon-burst",
                title = "Ribbon Burst",
                subtitle = "Loud transfer lines and party-mode pulses",
                description = "High-energy sharing with bold ribbons, pop badges, and quick-launch action.",
                recommendedThemeId = "taffy_transfer_rush"
            ),
            UtilityModePreset(
                id = "soft-pulse",
                title = "Soft Pulse",
                subtitle = "Warm gradients and calmer pair prompts",
                description = "Smoothes out the motion while keeping the transfer card bright and playful.",
                recommendedThemeId = "cotton_candy"
            ),
            UtilityModePreset(
                id = "radar-arc",
                title = "Radar Arc",
                subtitle = "Wave scans with candy radar sweeps",
                description = "Highlights device discovery and pairing with circular scan accents.",
                recommendedThemeId = "trippy_galaxy"
            )
        ),
        recommendedThemeIds = listOf(
            "taffy_transfer_rush",
            "cotton_candy",
            "trippy_galaxy"
        ),
        studioTitle = "Transfer Studio",
        supportsNearbyShare = true
    )

    val utilityTrail = CandyTrailEntry(
        id = "utility-ribbon",
        name = "Utility Ribbon",
        description = "Vaults, drops, and sugar-fast helper apps.",
        icon = "\uD83C\uDF80",
        appIds = listOf(SUGAR_FILES.appId, TAFFY_SEND.appId)
    )

    val allSpecs: List<PhaseOneUtilitySpec> = listOf(SUGAR_FILES, TAFFY_SEND)

    private val specsById = allSpecs.associateBy { it.appId }

    fun specFor(appId: String): PhaseOneUtilitySpec? = specsById[appId]

    fun enrichApp(app: AppEntry): AppEntry {
        val spec = specFor(app.id) ?: return app
        val fallback = spec.fallbackApp
        return app.copy(
            description = app.description.ifBlank { fallback.description },
            iconUrl = app.iconUrl ?: fallback.iconUrl,
            downloadUrl = app.downloadUrl.ifBlank { fallback.downloadUrl },
            version = app.version.ifBlank { fallback.version },
            source = app.source ?: fallback.source,
            category = app.category ?: fallback.category,
            accentColor = app.accentColor ?: fallback.accentColor,
            badge = app.badge ?: fallback.badge,
            featured = app.featured ?: fallback.featured,
            sortOrder = app.sortOrder ?: fallback.sortOrder,
            previewUrl = app.previewUrl ?: fallback.previewUrl,
            components = app.components ?: fallback.components,
            trailerUrl = app.trailerUrl ?: fallback.trailerUrl
        )
    }

    fun mergeApps(apps: List<AppEntry>): List<AppEntry> {
        val merged = apps.associateBy { it.id }.toMutableMap()
        allSpecs.forEach { spec ->
            merged[spec.appId] = merged[spec.appId]?.let(::enrichApp) ?: spec.fallbackApp
        }
        return merged.values.sortedWith(
            compareBy<AppEntry> { it.category ?: "" }
                .thenBy { it.sortOrder ?: Int.MAX_VALUE }
                .thenBy { it.name }
        )
    }

    fun mergeTrails(existing: List<CandyTrailEntry>): List<CandyTrailEntry> {
        if (existing.any { it.id == utilityTrail.id }) return existing
        return existing + utilityTrail
    }
}

