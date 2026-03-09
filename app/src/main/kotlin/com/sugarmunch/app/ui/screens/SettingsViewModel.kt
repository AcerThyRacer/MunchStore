package com.sugarmunch.app.ui.screens

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sugarmunch.app.data.PreferencesRepository
import com.sugarmunch.app.effects.fab.FabConfigurationManager
import com.sugarmunch.app.effects.special.SpecialEffectsManager
import com.sugarmunch.app.service.OverlayService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import android.content.Context
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: PreferencesRepository
) : ViewModel() {

    private val _overlayEnabled = MutableStateFlow(false)
    val overlayEnabled: StateFlow<Boolean> = _overlayEnabled.asStateFlow()

    private val _hasOverlayPermission = MutableStateFlow(false)
    val hasOverlayPermission: StateFlow<Boolean> = _hasOverlayPermission.asStateFlow()

    private val _reduceMotion = MutableStateFlow(false)
    val reduceMotion: StateFlow<Boolean> = _reduceMotion.asStateFlow()

    private val _catalogGridColumns = MutableStateFlow(2)
    val catalogGridColumns: StateFlow<Int> = _catalogGridColumns.asStateFlow()
    private val _catalogDefaultView = MutableStateFlow("list")
    val catalogDefaultView: StateFlow<String> = _catalogDefaultView.asStateFlow()
    private val _catalogCardStyle = MutableStateFlow("default")
    val catalogCardStyle: StateFlow<String> = _catalogCardStyle.asStateFlow()

    // Special Effects
    val fabConfigurationManager = FabConfigurationManager.getInstance(context)
    val specialEffectsManager = SpecialEffectsManager.getInstance(context)
    
    val fabConfiguration = fabConfigurationManager.configuration
    val selectedFabEffects = fabConfigurationManager.selectedEffects
    val fabEffectCount = fabConfigurationManager.selectionCount
    
    val activeSpecialEffects = specialEffectsManager.activeEffectIds
    val activeEffectCount = specialEffectsManager.activeEffectCount

    init {
        prefs.overlayEnabled.onEach { _overlayEnabled.value = it }.launchIn(viewModelScope)
        prefs.reduceMotion.onEach { _reduceMotion.value = it }.launchIn(viewModelScope)
        prefs.catalogGridColumns.onEach { _catalogGridColumns.value = it }.launchIn(viewModelScope)
        prefs.catalogDefaultView.onEach { _catalogDefaultView.value = it }.launchIn(viewModelScope)
        prefs.catalogCardStyle.onEach { _catalogCardStyle.value = it }.launchIn(viewModelScope)
        viewModelScope.launch {
            _hasOverlayPermission.value = android.provider.Settings.canDrawOverlays(context)
        }
    }

    fun setOverlayEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setOverlayEnabled(enabled)
            _overlayEnabled.value = enabled
            if (enabled) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(Intent(context, OverlayService::class.java))
                } else {
                    context.startService(Intent(context, OverlayService::class.java))
                }
            } else {
                context.stopService(Intent(context, OverlayService::class.java))
            }
        }
    }

    fun setReduceMotion(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setReduceMotion(enabled)
            _reduceMotion.value = enabled
        }
    }

    fun setCatalogGridColumns(columns: Int) {
        viewModelScope.launch {
            prefs.setCatalogGridColumns(columns)
            _catalogGridColumns.value = columns
        }
    }

    fun setCatalogDefaultView(view: String) {
        viewModelScope.launch {
            prefs.setCatalogDefaultView(view)
            _catalogDefaultView.value = view
        }
    }

    fun setCatalogCardStyle(style: String) {
        viewModelScope.launch {
            prefs.setCatalogCardStyle(style)
            _catalogCardStyle.value = style
        }
    }
}
