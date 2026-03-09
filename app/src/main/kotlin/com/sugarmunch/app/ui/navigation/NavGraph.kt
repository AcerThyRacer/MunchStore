package com.sugarmunch.app.ui.navigation

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sugarmunch.app.data.LocalPreferencesRepository
import com.sugarmunch.app.features.screens.AchievementsScreen
import com.sugarmunch.app.ui.screens.CatalogScreen
import com.sugarmunch.app.ui.screens.CustomEffectBuilderScreen
import com.sugarmunch.app.ui.screens.CustomEffectConfig
import com.sugarmunch.app.ui.screens.DetailScreen
import com.sugarmunch.app.ui.screens.EffectScheduleScreen
import com.sugarmunch.app.ui.screens.EffectsScreen
import com.sugarmunch.app.ui.screens.OnboardingScreen2026
import com.sugarmunch.app.ui.screens.SettingsScreen
import com.sugarmunch.app.theme.screens.ThemeSettingsScreen
import com.sugarmunch.app.theme.screens.AnimationSettingsScreen
import com.sugarmunch.app.effects.v2.screens.EffectsScreenV2
import com.sugarmunch.app.effects.screens.SpecialEffectsSelectionScreen
import com.sugarmunch.app.effects.screens.FabConfigurationScreen
import com.sugarmunch.app.effects.x2026.Effects2026Screen
import com.sugarmunch.app.ui.screens.CatalogScreenV2
import com.sugarmunch.app.shop.screens.ShopScreen
import com.sugarmunch.app.rewards.screens.DailyRewardsScreen
import com.sugarmunch.app.analytics.screens.AnalyticsScreen
import com.sugarmunch.app.ui.screens.automation.AutomationScreen
import com.sugarmunch.app.ui.screens.automation.TaskBuilderScreen
import com.sugarmunch.app.ui.screens.TrailDetailScreen
import com.sugarmunch.app.ui.screens.SugarPassScreen
import com.sugarmunch.app.ui.screens.clan.ClanScreen
import com.sugarmunch.app.ui.screens.events.QuestsScreen
import com.sugarmunch.app.ui.screens.trading.MarketplaceScreen
import com.sugarmunch.app.ui.screens.trading.CreateTradeScreen
import com.sugarmunch.app.ai.ConfectionerScreen
import com.sugarmunch.app.ai.PruningScreen
import com.sugarmunch.app.ui.screens.AppPreviewScreen
import com.sugarmunch.app.ui.screens.P2PShareScreen
import com.sugarmunch.app.ui.screens.UtilityStudioScreen
import com.sugarmunch.app.developer.DeveloperScreen
import com.sugarmunch.app.ui.theme.ScopedSugarMunchTheme
import com.sugarmunch.app.ui.settings.AccountSettingsScreen
import com.sugarmunch.app.ui.settings.NotificationSettingsScreen
import com.sugarmunch.app.ui.settings.AccessibilitySettingsScreen
import com.sugarmunch.app.ui.settings.HelpScreen
import com.sugarmunch.app.ui.customization.CustomizationDashboardScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Catalog : Screen("catalog")
    data object Detail : Screen("detail/{appId}") {
        fun route(appId: String) = "detail/$appId"
    }
    data object Preview : Screen("preview/{appId}") {
        fun route(appId: String) = "preview/$appId"
    }
    data object UtilityStudio : Screen("utility-studio/{appId}") {
        fun route(appId: String) = "utility-studio/$appId"
    }
    data object TrailDetail : Screen("trail/{trailId}") {
        fun route(trailId: String) = "trail/$trailId"
    }
    data object Clan : Screen("clan")
    data object Marketplace : Screen("marketplace")
    data object CreateTrade : Screen("create-trade")
    data object Confectioner : Screen("confectioner")
    data object Pruning : Screen("pruning")
    data object P2PShare : Screen("p2p-share")
    data object Developer : Screen("developer")
    data object Settings : Screen("settings")
    data object AccountSettings : Screen("account-settings")
    data object NotificationSettings : Screen("notification-settings")
    data object AccessibilitySettings : Screen("accessibility-settings")
    data object Help : Screen("help")
    data object Effects : Screen("effects")
    data object CustomEffectBuilder : Screen("custom-effect")
    data object EffectSchedule : Screen("effect-schedule")
    data object ThemeSettings : Screen("theme-settings?appId={appId}") {
        fun route(appId: String? = null): String {
            return if (appId.isNullOrBlank()) "theme-settings" else "theme-settings?appId=$appId"
        }
    }
    data object AnimationSettings : Screen("animation-settings")
    data object Shop : Screen("shop")
    data object SugarPass : Screen("sugar-pass")
    data object DailyRewards : Screen("daily-rewards")
    data object Achievements : Screen("achievements")
    data object Quests : Screen("quests")
    data object Analytics : Screen("analytics")
    data object Automation : Screen("automation")
    data object TaskBuilder : Screen("task-builder?taskId={taskId}&templateId={templateId}") {
        fun route(taskId: String? = null, templateId: String? = null): String {
            return when {
                taskId != null -> "task-builder?taskId=$taskId"
                templateId != null -> "task-builder?templateId=$templateId"
                else -> "task-builder"
            }
        }
    }
    data object SpecialEffects : Screen("special-effects")
    data object FabConfiguration : Screen("fab-configuration")
    data object Effects2026 : Screen("effects-2026")
    data object ExtremeCustomization : Screen("extreme-customization")
    // SugarMunch Extreme Routes
    data object SugarEffects : Screen("sugar-effects")
    data object CandyFab : Screen("candy-fab")
    data object AchievementsScreen : Screen("achievements-screen")
    data object OnboardingReplay : Screen("onboarding-replay")
    data object ExtremeMode : Screen("extreme-mode")
    data object CandyFactoryOnboarding : Screen("candy-factory-onboarding")
}

