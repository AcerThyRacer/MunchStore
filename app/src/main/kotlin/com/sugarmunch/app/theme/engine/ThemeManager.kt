package com.sugarmunch.app.theme.engine

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.EntryPointAccessors
import com.sugarmunch.app.data.PreferencesRepository
import com.sugarmunch.app.progression.ProgressionTracker
import com.sugarmunch.app.theme.model.CandyTheme
import com.sugarmunch.app.theme.model.IntensityLevels
import com.sugarmunch.app.theme.model.ThemeCategory
import com.sugarmunch.app.theme.presets.ThemePresets
import com.sugarmunch.app.theme.profile.AppThemeOverride
import com.sugarmunch.app.theme.profile.ImportedFontAsset
import com.sugarmunch.app.theme.profile.ThemeProfile
import com.sugarmunch.app.theme.profile.ThemeProfileRepository
import com.sugarmunch.app.theme.profile.toCandyTheme
import com.sugarmunch.app.theme.profile.toThemeProfile
import com.sugarmunch.app.ui.typography.toDynamicTypographyConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "sugarmunch_themes")

class ThemeManager private constructor(
    private val context: Context,
    private val repository: ThemeProfileRepository,
    private val preferencesRepository: PreferencesRepository
) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val progressionTracker = ProgressionTracker.getInstance(context)

    private val themeIntensityKey = floatPreferencesKey("theme_intensity")
    private val backgroundIntensityKey = floatPreferencesKey("background_intensity")
    private val particleIntensityKey = floatPreferencesKey("particle_intensity")
    private val animationIntensityKey = floatPreferencesKey("animation_intensity")
    private val dynamicThemingKey = stringPreferencesKey("dynamic_theming_enabled")

    private val _previewProfile = MutableStateFlow<ThemeProfile?>(null)
    val previewProfile: StateFlow<ThemeProfile?> = _previewProfile.asStateFlow()

    private val _themeIntensity = MutableStateFlow(FloatRange(0f, 2f, 1f))
    val themeIntensity: StateFlow<Float> = _themeIntensity.map { it.current }.stateIn(
        scope, SharingStarted.Eagerly, 1f
    )

    private val _backgroundIntensity = MutableStateFlow(FloatRange(0f, 2f, 1f))
    val backgroundIntensity: StateFlow<Float> = _backgroundIntensity.map { it.current }.stateIn(
        scope, SharingStarted.Eagerly, 1f
    )

    private val _particleIntensity = MutableStateFlow(FloatRange(0f, 2f, 1f))
    val particleIntensity: StateFlow<Float> = _particleIntensity.map { it.current }.stateIn(
        scope, SharingStarted.Eagerly, 1f
    )

    private val _animationIntensity = MutableStateFlow(FloatRange(0f, 2f, 1f))
    val animationIntensity: StateFlow<Float> = _animationIntensity.map { it.current }.stateIn(
        scope, SharingStarted.Eagerly, 1f
    )

    private val _isDynamicThemingEnabled = MutableStateFlow(true)
    val isDynamicThemingEnabled: StateFlow<Boolean> = _isDynamicThemingEnabled.asStateFlow()

    val allProfiles: StateFlow<List<ThemeProfile>> = repository.allProfiles.stateIn(
        scope,
        SharingStarted.Eagerly,
        ThemePresets.ALL_THEMES_2026.map { it.toThemeProfile() }
    )

    val currentProfile: StateFlow<ThemeProfile> = combine(
        repository.currentProfile,
        previewProfile
    ) { current, preview ->
        preview ?: current
    }.stateIn(
        scope,
        SharingStarted.Eagerly,
        ThemePresets.getDefault().toThemeProfile()
    )

    val importedFonts: StateFlow<List<ImportedFontAsset>> = repository.importedFonts.stateIn(
        scope,
        SharingStarted.Eagerly,
        emptyList()
    )

    val allThemes: StateFlow<List<CandyTheme>> = allProfiles.map { profiles ->
        profiles.map { it.toCandyTheme() }
    }.stateIn(scope, SharingStarted.Eagerly, ThemePresets.ALL_THEMES_2026)

    val currentTheme: StateFlow<CandyTheme> = currentProfile.map { it.toCandyTheme() }.stateIn(
        scope,
        SharingStarted.Eagerly,
        ThemePresets.getDefault()
    )

    val currentColors = combine(currentTheme, themeIntensity) { theme, intensity ->
        theme.getColorsForIntensity(intensity)
    }.stateIn(scope, SharingStarted.Eagerly, ThemePresets.getDefault().getColorsForIntensity(1f))

    val currentBackground = combine(currentTheme, backgroundIntensity) { theme, intensity ->
        theme.getBackgroundGradient(intensity)
    }.stateIn(scope, SharingStarted.Eagerly, ThemePresets.getDefault().getBackgroundGradient(1f))

    val currentParticleConfig = combine(currentTheme, particleIntensity) { theme, intensity ->
        theme.particleConfig.copy(
            count = (theme.particleConfig.count.first * intensity).toInt()..
                (theme.particleConfig.count.last * intensity).toInt(),
            speed = com.sugarmunch.app.theme.model.FloatRange(
                theme.particleConfig.speed.min * intensity,
                theme.particleConfig.speed.max * intensity
            )
        )
    }.stateIn(scope, SharingStarted.Eagerly, ThemePresets.getDefault().particleConfig)

    val isDarkTheme = currentTheme.map { it.isDark }.stateIn(scope, SharingStarted.Eagerly, false)

    val themesByCategory = allThemes.map { themes ->
        themes.groupBy { it.category }
    }.stateIn(scope, SharingStarted.Eagerly, emptyMap())

    val intensityLabel = themeIntensity.map(::getIntensityLabel).stateIn(
        scope,
        SharingStarted.Eagerly,
        "🍭 Sweet"
    )

    init {
        loadSavedPreferences()
        if (_isDynamicThemingEnabled.value) {
            startDynamicThemingRoutine()
        }
    }

    fun observeAppThemeOverride(appId: String): kotlinx.coroutines.flow.Flow<AppThemeOverride?> {
        return preferencesRepository.getAppThemeOverride(appId)
    }

    fun observeThemeRuntime(appId: String? = null): kotlinx.coroutines.flow.Flow<ThemeRuntimeSnapshot> {
        val appOverrideFlow = appId?.let(preferencesRepository::getAppThemeOverride) ?: flowOf(null)
        return combine(
            repository.currentProfile,
            previewProfile,
            repository.allProfiles,
            appOverrideFlow,
            themeIntensity,
            backgroundIntensity,
            particleIntensity,
            animationIntensity,
            importedFonts
        ) { globalProfile, preview, allProfiles, appOverride, globalThemeIntensity, globalBgIntensity, globalParticleIntensity, globalAnimIntensity, fonts ->
            val effectiveProfile = resolveProfile(globalProfile, preview, allProfiles, appOverride)
            val theme = effectiveProfile.toCandyTheme(appOverride?.accentHex)
            val effectiveThemeIntensity = appOverride?.themeIntensity ?: globalThemeIntensity
            val effectiveBackgroundIntensity = appOverride?.backgroundIntensity ?: globalBgIntensity
            val effectiveParticleIntensity = appOverride?.particleIntensity ?: globalParticleIntensity
            val effectiveAnimationIntensity = appOverride?.animationIntensity ?: globalAnimIntensity
            ThemeRuntimeSnapshot(
                profile = effectiveProfile,
                theme = theme,
                colors = theme.getColorsForIntensity(effectiveThemeIntensity),
                typography = effectiveProfile.typography.toDynamicTypographyConfig(context, fonts),
                themeIntensity = effectiveThemeIntensity,
                backgroundIntensity = effectiveBackgroundIntensity,
                particleIntensity = effectiveParticleIntensity,
                animationIntensity = effectiveAnimationIntensity,
                isOverrideActive = appOverride?.enabled == true
            )
        }
    }

    fun setDynamicThemingEnabled(enabled: Boolean) {
        _isDynamicThemingEnabled.value = enabled
        scope.launch {
            context.themeDataStore.edit { prefs ->
                prefs[dynamicThemingKey] = enabled.toString()
            }
        }
        if (enabled) {
            startDynamicThemingRoutine()
        }
    }

    private fun startDynamicThemingRoutine() {
        scope.launch {
            while (_isDynamicThemingEnabled.value) {
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                val newThemeId = when (hour) {
                    in 6..11 -> "sunrise_sherbet"
                    in 12..17 -> "classic_candy"
                    in 18..21 -> "sunset_swirl"
                    else -> "midnight_mint"
                }
                if (currentProfile.value.id != newThemeId) {
                    repository.setCurrentProfileId(newThemeId)
                }
                delay(60_000)
            }
        }
    }

    private fun loadSavedPreferences() {
        scope.launch {
            context.themeDataStore.data.collect { prefs ->
                prefs[themeIntensityKey]?.let {
                    _themeIntensity.value = FloatRange(0f, 2f, it.coerceIn(0f, 2f))
                }
                prefs[backgroundIntensityKey]?.let {
                    _backgroundIntensity.value = FloatRange(0f, 2f, it.coerceIn(0f, 2f))
                }
                prefs[particleIntensityKey]?.let {
                    _particleIntensity.value = FloatRange(0f, 2f, it.coerceIn(0f, 2f))
                }
                prefs[animationIntensityKey]?.let {
                    _animationIntensity.value = FloatRange(0f, 2f, it.coerceIn(0f, 2f))
                }
                prefs[dynamicThemingKey]?.let {
                    _isDynamicThemingEnabled.value = it.toBooleanStrictOrNull() ?: true
                }
            }
        }
    }

    fun setTheme(theme: CandyTheme) {
        progressionTracker.onThemeChanged(theme.id)
        scope.launch {
            val profile = repository.getProfile(theme.id) ?: theme.toThemeProfile(builtIn = false)
            if (repository.getProfile(profile.id) == null) {
                repository.upsertProfile(profile.copy(category = ThemeCategory.CUSTOM))
            }
            repository.setCurrentProfileId(profile.id)
        }
    }

    fun setThemeById(themeId: String) {
        progressionTracker.onThemeChanged(themeId)
        scope.launch {
            repository.setCurrentProfileId(themeId)
        }
    }

    fun saveThemeProfile(profile: ThemeProfile, activate: Boolean = true) {
        scope.launch {
            val saved = repository.upsertProfile(profile.copy(category = ThemeCategory.CUSTOM))
            if (activate) {
                repository.setCurrentProfileId(saved.id)
            }
        }
    }

    fun duplicateThemeProfile(profileId: String, activate: Boolean = true) {
        scope.launch {
            val duplicated = repository.duplicateProfile(profileId) ?: return@launch
            if (activate) {
                repository.setCurrentProfileId(duplicated.id)
            }
        }
    }

    fun deleteThemeProfile(profileId: String) {
        scope.launch {
            repository.deleteProfile(profileId)
        }
    }

    fun setPreviewProfile(profile: ThemeProfile?) {
        _previewProfile.value = profile
    }

    fun clearPreviewProfile() {
        _previewProfile.value = null
    }

    fun applyAppThemeOverride(appId: String, appThemeOverride: AppThemeOverride?) {
        scope.launch {
            preferencesRepository.setAppThemeOverride(appId, appThemeOverride)
        }
    }

    fun clearAppThemeOverride(appId: String) {
        scope.launch {
            preferencesRepository.clearAppThemeOverride(appId)
        }
    }

    fun saveImportedFont(font: ImportedFontAsset) {
        scope.launch {
            repository.saveImportedFont(font)
        }
    }

    fun removeImportedFont(fontId: String) {
        scope.launch {
            repository.removeImportedFont(fontId)
        }
    }

    fun setThemeIntensity(intensity: Float) {
        val coerced = intensity.coerceIn(0f, 2f)
        _themeIntensity.value = FloatRange(0f, 2f, coerced)
        progressionTracker.onThemeIntensityChanged(coerced)
        scope.launch {
            context.themeDataStore.edit { prefs ->
                prefs[themeIntensityKey] = coerced
            }
        }
    }

    fun setBackgroundIntensity(intensity: Float) {
        val coerced = intensity.coerceIn(0f, 2f)
        _backgroundIntensity.value = FloatRange(0f, 2f, coerced)
        scope.launch {
            context.themeDataStore.edit { prefs ->
                prefs[backgroundIntensityKey] = coerced
            }
        }
    }

    fun setParticleIntensity(intensity: Float) {
        val coerced = intensity.coerceIn(0f, 2f)
        _particleIntensity.value = FloatRange(0f, 2f, coerced)
        scope.launch {
            context.themeDataStore.edit { prefs ->
                prefs[particleIntensityKey] = coerced
            }
        }
    }

    fun setAnimationIntensity(intensity: Float) {
        val coerced = intensity.coerceIn(0f, 2f)
        _animationIntensity.value = FloatRange(0f, 2f, coerced)
        scope.launch {
            context.themeDataStore.edit { prefs ->
                prefs[animationIntensityKey] = coerced
            }
        }
    }

    fun setMasterIntensity(intensity: Float) {
        setThemeIntensity(intensity)
        setBackgroundIntensity(intensity)
        setParticleIntensity(intensity)
        setAnimationIntensity(intensity)
    }

    fun applyIntensityPreset(preset: IntensityPreset) {
        when (preset) {
            IntensityPreset.CHILL -> setMasterIntensity(IntensityLevels.CHILL)
            IntensityPreset.NORMAL -> setMasterIntensity(IntensityLevels.NORMAL)
            IntensityPreset.SWEET -> setMasterIntensity(IntensityLevels.SWEET)
            IntensityPreset.SUGARRUSH -> setMasterIntensity(IntensityLevels.SUGARRUSH)
            IntensityPreset.MAXIMUM -> setMasterIntensity(IntensityLevels.MAXIMUM)
        }
    }

    fun boostIntensity(durationMs: Long = 3000, boostAmount: Float = 0.5f) {
        val original = _themeIntensity.value.current
        val boosted = (original + boostAmount).coerceIn(0f, 2f)
        setMasterIntensity(boosted)
        scope.launch {
            delay(durationMs)
            setMasterIntensity(original)
        }
    }

    fun getThemesByCategory(category: ThemeCategory): List<CandyTheme> {
        return allThemes.value.filter { it.category == category }
    }

    fun searchThemes(query: String): List<CandyTheme> {
        return allThemes.value.filter { theme ->
            theme.name.contains(query, ignoreCase = true) ||
                theme.description.contains(query, ignoreCase = true) ||
                theme.category.name.contains(query, ignoreCase = true)
        }
    }

    private fun resolveProfile(
        globalProfile: ThemeProfile,
        preview: ThemeProfile?,
        allProfiles: List<ThemeProfile>,
        appOverride: AppThemeOverride?
    ): ThemeProfile {
        if (preview != null) return preview
        if (appOverride?.enabled == true && appOverride.themeProfileId != null) {
            return allProfiles.firstOrNull { it.id == appOverride.themeProfileId } ?: globalProfile
        }
        return globalProfile
    }

    private fun getIntensityLabel(intensity: Float): String {
        return when {
            intensity <= 0.3f -> "🧊 Chill"
            intensity <= 0.7f -> "🍬 Normal"
            intensity <= 1.0f -> "🍭 Sweet"
            intensity <= 1.5f -> "🚀 SugarRush"
            else -> "🔥 MAXIMUM"
        }
    }

    data class FloatRange(val min: Float, val max: Float, val current: Float)

    enum class IntensityPreset {
        CHILL,
        NORMAL,
        SWEET,
        SUGARRUSH,
        MAXIMUM
    }

    companion object {
        @Volatile
        private var instance: ThemeManager? = null

        fun getInstance(context: Context): ThemeManager {
            return instance ?: synchronized(this) {
                instance ?: run {
                    val appContext = context.applicationContext
                    val entryPoint = EntryPointAccessors.fromApplication(
                        appContext,
                        ThemeManagerEntryPoint::class.java
                    )
                    ThemeManager(
                        context = appContext,
                        repository = entryPoint.themeProfileRepository(),
                        preferencesRepository = entryPoint.preferencesRepository()
                    ).also { instance = it }
                }
            }
        }

        fun destroy() {
            instance = null
        }
    }
}
