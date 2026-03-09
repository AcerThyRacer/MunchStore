package com.sugarmunch.app.accessibility

import android.content.Context
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityEvent
import androidx.compose.runtime.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.semantics.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AccessibilityManager - Full accessibility suite for inclusive design.
 * Features:
 * - Screen reader optimization
 * - High contrast themes
 * - Color blind modes
 * - Reduced motion presets
 * - One-handed mode
 * - Voice navigation
 * - Switch access support
 */
class AccessibilityManager(private val context: Context) {

    private val systemA11yManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

    // Accessibility settings state
    private val _settings = MutableStateFlow(AccessibilitySettings())
    val settings: StateFlow<AccessibilitySettings> = _settings.asStateFlow()

    private val _isScreenReaderEnabled = MutableStateFlow(false)
    val isScreenReaderEnabled: StateFlow<Boolean> = _isScreenReaderEnabled.asStateFlow()

    private val _isHighContrastEnabled = MutableStateFlow(false)
    val isHighContrastEnabled: StateFlow<Boolean> = _isHighContrastEnabled.asStateFlow()

    private val _isTalkBackEnabled = MutableStateFlow(false)
    val isTalkBackEnabled: StateFlow<Boolean> = _isTalkBackEnabled.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: AccessibilityManager? = null