@Composable
fun SugarMunchNavHost(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val preferencesRepository = LocalPreferencesRepository.current
    val onboardingCompleted by preferencesRepository.onboardingCompleted.collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()
    
    val startDestination = if (onboardingCompleted) Screen.Catalog.route else Screen.Onboarding.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen2026(
                onComplete = {
                    coroutineScope.launch {
                        preferencesRepository.setOnboardingCompleted(true)
                    }
                    navController.navigate(Screen.Catalog.route) { popUpTo(0) }
                },
                onRequestOverlayPermission = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    )
                }
            )
        }
        composable(Screen.Catalog.route) {
            CatalogScreenV2(
                onAppClick = { id -> navController.navigate(Screen.Detail.route(id)) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onThemeClick = { navController.navigate(Screen.ThemeSettings.route()) },
                onEffectsClick = { navController.navigate(Screen.Effects.route) },
                onShopClick = { navController.navigate(Screen.Shop.route) },
                onRewardsClick = { navController.navigate(Screen.DailyRewards.route) },
                onAnalyticsClick = { navController.navigate(Screen.Analytics.route) },
                onTrailClick = { trail -> navController.navigate(Screen.TrailDetail.route(trail.id)) },
                onClanClick = { navController.navigate(Screen.Clan.route) },
                onConfectionerClick = { navController.navigate(Screen.Confectioner.route) }
            )
        }
        composable(Screen.Clan.route) {
            ClanScreen(
                onBack = { navController.popBackStack() },
                onNavigateToClanList = { /* same screen, or future ClanList route */ },
                onNavigateToClanWar = { /* future ClanWar route or tab */ },
                onNavigateToChat = { /* future ClanChat route */ },
                onNavigateToRewards = { navController.navigate(Screen.Shop.route) }
            )
        }
        composable(Screen.Marketplace.route) {
            MarketplaceScreen(
                onBack = { navController.popBackStack() },
                onCreateListing = { navController.navigate(Screen.CreateTrade.route) }
            )
        }
        composable(Screen.CreateTrade.route) {
            CreateTradeScreen(
                onBack = { navController.popBackStack() },
                onTradeCreated = { navController.popBackStack() }
            )
        }
        composable(Screen.Confectioner.route) {
            ConfectionerScreen(
                onBack = { navController.popBackStack() },
                onAppClick = { id -> navController.navigate(Screen.Detail.route(id)) }
            )
        }
        composable(Screen.Pruning.route) {
            PruningScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.P2PShare.route) {
            P2PShareScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Developer.route) {
            DeveloperScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.AccountSettings.route) {
            AccountSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.NotificationSettings.route) {
            NotificationSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AccessibilitySettings.route) {
            AccessibilitySettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Help.route) {
            HelpScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.TrailDetail.route) { backStackEntry ->
            val trailId = backStackEntry.arguments?.getString("trailId") ?: ""
            TrailDetailScreen(
                trailId = trailId,
                onBack = { navController.popBackStack() },
                onAppClick = { id -> navController.navigate(Screen.Detail.route(id)) }
            )
        }
        composable(Screen.Detail.route) { backStackEntry ->
            val appId = backStackEntry.arguments?.getString("appId") ?: ""
            ScopedSugarMunchTheme(appId = appId) {
                DetailScreen(
                    appId = appId,
                    onBack = { navController.popBackStack() },
                    onPreviewClick = { navController.navigate(Screen.Preview.route(appId)) },
                    onUtilityStudioClick = { id -> navController.navigate(Screen.UtilityStudio.route(id)) }
                )
            }
        }
        composable(Screen.Preview.route) { backStackEntry ->
            val appId = backStackEntry.arguments?.getString("appId") ?: ""
            ScopedSugarMunchTheme(appId = appId) {
                AppPreviewScreen(
                    appId = appId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(Screen.UtilityStudio.route) { backStackEntry ->
            val appId = backStackEntry.arguments?.getString("appId") ?: ""
            ScopedSugarMunchTheme(appId = appId) {
                UtilityStudioScreen(
                    appId = appId,
                    onBack = { navController.popBackStack() },
                    onPreviewClick = { navController.navigate(Screen.Preview.route(appId)) },
                    onThemeStudioClick = { navController.navigate(Screen.ThemeSettings.route(appId)) },
                    onNearbyShareClick = { navController.navigate(Screen.P2PShare.route) }
                )
            }
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onEffectsClick = { navController.navigate(Screen.Effects.route) },
                onThemeClick = { navController.navigate(Screen.ThemeSettings.route()) },
                onShopClick = { navController.navigate(Screen.Shop.route) },
                onRewardsClick = { navController.navigate(Screen.DailyRewards.route) },
                onAchievementsClick = { navController.navigate(Screen.Achievements.route) },
                onQuestsClick = { navController.navigate(Screen.Quests.route) },
                onAnalyticsClick = { navController.navigate(Screen.Analytics.route) },
                onAutomationClick = { navController.navigate(Screen.Automation.route) },
                onAnimationClick = { navController.navigate(Screen.AnimationSettings.route) },
                onExtremeCustomizationClick = { navController.navigate(Screen.ExtremeCustomization.route) },
                onSpecialEffectsClick = { navController.navigate(Screen.SpecialEffects.route) },
                onFabConfigClick = { navController.navigate(Screen.FabConfiguration.route) },
                onEffects2026Click = { navController.navigate(Screen.Effects2026.route) },
                onAccountClick = { navController.navigate(Screen.AccountSettings.route) },
                onNotificationClick = { navController.navigate(Screen.NotificationSettings.route) },
                onAccessibilityClick = { navController.navigate(Screen.AccessibilitySettings.route) },
                onHelpClick = { navController.navigate(Screen.Help.route) }
            )
        }
        composable(Screen.SpecialEffects.route) {
            SpecialEffectsSelectionScreen(
                onBack = { navController.popBackStack() },
                onFabConfigClick = { navController.navigate(Screen.FabConfiguration.route) }
            )
        }
        composable(Screen.FabConfiguration.route) {
            FabConfigurationScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Effects2026.route) {
            Effects2026Screen(
                onBack = { navController.popBackStack() },
                onEffectDetail = { effectId ->
                    // Navigate to effect detail or activate effect
                }
            )
        }
        composable(Screen.AnimationSettings.route) {
            AnimationSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ExtremeCustomization.route) {
            CustomizationDashboardScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBackground = { navController.navigate("customization/background_customization") },
                onNavigateToColorSystem = { navController.navigate("customization/color_system") },
                onNavigateToComponentAnimation = { navController.navigate("customization/component_animation") },
                onNavigateToPerformanceProfiles = { navController.navigate("customization/performance_profiles") },
                onNavigateToGestureControl = { navController.navigate("customization/gesture_control") },
                onNavigateToTouchHaptic = { navController.navigate("customization/touch_haptic") },
                onNavigateToGridSpacing = { navController.navigate("customization/grid_spacing") },
                onNavigateToNavigation = { navController.navigate("customization/navigation_customization") },
                onNavigateToCardStyle = { navController.navigate("customization/card_style") },
                onNavigateToTypography = { navController.navigate("customization/typography") },
                onNavigateToEffectMatrix = { navController.navigate("customization/effect_matrix") },
                onNavigateToParticleSystem = { navController.navigate("customization/particle_system") },
                onNavigateToProfileManagement = { navController.navigate("customization/profile_management") },
                onNavigateToSmartPresets = { navController.navigate("customization/smart_presets") },
                onNavigateToExperimentalLab = { navController.navigate("customization/experimental_lab") },
                onNavigateToBackupMigration = { navController.navigate("customization/backup_migration") }
            )
        }
        composable(
            route = Screen.ThemeSettings.route,
            arguments = listOf(
                navArgument("appId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getString("appId")
            ThemeSettingsScreen(
                appId = appId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Effects.route) {
            EffectsScreenV2(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Shop.route) {
            ShopScreen(
                onBack = { navController.popBackStack() },
                onSugarPassClick = { navController.navigate(Screen.SugarPass.route) },
                onMarketplaceClick = { navController.navigate(Screen.Marketplace.route) }
            )
        }
        composable(Screen.SugarPass.route) {
            SugarPassScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.DailyRewards.route) {
            DailyRewardsScreen(
                onBack = { navController.popBackStack() },
                onSugarPassClick = { navController.navigate(Screen.SugarPass.route) },
                onAchievementsClick = { navController.navigate(Screen.Achievements.route) },
                onQuestsClick = { navController.navigate(Screen.Quests.route) }
            )
        }
        composable(Screen.Achievements.route) {
            AchievementsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Quests.route) {
            QuestsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Analytics.route) {
            AnalyticsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Automation.route) {
            AutomationScreen(
                onBack = { navController.popBackStack() },
                onCreateTask = { navController.navigate(Screen.TaskBuilder.route()) },
                onEditTask = { taskId -> navController.navigate(Screen.TaskBuilder.route(taskId = taskId)) },
                onTemplatesClick = { navController.navigate("templates") }
            )
        }
        composable(Screen.TaskBuilder.route) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            val templateId = backStackEntry.arguments?.getString("templateId")
            TaskBuilderScreen(
                taskId = taskId,
                templateId = templateId,
                onBack = { navController.popBackStack() },
                onTaskCreated = { navController.popBackStack() }
            )
        }
        
        // SugarMunch Extreme Routes
        composable(Screen.SugarEffects.route) {
            com.sugarmunch.app.ui.customization.SugarEffectsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.CandyFab.route) {
            com.sugarmunch.app.ui.customization.CandyFabScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.AchievementsScreen.route) {
            com.sugarmunch.app.ui.customization.AchievementsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.OnboardingReplay.route) {
            com.sugarmunch.app.ui.customization.OnboardingReplayScreen(
                onNavigateBack = { navController.popBackStack() },
                onRestartOnboarding = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ExtremeMode.route) {
            com.sugarmunch.app.ui.customization.ExtremeModeScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.CandyFactoryOnboarding.route) {
            com.sugarmunch.app.ui.onboarding.CandyFactoryOnboarding(
                onComplete = {
                    navController.navigate(Screen.Catalog.route) { popUpTo(0) }
                },
                onRequestOverlayPermission = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    )
                }
            )
        }
    }
}
