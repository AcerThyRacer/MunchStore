package com.sugarmunch.app.ai

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.effects.v2.engine.EffectEngineV2
import com.sugarmunch.app.theme.engine.ThemeManager
import java.util.Calendar

class ContextAwareFabManager private constructor(
    private val context: Context
) {
    private val effectEngine = EffectEngineV2.getInstance(context)
    private val themeManager = ThemeManager.getInstance(context)

    private val _currentContext = kotlinx.coroutines.flow.MutableStateFlow(resolveContext())
    val currentContext: kotlinx.coroutines.flow.StateFlow<FabContext> = _currentContext

    private val _suggestedAction = kotlinx.coroutines.flow.MutableStateFlow<FabAction?>(suggestAction(_currentContext.value))
    val suggestedAction: kotlinx.coroutines.flow.StateFlow<FabAction?> = _suggestedAction

    private val _shouldShowFab = kotlinx.coroutines.flow.MutableStateFlow(true)
    val shouldShowFab: kotlinx.coroutines.flow.StateFlow<Boolean> = _shouldShowFab

    private val _predictedIntent = kotlinx.coroutines.flow.MutableStateFlow<UserIntent?>(intentFor(_currentContext.value))
    val predictedIntent: kotlinx.coroutines.flow.StateFlow<UserIntent?> = _predictedIntent

    suspend fun predictUserIntent(): UserIntent {
        refresh()
        return _predictedIntent.value ?: UserIntent.Exploring
    }

    fun executeAction(action: FabAction) {
        when (action) {
            is FabAction.ApplyTheme -> themeManager.setThemeById(action.themeId)
            is FabAction.FocusMode -> {
                themeManager.setThemeById(action.theme)
                action.effects.forEach { effectEngine.enableEffect(it, 0.7f) }
            }
            is FabAction.GamingMode -> {
                action.effects.forEach { effectEngine.enableEffect(it, 1f) }
            }
            is FabAction.ChillMode -> {
                action.effects.forEach { effectEngine.enableEffect(it, 0.5f) }
            }
            is FabAction.NightMode -> themeManager.setThemeById(action.theme)
            is FabAction.QuickInstall -> Unit
            is FabAction.UpdateDailyApp -> Unit
            is FabAction.Discover -> Unit
            is FabAction.SmartSuggestion -> Unit
        }
    }

    fun getContextualActions(): List<ContextualAction> {
        return when (_currentContext.value) {
            FabContext.MORNING -> listOf(
                ContextualAction("Morning Apps", Icons.Default.WbSunny),
                ContextualAction("Fresh Theme", Icons.Default.Palette)
            )
            FabContext.WORK -> listOf(
                ContextualAction("Focus Mode", Icons.Default.CenterFocusStrong),
                ContextualAction("Quiet Theme", Icons.Default.Palette)
            )
            FabContext.EVENING -> listOf(
                ContextualAction("Chill Mode", Icons.Default.NightsStay),
                ContextualAction("Discover", Icons.Default.Explore)
            )
            FabContext.GAMING -> listOf(
                ContextualAction("Gaming Mode", Icons.Default.SportsEsports),
                ContextualAction("Turbo Theme", Icons.Default.AutoAwesome)
            )
            FabContext.WEEKEND -> listOf(
                ContextualAction("Discover", Icons.Default.Explore),
                ContextualAction("Weekend Theme", Icons.Default.Palette)
            )
            FabContext.LATE_NIGHT -> listOf(
                ContextualAction("Night Mode", Icons.Default.Bedtime),
                ContextualAction("Soft Glow", Icons.Default.AutoAwesome)
            )
            FabContext.GENERAL -> listOf(
                ContextualAction("Quick Pick", Icons.Default.AutoAwesome),
                ContextualAction("Explore", Icons.Default.Explore)
            )
        }
    }

    private fun refresh() {
        val context = resolveContext()
        _currentContext.value = context
        _suggestedAction.value = suggestAction(context)
        _predictedIntent.value = intentFor(context)
    }

    private fun resolveContext(): FabContext {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        return when {
            hour in 6..10 -> FabContext.MORNING
            hour in 11..17 && day !in listOf(Calendar.SATURDAY, Calendar.SUNDAY) -> FabContext.WORK
            hour in 18..20 -> FabContext.EVENING
            hour in 21..23 -> FabContext.GAMING
            hour in 0..5 -> FabContext.LATE_NIGHT
            day in listOf(Calendar.SATURDAY, Calendar.SUNDAY) -> FabContext.WEEKEND
            else -> FabContext.GENERAL
        }
    }

    private fun suggestAction(context: FabContext): FabAction {
        return when (context) {
            FabContext.MORNING -> FabAction.QuickInstall(
                label = "Morning Apps",
                description = "Open the apps that kick off your day.",
                icon = Icons.Default.WbSunny,
                targetApps = emptyList()
            )
            FabContext.WORK -> FabAction.FocusMode(
                label = "Focus Mode",
                description = "Dim the noise and keep the essentials.",
                icon = Icons.Default.CenterFocusStrong,
                effects = emptyList(),
                theme = "focus_minimal"
            )
            FabContext.EVENING -> FabAction.ChillMode(
                label = "Chill Mode",
                description = "Softer visuals for winding down.",
                icon = Icons.Default.NightsStay,
                suggestedApps = emptyList(),
                effects = emptyList()
            )
            FabContext.GAMING -> FabAction.GamingMode(
                label = "Gaming Mode",
                description = "Bright, high-energy visuals.",
                icon = Icons.Default.SportsEsports,
                effects = emptyList(),
                performanceProfile = PerformanceProfile.HIGH
            )
            FabContext.WEEKEND -> FabAction.Discover(
                label = "Discover",
                description = "Browse something new this weekend.",
                icon = Icons.Default.Explore,
                featuredApps = emptyList()
            )
            FabContext.LATE_NIGHT -> FabAction.NightMode(
                label = "Night Mode",
                description = "Reduce glare for late sessions.",
                icon = Icons.Default.Bedtime,
                enableBlueLightFilter = true,
                theme = "midnight_dark"
            )
            FabContext.GENERAL -> FabAction.SmartSuggestion(
                label = "Quick Action",
                description = "A small shortcut based on the current moment.",
                icon = Icons.Default.AutoAwesome,
                suggestedCategory = null
            )
        }
    }

    private fun intentFor(context: FabContext): UserIntent {
        return when (context) {
            FabContext.MORNING -> UserIntent.StartingDay
            FabContext.EVENING -> UserIntent.WindingDown
            FabContext.GAMING -> UserIntent.Entertainment
            else -> UserIntent.Exploring
        }
    }

    companion object {
        @Volatile
        private var instance: ContextAwareFabManager? = null

        fun getInstance(context: Context): ContextAwareFabManager {
            return instance ?: synchronized(this) {
                instance ?: ContextAwareFabManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

enum class FabContext {
    MORNING,
    WORK,
    EVENING,
    GAMING,
    WEEKEND,
    LATE_NIGHT,
    GENERAL
}

sealed class FabAction(
    open val label: String,
    open val description: String,
    open val icon: ImageVector
) {
    data class UpdateDailyApp(
        override val label: String,
        override val description: String,
        override val icon: ImageVector,
        val appId: String,
        val appName: String
    ) : FabAction(label, description, icon)

    data class QuickInstall(
        override val label: String,
        override val description: String,
        override val icon: ImageVector,
        val targetApps: List<com.sugarmunch.app.data.AppEntry>
    ) : FabAction(label, description, icon)

    data class FocusMode(
        override val label: String,
        override val description: String,
        override val icon: ImageVector,
        val effects: List<String>,
        val theme: String
    ) : FabAction(label, description, icon)

    data class ChillMode(
        override val label: String,
        override val description: String,
        override val icon: ImageVector,
        val suggestedApps: List<com.sugarmunch.app.data.AppEntry>,
        val effects: List<String>
    ) : FabAction(label, description, icon)

    data class GamingMode(
        override val label: String,
        override val description: String,
        override val icon: ImageVector,
        val effects: List<String>,
        val performanceProfile: PerformanceProfile
    ) : FabAction(label, description, icon)

    data class NightMode(
        override val label: String,
        override val description: String,
        override val icon: ImageVector,
        val enableBlueLightFilter: Boolean,
        val theme: String
    ) : FabAction(label, description, icon)

    data class ApplyTheme(
        override val label: String,
        override val description: String,
        override val icon: ImageVector,
        val themeId: String
    ) : FabAction(label, description, icon)

    data class Discover(
        override val label: String,
        override val description: String,
        override val icon: ImageVector,
        val featuredApps: List<com.sugarmunch.app.data.AppEntry>
    ) : FabAction(label, description, icon)

    data class SmartSuggestion(
        override val label: String,
        override val description: String,
        override val icon: ImageVector,
        val suggestedCategory: String?
    ) : FabAction(label, description, icon)
}

sealed class UserIntent {
    object StartingDay : UserIntent()
    object WindingDown : UserIntent()
    object Entertainment : UserIntent()
    object Exploring : UserIntent()
    data class Installing(val likelyNextCategory: String?) : UserIntent()
    data class Browsing(val currentInterest: String?) : UserIntent()
}

enum class PerformanceProfile {
    LOW,
    MEDIUM,
    HIGH
}

data class ContextualAction(
    val label: String,
    val icon: ImageVector
)

@Composable
fun ContextAwareFab(
    onActionClick: (FabAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val manager = remember { ContextAwareFabManager.getInstance(context) }
    val fabContext by manager.currentContext.collectAsState()
    val action by manager.suggestedAction.collectAsState()
    var chips by remember { mutableStateOf(manager.getContextualActions()) }

    LaunchedEffect(fabContext) {
        chips = manager.getContextualActions()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            chips.take(2).forEach { chip ->
                AssistChip(
                    onClick = { },
                    label = { Text(chip.label) },
                    leadingIcon = { Icon(chip.icon, contentDescription = null) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = getContextColor(fabContext).copy(alpha = 0.15f)
                    )
                )
            }
        }
        action?.let { currentAction ->
            ExtendedFloatingActionButton(
                onClick = {
                    manager.executeAction(currentAction)
                    onActionClick(currentAction)
                },
                icon = { Icon(currentAction.icon, contentDescription = null) },
                text = { Text(currentAction.label) },
                containerColor = getContextColor(fabContext),
                contentColor = Color.White
            )
        }
    }
}

@Composable
fun ConfiguredEffectsFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier.padding(8.dp),
        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
        text = { Text("Effects", color = MaterialTheme.colorScheme.onPrimary) },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )
}

@Composable
private fun getContextColor(context: FabContext): Color {
    return when (context) {
        FabContext.MORNING -> Color(0xFFFFA726)
        FabContext.WORK -> Color(0xFF42A5F5)
        FabContext.EVENING -> Color(0xFFAB47BC)
        FabContext.GAMING -> Color(0xFFEF5350)
        FabContext.WEEKEND -> Color(0xFF66BB6A)
        FabContext.LATE_NIGHT -> Color(0xFF5C6BC0)
        FabContext.GENERAL -> MaterialTheme.colorScheme.primary
    }
}
