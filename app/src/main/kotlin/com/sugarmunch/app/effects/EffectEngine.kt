package com.sugarmunch.app.effects

import android.content.Context
import android.view.WindowManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object EffectEngine {
    private val _activeEffects = MutableStateFlow<Set<String>>(emptySet())
    val activeEffects: StateFlow<Set<String>> = _activeEffects.asStateFlow()

    private val registry = listOf(
        SugarrushEffect,
        RainbowTintEffect,
        MintWashEffect,
        CaramelDimEffect,
        CandyConfettiEffect,
        HeartbeatHapticEffect
    )

    fun allEffects(): List<Effect> = registry

    fun isEnabled(effectId: String): Boolean = effectId in _activeEffects.value

    fun toggle(context: Context, windowManager: WindowManager, effect: Effect) {
        if (effect.isActive()) {
            effect.disable()
            _activeEffects.value = _activeEffects.value - effect.id
        } else {
            effect.enable(context, windowManager)
            _activeEffects.value = _activeEffects.value + effect.id
        }
    }
}
