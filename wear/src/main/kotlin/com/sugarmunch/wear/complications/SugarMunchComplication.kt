package com.sugarmunch.wear.complications

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.sugarmunch.wear.R
import com.sugarmunch.wear.presentation.MainActivity

/**
 * SugarMunch Complication Service
 * 
 * Shows active effect count on watch face and allows quick access to the app.
 * Supports SHORT_TEXT, LONG_TEXT, RANGED_VALUE, and ICON complication types.
 */
class SugarMunchComplicationService : SuspendingComplicationDataSourceService() {

    companion object {
        private const val MAX_EFFECTS = 18
        
        // Keys for shared preferences to store state
        private const val PREFS_NAME = "sugarmunch_complication_prefs"
        private const val KEY_ACTIVE_COUNT = "active_effect_count"
        private const val KEY_BOOST_MODE = "boost_mode"
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        // Get current state from preferences (updated via Data Layer)
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val activeCount = prefs.getInt(KEY_ACTIVE_COUNT, 0)
        val boostMode = prefs.getBoolean(KEY_BOOST_MODE, false)

        return when (request.complicationType) {
            ComplicationType.SHORT_TEXT -> createShortTextComplication(activeCount, boostMode)
            ComplicationType.LONG_TEXT -> createLongTextComplication(activeCount, boostMode)
            ComplicationType.RANGED_VALUE -> createRangedValueComplication(activeCount, boostMode)
            ComplicationType.MONOCHROMATIC_IMAGE -> createIconComplication(boostMode)
            ComplicationType.SMALL_IMAGE -> createSmallImageComplication(boostMode)
            else -> null
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        // Preview data shown in complication picker
        return when (type) {
            ComplicationType.SHORT_TEXT -> createShortTextComplication(5, false)
            ComplicationType.LONG_TEXT -> createLongTextComplication(5, false)
            ComplicationType.RANGED_VALUE -> createRangedValueComplication(5, false)
            ComplicationType.MONOCHROMATIC_IMAGE -> createIconComplication(false)
            ComplicationType.SMALL_IMAGE -> createSmallImageComplication(false)
            else -> null
        }
    }

    /**
     * Create short text complication (most common)
     * Shows: [Candy Icon] 5 effects
     */
    private fun createShortTextComplication(activeCount: Int, boostMode: Boolean): ShortTextComplicationData {
        val tapAction = createTapAction()
        
        val icon = if (boostMode) "🚀" else "🍬"
        val text = if (activeCount > 0) "$activeCount" else "--"
        
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text).build(),
            contentDescription = PlainComplicationText.Builder(
                if (activeCount > 0) "$activeCount effects active" else "No effects active"
            ).build()
        )
            .setTitle(
                PlainComplicationText.Builder(icon).build()
            )
            .setTapAction(tapAction)
            .build()
    }

    /**
     * Create long text complication
     * Shows: SugarMunch - 5 effects active
     */
    private fun createLongTextComplication(activeCount: Int, boostMode: Boolean): LongTextComplicationData {
        val tapAction = createTapAction()
        
        val title = if (boostMode) "🚀 SugarMunch" else "🍬 SugarMunch"
        val text = when {
            activeCount == 0 -> "No effects active"
            activeCount == 1 -> "1 effect active"
            else -> "$activeCount effects active"
        }
        
        return LongTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text).build(),
            contentDescription = PlainComplicationText.Builder("SugarMunch: $text").build()
        )
            .setTitle(PlainComplicationText.Builder(title).build())
            .setTapAction(tapAction)
            .build()
    }

    /**
     * Create ranged value complication (progress bar style)
     * Shows: [=====>    ] 5/18
     */
    private fun createRangedValueComplication(activeCount: Int, boostMode: Boolean): RangedValueComplicationData {
        val tapAction = createTapAction()
        
        val value = activeCount.toFloat()
        val max = MAX_EFFECTS.toFloat()
        
        val text = when {
            activeCount == 0 -> "No effects"
            activeCount == 1 -> "1 effect"
            else -> "$activeCount effects"
        }
        
        val title = if (boostMode) "🚀 Boost" else "🍬 SugarMunch"
        
        return RangedValueComplicationData.Builder(
            value = value,
            min = 0f,
            max = max,
            contentDescription = PlainComplicationText.Builder("$activeCount of $MAX_EFFECTS effects active").build()
        )
            .setText(PlainComplicationText.Builder(text).build())
            .setTitle(PlainComplicationText.Builder(title).build())
            .setTapAction(tapAction)
            .build()
    }

    /**
     * Create icon-only complication
     */
    private fun createIconComplication(boostMode: Boolean): ShortTextComplicationData {
        val tapAction = createTapAction()
        val icon = if (boostMode) "🚀" else "🍬"
        
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(icon).build(),
            contentDescription = PlainComplicationText.Builder("SugarMunch").build()
        )
            .setTapAction(tapAction)
            .build()
    }

    /**
     * Create small image complication
     */
    private fun createSmallImageComplication(boostMode: Boolean): SmallImageComplicationData {
        val tapAction = createTapAction()
        
        // Use a colored placeholder - in production, use actual app icon
        val imageRes = if (boostMode) R.drawable.ic_candy_boost else R.drawable.ic_candy
        
        return SmallImageComplicationData.Builder(
            smallImage = SmallImage.Builder(
                image = Icon.createWithResource(this, imageRes),
                type = SmallImageType.ICON
            ).build(),
            contentDescription = PlainComplicationText.Builder("SugarMunch").build()
        )
            .setTapAction(tapAction)
            .build()
    }

    /**
     * Create pending intent to open the app when complication is tapped
     */
    private fun createTapAction(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

/**
 * Extension function to update complication data from Data Layer
 * Call this when effect count changes
 */
fun updateComplicationData(context: android.content.Context, activeCount: Int, boostMode: Boolean) {
    val prefs = context.getSharedPreferences("sugarmunch_complication_prefs", MODE_PRIVATE)
    prefs.edit()
        .putInt("active_effect_count", activeCount)
        .putBoolean("boost_mode", boostMode)
        .apply()
    
    // Request complication update
    androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
        .create(
            context = context,
            providerComponent = android.content.ComponentName(context, SugarMunchComplicationService::class.java)
        )
        .requestUpdate()
}
