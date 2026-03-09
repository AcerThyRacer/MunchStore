package com.sugarmunch.app.ui.customization.manager

import com.sugarmunch.app.ui.customization.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * EXTREME Profile Manager for SugarMunch
 * Manages user profiles, switching, and profile-based settings
 */
class ProfileManager(
    private val customizationRepository: CustomizationRepository
) {
    private val mutex = Mutex()

    private val _activeProfileId = MutableStateFlow<String?>(null)
    val activeProfileId: StateFlow<String?> = _activeProfileId.asStateFlow()

    private val _currentProfile = MutableStateFlow<UserProfile?>(null)
    val currentProfile: StateFlow<UserProfile?> = _currentProfile.asStateFlow()

    private val _profiles = MutableStateFlow<List<UserProfile>>(emptyList())
    val profiles: StateFlow<List<UserProfile>> = _profiles.asStateFlow()

    /**
     * Initialize profile manager by loading profiles from repository
     */
    suspend fun initialize() {
        mutex.withLock {
            // Load active profile ID
            _activeProfileId.value = customizationRepository.activeProfileIdFlow.value

            // Load all profiles
            _profiles.value = customizationRepository.userProfilesFlow.value

            // Load current profile
            _activeProfileId.value?.let { id ->
                _currentProfile.value = _profiles.value.find { it.id == id }
            }

            // Observe changes
            observeProfiles()
        }
    }

    private suspend fun observeProfiles() {
        customizationRepository.userProfilesFlow.collect { profiles ->
            _profiles.value = profiles
            _activeProfileId.value?.let { id ->
                _currentProfile.value = profiles.find { it.id == id }
            }
        }
    }

    /**
     * Create a new profile
     */
    suspend fun createProfile(
        name: String,
        category: ProfileCategory = ProfileCategory.CUSTOM,
        description: String = "",
        iconId: String = "default",
        iconColor: String = "#FFFF69B4"
    ): UserProfile {
        return mutex.withLock {
            val profile = UserProfile(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                category = category,
                description = description,
                iconId = iconId,
                iconColor = iconColor,
                createdAt = System.currentTimeMillis(),
                lastUsedAt = System.currentTimeMillis()
            )

            customizationRepository.addUserProfile(profile)
            profile
        }
    }

    /**
     * Update an existing profile
     */
    suspend fun updateProfile(profile: UserProfile) {
        mutex.withLock {
            val updated = profile.copy(
                lastUsedAt = System.currentTimeMillis()
            )
            customizationRepository.updateUserProfile(updated)
        }
    }

    /**
     * Delete a profile
     */
    suspend fun deleteProfile(profileId: String) {
        mutex.withLock {
            // If deleting active profile, clear active
            if (_activeProfileId.value == profileId) {
                _activeProfileId.value = null
                _currentProfile.value = null
                customizationRepository.setActiveProfileId(null)
            }

            customizationRepository.deleteUserProfile(profileId)
        }
    }

    /**
     * Activate a profile
     */
    suspend fun activateProfile(profileId: String) {
        mutex.withLock {
            val profile = _profiles.value.find { it.id == profileId }
            if (profile != null) {
                _activeProfileId.value = profileId
                _currentProfile.value = profile

                // Update usage stats
                val updated = profile.copy(
                    lastUsedAt = System.currentTimeMillis(),
                    usageCount = profile.usageCount + 1
                )
                customizationRepository.updateUserProfile(updated)

                // Save as active
                customizationRepository.setActiveProfileId(profileId)

                // Apply profile settings
                applyProfile(profile)
            }
        }
    }

    /**
     * Deactivate current profile
     */
    suspend fun deactivateProfile() {
        mutex.withLock {
            _activeProfileId.value = null
            _currentProfile.value = null
            customizationRepository.setActiveProfileId(null)
        }
    }

    /**
     * Apply profile settings to repository
     */
    private suspend fun applyProfile(profile: UserProfile) {
        // Save all profile configurations
        customizationRepository.saveBackgroundConfig(profile.backgroundConfig)
        customizationRepository.saveColorProfile(profile.colorProfile)
        customizationRepository.saveAnimationProfile(profile.animationProfile)
        customizationRepository.saveGestureMapping(profile.gestureMapping)
        customizationRepository.saveHapticPattern(profile.hapticPattern)
        customizationRepository.saveLayoutConfig(profile.layoutConfig)
        customizationRepository.saveNavigationConfig(profile.navigationConfig)
        customizationRepository.saveCardStyleConfig(profile.cardStyleConfig)
        customizationRepository.saveTypographyConfig(profile.typographyConfig)
        customizationRepository.saveParticleConfig(profile.particleConfig)

        if (profile.effectConfigs.isNotEmpty()) {
            customizationRepository.saveEffectConfigs(profile.effectConfigs)
        }
    }

    /**
     * Duplicate an existing profile
     */
    suspend fun duplicateProfile(sourceProfileId: String, newName: String): UserProfile {
        return mutex.withLock {
            val source = _profiles.value.find { it.id == sourceProfileId }
                ?: throw IllegalArgumentException("Profile not found: $sourceProfileId")

            val duplicated = source.copy(
                id = java.util.UUID.randomUUID().toString(),
                name = newName,
                createdAt = System.currentTimeMillis(),
                lastUsedAt = System.currentTimeMillis(),
                usageCount = 0,
                isFavorite = false
            )

            customizationRepository.addUserProfile(duplicated)
            duplicated
        }
    }

    /**
     * Set profile as favorite
     */
    suspend fun setFavorite(profileId: String, isFavorite: Boolean) {
        mutex.withLock {
            val profile = _profiles.value.find { it.id == profileId }
            if (profile != null) {
                updateProfile(profile.copy(isFavorite = isFavorite))
            }
        }
    }

    /**
     * Get profile by ID
     */
    fun getProfile(profileId: String): UserProfile? {
        return _profiles.value.find { it.id == profileId }
    }

    /**
     * Get all profiles in a category
     */
    fun getProfilesByCategory(category: ProfileCategory): List<UserProfile> {
        return _profiles.value.filter { it.category == category }
    }

    /**
     * Get favorite profiles
     */
    fun getFavoriteProfiles(): List<UserProfile> {
        return _profiles.value.filter { it.isFavorite }
    }

    /**
     * Get recently used profiles
     */
    fun getRecentlyUsedProfiles(limit: Int = 5): List<UserProfile> {
        return _profiles.value
            .sortedByDescending { it.lastUsedAt }
            .take(limit)
    }

    /**
     * Search profiles by name
     */
    fun searchProfiles(query: String): List<UserProfile> {
        if (query.isBlank()) return _profiles.value
        return _profiles.value.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true)
        }
    }

    /**
     * Export profile to JSON string
     */
    suspend fun exportProfile(profileId: String): String {
        val profile = getProfile(profileId)
            ?: throw IllegalArgumentException("Profile not found: $profileId")

        return kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            isLenient = true
        }.encodeToString(UserProfile.serializer(), profile)
    }

    /**
     * Import profile from JSON string
     */
    suspend fun importProfile(jsonString: String): UserProfile {
        val profile = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            isLenient = true
        }.decodeFromString(UserProfile.serializer(), jsonString)

        val newProfile = profile.copy(
            id = java.util.UUID.randomUUID().toString(),
            createdAt = System.currentTimeMillis(),
            lastUsedAt = System.currentTimeMillis(),
            usageCount = 0
        )

        customizationRepository.addUserProfile(newProfile)
        return newProfile
    }

    /**
     * Auto-switch profile based on context
     */
    suspend fun autoSwitchProfile(
        batteryLevel: Int? = null,
        isCharging: Boolean? = null,
        hourOfDay: Int? = null,
        currentApp: String? = null
    ) {
        // Time-based switching
        hourOfDay?.let { hour ->
            when {
                hour in 6..11 -> findAndActivateProfile(PresetCategory.MORNING)
                hour in 17..21 -> findAndActivateProfile(PresetCategory.EVENING)
                hour in 22..5 || hour in 0..5 -> findAndActivateProfile(PresetCategory.NIGHT)
            }
        }

        // Battery-based switching
        batteryLevel?.let { level ->
            when {
                level <= 20 && !isCharging -> findAndActivateProfile(PresetCategory.BATTERY_SAVER)
                level <= 50 && !isCharging -> { /* Could activate power saver */ }
                isCharging == true -> { /* Could activate high performance */ }
            }
        }
    }

    private suspend fun findAndActivateProfile(category: PresetCategory) {
        val presetProfiles = _profiles.value.filter {
            it.category.name == category.name
        }
        if (presetProfiles.isNotEmpty()) {
            activateProfile(presetProfiles.first().id)
        }
    }
}
