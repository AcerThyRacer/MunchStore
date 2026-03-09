package com.sugarmunch.app.ui.customization.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.sugarmunch.app.ui.customization.*
import com.sugarmunch.app.ui.customization.viewmodel.CustomizationViewModel
import com.sugarmunch.app.ui.customization.viewmodel.ResetCategory

/**
 * EXTREME Customization Navigation Graph
 * All customization screens and their routes
 */

object CustomizationRoutes {
    const val DASHBOARD = "customization_dashboard"
    const val BACKGROUND = "background_customization"
    const val COLOR_SYSTEM = "color_system"
    const val COMPONENT_ANIMATION = "component_animation"
    const val PERFORMANCE_PROFILES = "performance_profiles"
    const val GESTURE_CONTROL = "gesture_control"
    const val TOUCH_HAPTIC = "touch_haptic"
    const val GRID_SPACING = "grid_spacing"
    const val NAVIGATION = "navigation_customization"
    const val CARD_STYLE = "card_style"
    const val TYPOGRAPHY = "typography"
    const val EFFECT_MATRIX = "effect_matrix"
    const val PARTICLE_SYSTEM = "particle_system"
    const val PROFILE_MANAGEMENT = "profile_management"
    const val SMART_PRESETS = "smart_presets"
    const val EXPERIMENTAL_LAB = "experimental_lab"
    const val BACKUP_MIGRATION = "backup_migration"
}