        fun getInstance(context: Context): AccessibilityManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AccessibilityManager(context).also { INSTANCE = it }
            }
        }
    }

    init {
        updateAccessibilityState()
    }

    /**
     * Update accessibility state from system settings
     */
    fun updateAccessibilityState() {
        _isScreenReaderEnabled.value = systemA11yManager.isEnabled
        _isTalkBackEnabled.value = checkTalkBackEnabled()
    }

    /**
     * Check if TalkBack is enabled
     */
    private fun checkTalkBackEnabled(): Boolean {
        return try {
            val enabledServices = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            enabledServices?.contains("com.google.android.marvin.talkback") == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Enable high contrast mode
     */
    fun setHighContrastEnabled(enabled: Boolean) {
        _isHighContrastEnabled.value = enabled
        _settings.value = _settings.value.copy(
            highContrastEnabled = enabled
        )
    }

    /**
     * Set color blind mode
     */
    fun setColorBlindMode(mode: ColorBlindMode) {
        _settings.value = _settings.value.copy(
            colorBlindMode = mode
        )
    }

    /**
     * Set motion reduction level
     */
    fun setMotionReduction(level: MotionReductionLevel) {
        _settings.value = _settings.value.copy(
            motionReduction = level
        )
    }

    /**
     * Enable one-handed mode
     */
    fun setOneHandedEnabled(enabled: Boolean) {
        _settings.value = _settings.value.copy(
            oneHandedMode = enabled
        )
    }

    /**
     * Set one-handed mode position
     */
    fun setOneHandedPosition(position: OneHandedPosition) {
        _settings.value = _settings.value.copy(
            oneHandedPosition = position
        )
    }

    /**
     * Set font scale
     */
    fun setFontScale(scale: Float) {
        _settings.value = _settings.value.copy(
            fontScale = scale
        )
    }

    /**
     * Set minimum touch target size
     */
    fun setMinTouchTargetSize(sizeDp: Int) {
        _settings.value = _settings.value.copy(
            minTouchTargetSizeDp = sizeDp
        )
    }

    /**
     * Enable voice navigation
     */
    fun setVoiceNavigationEnabled(enabled: Boolean) {
        _settings.value = _settings.value.copy(
            voiceNavigationEnabled = enabled
        )
    }

    /**
     * Announce a message to screen reader
     */
    fun announceForAccessibility(message: String) {
        if (_isTalkBackEnabled.value || _isScreenReaderEnabled.value) {
            // Would trigger accessibility announcement
        }
    }

    /**
     * Check if reduced motion should be used
     */
    fun shouldReduceMotion(): Boolean {
        return _settings.value.motionReduction != MotionReductionLevel.NONE
    }

    /**
     * Get animation duration modifier (0.0 = no animation, 1.0 = normal)
     */
    fun getAnimationDurationModifier(): Float {
        return when (_settings.value.motionReduction) {
            MotionReductionLevel.NONE -> 1.0f
            MotionReductionLevel.REDUCED -> 0.5f
            MotionReductionLevel.MINIMAL -> 0.2f
            MotionReductionLevel.OFF -> 0.0f
        }
    }

    /**
     * Get appropriate colors based on accessibility settings
     */
    fun getAccessibilityColors(): AccessibilityColors {
        val base = AccessibilityColors()

        return when {
            _settings.value.highContrastEnabled -> base.copy(
                primary = android.graphics.Color.BLACK,
                onPrimary = android.graphics.Color.WHITE,
                background = android.graphics.Color.WHITE,
                onBackground = android.graphics.Color.BLACK
            )

            _settings.value.colorBlindMode != ColorBlindMode.NONE -> applyColorBlindAdjustments(
                base,
                _settings.value.colorBlindMode
            )

            else -> base
        }
    }

    private fun applyColorBlindAdjustments(
        colors: AccessibilityColors,
        mode: ColorBlindMode
    ): AccessibilityColors {
        return when (mode) {
            ColorBlindMode.PROTANOPIA -> colors.copy(
                // Adjust reds to be distinguishable
                error = android.graphics.Color.parseColor("#FF6600")
            )
            ColorBlindMode.DEUTERANOPIA -> colors.copy(
                error = android.graphics.Color.parseColor("#FF6600")
            )
            ColorBlindMode.TRITANOPIA -> colors.copy(
                // Adjust blues/yellows
                secondary = android.graphics.Color.parseColor("#00BFFF")
            )
            else -> colors
        }
    }

    /**
     * Check if touch target meets minimum size
     */
    fun isTouchTargetValid(sizeDp: Int): Boolean {
        return sizeDp >= _settings.value.minTouchTargetSizeDp
    }

    /**
     * Get accessibility profile for current settings
     */
    fun getAccessibilityProfile(): AccessibilityProfile {
        return AccessibilityProfile(
            highContrast = _settings.value.highContrastEnabled,
            colorBlindMode = _settings.value.colorBlindMode,
            motionReduction = _settings.value.motionReduction,
            fontScale = _settings.value.fontScale,
            oneHandedMode = _settings.value.oneHandedMode,
            voiceNavigation = _settings.value.voiceNavigationEnabled
        )
    }

    /**
     * Apply preset accessibility profile
     */
    fun applyPreset(preset: AccessibilityPreset) {
        _settings.value = when (preset) {
            AccessibilityPreset.DEFAULT -> AccessibilitySettings()
            AccessibilityPreset.LOW_VISION -> AccessibilitySettings(
                highContrastEnabled = true,
                fontScale = 1.3f,
                minTouchTargetSizeDp = 48
            )
            AccessibilityPreset.MOTOR_IMPAIRMENT -> AccessibilitySettings(
                minTouchTargetSizeDp = 48,
                longPressTimeout = 1000L
            )
            AccessibilityPreset.COGNITIVE -> AccessibilitySettings(
                motionReduction = MotionReductionLevel.REDUCED,
                simplifiedLayout = true
            )
            AccessibilityPreset.HEARING_IMPAIRMENT -> AccessibilitySettings(
                visualNotificationsEnabled = true,
                captionEnabled = true
            )
        }
    }
}

/**
 * Accessibility settings data class
 */
data class AccessibilitySettings(
    val highContrastEnabled: Boolean = false,
    val colorBlindMode: ColorBlindMode = ColorBlindMode.NONE,
    val motionReduction: MotionReductionLevel = MotionReductionLevel.NONE,
    val fontScale: Float = 1.0f,
    val minTouchTargetSizeDp: Int = 44,
    val oneHandedMode: Boolean = false,
    val oneHandedPosition: OneHandedPosition = OneHandedPosition.RIGHT,
    val longPressTimeout: Long = 400L,
    val voiceNavigationEnabled: Boolean = false,
    val visualNotificationsEnabled: Boolean = false,
    val captionEnabled: Boolean = false,
    val simplifiedLayout: Boolean = false
)

enum class ColorBlindMode {
    NONE,
    PROTANOPIA,
    DEUTERANOPIA,
    TRITANOPIA
}

enum class MotionReductionLevel {
    NONE,
    REDUCED,
    MINIMAL,
    OFF
}

enum class OneHandedPosition {
    LEFT,
    RIGHT
}

/**
 * Accessibility colors data class
 */
data class AccessibilityColors(
    val primary: Int = android.graphics.Color.parseColor("#E91E63"),
    val onPrimary: Int = android.graphics.Color.WHITE,
    val secondary: Int = android.graphics.Color.parseColor("#2196F3"),
    val onSecondary: Int = android.graphics.Color.WHITE,
    val background: Int = android.graphics.Color.parseColor("#FAFAFA"),
    val onBackground: Int = android.graphics.Color.parseColor("#212121"),
    val surface: Int = android.graphics.Color.WHITE,
    val onSurface: Int = android.graphics.Color.parseColor("#212121"),
    val error: Int = android.graphics.Color.RED,
    val success: Int = android.graphics.Color.parseColor("#4CAF50"),
    val warning: Int = android.graphics.Color.parseColor("#FF9800")
)

/**
 * Accessibility profile summary
 */
data class AccessibilityProfile(
    val highContrast: Boolean,
    val colorBlindMode: ColorBlindMode,
    val motionReduction: MotionReductionLevel,
    val fontScale: Float,
    val oneHandedMode: Boolean,
    val voiceNavigation: Boolean
)

enum class AccessibilityPreset {
    DEFAULT,
    LOW_VISION,
    MOTOR_IMPAIRMENT,
    COGNITIVE,
    HEARING_IMPAIRMENT
}

/**
 * Accessibility semantics helpers
 */
object AccessibilitySemantics {
    fun Modifier.accessibleButton(
        label: String,
        actionDescription: String? = null
    ): Modifier = this
        .semantics {
            role = Role.Button
            contentDescription = label
            actionDescription?.let {
                stateDescription = it
            }
        }

    fun Modifier.accessibleImage(
        label: String
    ): Modifier = this
        .semantics {
            role = Role.Image
            contentDescription = label
        }

    fun Modifier.accessibleList(
        itemCount: Int
    ): Modifier = this
        .semantics {
            role = Role.List
            stateDescription = "$itemCount items"
        }

    fun Modifier.accessibleListItem(
        position: Int,
        totalCount: Int,
        label: String
    ): Modifier = this
        .semantics {
            role = Role.ListItem
            contentDescription = "$label, ${position + 1} of $totalCount"
            traversalIndex = position
        }

    fun Modifier.accessibleHeading(
        level: Int = 1
    ): Modifier = this
        .semantics {
            heading()
            level?.let {
                stateDescription = "Heading level $it"
            }
        }

    fun Modifier.accessibleSwitch(
        label: String,
        isOn: Boolean,
        onToggle: () -> Unit
    ): Modifier = this
        .semantics {
            role = Role.Switch
            contentDescription = label
            stateDescription = if (isOn) "On" else "Off"
            customActions = listOf(
                CustomAccessibilityAction(
                    label = if (isOn) "Turn off" else "Turn on",
                    action = {
                        onToggle()
                        true
                    }
                )
            )
        }

    fun Modifier.accessibleSlider(
        label: String,
        value: Float,
        range: ClosedFloatingPointRange<Float>
    ): Modifier = this
        .semantics {
            role = Role.Slider
            contentDescription = label
            stateDescription = "${value.toInt()}%"
            progressBarRangeInfo = ProgressBarRangeInfo(range, 1)
        }

    fun Modifier.accessibleTab(
        label: String,
        position: Int,
        totalCount: Int,
        isSelected: Boolean
    ): Modifier = this
        .semantics {
            role = Role.Tab
            contentDescription = "$label, Tab ${position + 1} of $totalCount"
            stateDescription = if (isSelected) "Selected" else "Not selected"
            selected = isSelected
        }
}

/**
 * Voice command handlers
 */
data class VoiceCommand(
    val command: String,
    val aliases: List<String>,
    val action: () -> Unit
)

class VoiceNavigationManager {
    private val commands = mutableListOf<VoiceCommand>()

    fun registerCommand(command: VoiceCommand) {
        commands.add(command)
    }

    fun unregisterCommand(command: String) {
        commands.removeAll { it.command == command }
    }

    fun processVoiceInput(input: String): Boolean {
        val normalizedInput = input.lowercase().trim()

        for (cmd in commands) {
            if (normalizedInput.contains(cmd.command.lowercase()) ||
                cmd.aliases.any { normalizedInput.contains(it.lowercase()) }) {
                cmd.action()
                return true
            }
        }

        return false
    }
}