fun NavGraphBuilder.customizationNavGraph(
    navController: NavHostController,
    viewModel: CustomizationViewModel
) {
    navigation(
        startDestination = CustomizationRoutes.DASHBOARD,
        route = "customization"
    ) {
        // Dashboard
        composable(CustomizationRoutes.DASHBOARD) {
            CustomizationDashboardScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBackground = {
                    navController.navigate(CustomizationRoutes.BACKGROUND)
                },
                onNavigateToColorSystem = {
                    navController.navigate(CustomizationRoutes.COLOR_SYSTEM)
                },
                onNavigateToComponentAnimation = {
                    navController.navigate(CustomizationRoutes.COMPONENT_ANIMATION)
                },
                onNavigateToPerformanceProfiles = {
                    navController.navigate(CustomizationRoutes.PERFORMANCE_PROFILES)
                },
                onNavigateToGestureControl = {
                    navController.navigate(CustomizationRoutes.GESTURE_CONTROL)
                },
                onNavigateToTouchHaptic = {
                    navController.navigate(CustomizationRoutes.TOUCH_HAPTIC)
                },
                onNavigateToGridSpacing = {
                    navController.navigate(CustomizationRoutes.GRID_SPACING)
                },
                onNavigateToNavigation = {
                    navController.navigate(CustomizationRoutes.NAVIGATION)
                },
                onNavigateToCardStyle = {
                    navController.navigate(CustomizationRoutes.CARD_STYLE)
                },
                onNavigateToTypography = {
                    navController.navigate(CustomizationRoutes.TYPOGRAPHY)
                },
                onNavigateToEffectMatrix = {
                    navController.navigate(CustomizationRoutes.EFFECT_MATRIX)
                },
                onNavigateToParticleSystem = {
                    navController.navigate(CustomizationRoutes.PARTICLE_SYSTEM)
                },
                onNavigateToProfileManagement = {
                    navController.navigate(CustomizationRoutes.PROFILE_MANAGEMENT)
                },
                onNavigateToSmartPresets = {
                    navController.navigate(CustomizationRoutes.SMART_PRESETS)
                },
                onNavigateToExperimentalLab = {
                    navController.navigate(CustomizationRoutes.EXPERIMENTAL_LAB)
                },
                onNavigateToBackupMigration = {
                    navController.navigate(CustomizationRoutes.BACKUP_MIGRATION)
                }
            )
        }

        // Background Customization
        composable(CustomizationRoutes.BACKGROUND) {
            BackgroundCustomizationScreen(
                onNavigateBack = { navController.popBackStack() },
                config = viewModel.backgroundConfig.value,
                onConfigChange = { viewModel.saveBackgroundConfig(it) }
            )
        }

        // Color System
        composable(CustomizationRoutes.COLOR_SYSTEM) {
            ColorSystemScreen(
                onNavigateBack = { navController.popBackStack() },
                profile = viewModel.colorProfile.value,
                onProfileChange = { viewModel.saveColorProfile(it) }
            )
        }

        // Component Animation
        composable(CustomizationRoutes.COMPONENT_ANIMATION) {
            ComponentAnimationScreen(
                onNavigateBack = { navController.popBackStack() },
                profile = viewModel.animationProfile.value,
                onProfileChange = { viewModel.saveAnimationProfile(it) }
            )
        }

        // Performance Profiles
        composable(CustomizationRoutes.PERFORMANCE_PROFILES) {
            PerformanceProfilesScreen(
                onNavigateBack = { navController.popBackStack() },
                profile = viewModel.animationProfile.value,
                onProfileChange = { viewModel.saveAnimationProfile(it) }
            )
        }

        // Gesture Control
        composable(CustomizationRoutes.GESTURE_CONTROL) {
            GestureControlScreen(
                onNavigateBack = { navController.popBackStack() },
                gestureMapping = viewModel.gestureMapping.value,
                onGestureMappingChange = { viewModel.saveGestureMapping(it) }
            )
        }

        // Touch & Haptic
        composable(CustomizationRoutes.TOUCH_HAPTIC) {
            TouchHapticScreen(
                onNavigateBack = { navController.popBackStack() },
                hapticPattern = viewModel.hapticPattern.value,
                onHapticPatternChange = { viewModel.saveHapticPattern(it) }
            )
        }

        // Grid & Spacing
        composable(CustomizationRoutes.GRID_SPACING) {
            GridSpacingEditor(
                onNavigateBack = { navController.popBackStack() },
                layoutConfig = viewModel.layoutConfig.value,
                onLayoutConfigChange = { viewModel.saveLayoutConfig(it) }
            )
        }

        // Navigation Customization
        composable(CustomizationRoutes.NAVIGATION) {
            NavigationCustomizationScreen(
                onNavigateBack = { navController.popBackStack() },
                navigationConfig = viewModel.navigationConfig.value,
                onNavigationConfigChange = { viewModel.saveNavigationConfig(it) }
            )
        }

        // Card Style
        composable(CustomizationRoutes.CARD_STYLE) {
            CardStyleScreen(
                onNavigateBack = { navController.popBackStack() },
                cardStyleConfig = viewModel.cardStyleConfig.value,
                onCardStyleConfigChange = { viewModel.saveCardStyleConfig(it) }
            )
        }

        // Typography
        composable(CustomizationRoutes.TYPOGRAPHY) {
            TypographyScreen(
                onNavigateBack = { navController.popBackStack() },
                typographyConfig = viewModel.typographyConfig.value,
                onTypographyConfigChange = { viewModel.saveTypographyConfig(it) }
            )
        }

        // Effect Matrix
        composable(CustomizationRoutes.EFFECT_MATRIX) {
            EffectMatrixScreen(
                onNavigateBack = { navController.popBackStack() },
                effectConfigs = viewModel.effectConfigs.value,
                onEffectConfigsChange = { viewModel.saveEffectConfigs(it) }
            )
        }

        // Particle System
        composable(CustomizationRoutes.PARTICLE_SYSTEM) {
            ParticleSystemScreen(
                onNavigateBack = { navController.popBackStack() },
                particleConfig = viewModel.particleConfig.value,
                onParticleConfigChange = { viewModel.saveParticleConfig(it) }
            )
        }

        // Profile Management
        composable(CustomizationRoutes.PROFILE_MANAGEMENT) {
            ProfileManagementScreen(
                onNavigateBack = { navController.popBackStack() },
                profiles = viewModel.profiles.value,
                activeProfileId = viewModel.activeProfileId.value,
                onProfileSelected = { viewModel.activateProfile(it) },
                onProfileAdded = { viewModel.createProfile(name = it.name) },
                onProfileUpdated = { viewModel.updateProfile(it) },
                onProfileDeleted = { viewModel.deleteProfile(it) }
            )
        }

        // Smart Presets
        composable(CustomizationRoutes.SMART_PRESETS) {
            SmartPresetsScreen(
                onNavigateBack = { navController.popBackStack() },
                presets = viewModel.presets.value,
                onPresetActivated = { viewModel.activatePreset(it.id) },
                onPresetCreated = { viewModel.createPreset(name = it.name) },
                onPresetDeleted = { viewModel.deletePreset(it.id) }
            )
        }

        // Experimental Lab
        composable(CustomizationRoutes.EXPERIMENTAL_LAB) {
            ExperimentalLabScreen(
                onNavigateBack = { navController.popBackStack() },
                experimentalFlags = viewModel.experimentalFlags.value,
                onFlagsChange = { viewModel.saveExperimentalFlags(it) }
            )
        }

        // Backup & Migration
        composable(CustomizationRoutes.BACKUP_MIGRATION) {
            BackupMigrationScreen(
                onNavigateBack = { navController.popBackStack() },
                onBackupCreated = { type, categories ->
                    viewModel.createBackup(type, categories)
                },
                onRestoreRequested = { backupId, merge ->
                    viewModel.restoreBackup(backupId, merge)
                },
                onExportRequested = { categories ->
                    viewModel.exportSettings(categories) { /* Handle export */ }
                },
                onImportRequested = { json, merge ->
                    viewModel.importSettings(json, merge) { /* Handle result */ }
                }
            )
        }
    }
}
