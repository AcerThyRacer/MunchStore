package com.sugarmunch.wear.tiles

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.Box
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.Row
import androidx.wear.protolayout.LayoutElementBuilders.Text
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.TypeBuilders
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.ButtonColors
import androidx.wear.protolayout.material.Chip
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import androidx.wear.tiles.tooling.preview.PreviewParameter
import androidx.wear.tiles.tooling.preview.TilePreviewData
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.sugarmunch.wear.data.WearDataLayer
import kotlinx.coroutines.async
import kotlinx.coroutines.guava.asListenableFuture

/**
 * SugarMunch Tile Service
 * 
 * Provides quick action buttons directly on the watch face carousel.
 * Shows current effect count and quick toggles for common actions.
 */
class SugarMunchTileService : TileService() {

    companion object {
        // Action IDs
        private const val ACTION_TOGGLE_BOOST = "toggle_boost"
        private const val ACTION_ALL_OFF = "all_off"
        private const val ACTION_OPEN_APP = "open_app"
        private const val ACTION_PRESET_CHILL = "preset_chill"
        private const val ACTION_PRESET_PARTY = "preset_party"
        
        // Colors
        private const val COLOR_PRIMARY = 0xFFFF1493.toInt()
        private const val COLOR_SECONDARY = 0xFF00CED1.toInt()
        private const val COLOR_BACKGROUND = 0xFF000000.toInt()
        private const val COLOR_SURFACE = 0xFF1A1A1A.toInt()
        private const val COLOR_ON_SURFACE = 0xFFFFFFFF.toInt()
        private const val COLOR_ERROR = 0xFFFF6B6B.toInt()
    }

    override fun onTileRequest(
        requestParams: RequestBuilders.TileRequest
    ): ListenableFuture<TileBuilders.Tile> {
        return lifecycleScope.async {
            // Get current state
            val wearDataLayer = WearDataLayer(this@SugarMunchTileService)
            val activeCount = wearDataLayer.activeEffectCount.value
            val boostMode = wearDataLayer.boostMode.value
            
            buildTile(activeCount, boostMode)
        }.asListenableFuture()
    }

    override fun onResourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ListenableFuture<ResourceBuilders.Resources> {
        return Futures.immediateFuture(
            ResourceBuilders.Resources.Builder()
                .setVersion("1")
                .build()
        )
    }

    /**
     * Build the tile layout
     */
    private fun buildTile(activeCount: Int, boostMode: Boolean): TileBuilders.Tile {
        return TileBuilders.Tile.Builder()
            .setResourcesVersion("1")
            .setFreshnessIntervalMillis(60000) // Refresh every minute
            .setTimeline(
                TimelineBuilders.Timeline.Builder()
                    .addTimelineEntry(
                        TimelineBuilders.TimelineEntry.Builder()
                            .setLayout(
                                LayoutElementBuilders.Layout.Builder()
                                    .setRoot(createTileLayout(activeCount, boostMode))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }

    /**
     * Create the main tile layout
     */
    private fun createTileLayout(activeCount: Int, boostMode: Boolean): LayoutElementBuilders.LayoutElement {
        return Box.Builder()
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setBackground(
                        ModifiersBuilders.Background.Builder()
                            .setColor(ColorBuilders.ColorProp.Builder(COLOR_BACKGROUND).build())
                            .build()
                    )
                    .build()
            )
            .addContent(
                Column.Builder()
                    .addContent(createHeader(activeCount, boostMode))
                    .addContent(createActionButtons())
                    .addContent(createPresetButtons())
                    .addContent(createOpenAppButton())
                    .build()
            )
            .build()
    }

    /**
     * Create the header with effect count
     */
    private fun createHeader(activeCount: Int, boostMode: Boolean): LayoutElementBuilders.LayoutElement {
        val statusText = when {
            boostMode -> "🚀 BOOST MODE"
            activeCount > 0 -> "✨ $activeCount effects active"
            else -> "😴 No effects"
        }
        
        val statusColor = when {
            boostMode -> COLOR_PRIMARY
            activeCount > 0 -> COLOR_SECONDARY
            else -> 0xFFB0B0B0.toInt()
        }

        return Column.Builder()
            .addContent(
                Text.Builder(this, "🍬 SugarMunch")
                    .setTypography(Typography.TYPOGRAPHY_TITLE3)
                    .setColor(ColorBuilders.ColorProp.Builder(COLOR_PRIMARY).build())
                    .build()
            )
            .addContent(
                Text.Builder(this, statusText)
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(ColorBuilders.ColorProp.Builder(statusColor).build())
                    .build()
            )
            .build()
    }

    /**
     * Create main action buttons row
     */
    private fun createActionButtons(): LayoutElementBuilders.LayoutElement {
        return Row.Builder()
            .addContent(
                Button.Builder(this, "🚀", createAction(ACTION_TOGGLE_BOOST))
                    .setButtonColors(
                        ButtonColors(
                            COLOR_PRIMARY,
                            COLOR_ON_SURFACE
                        )
                    )
                    .setSize(DimensionBuilders.dp(48f), DimensionBuilders.dp(48f))
                    .build()
            )
            .addContent(
                Button.Builder(this, "🛑", createAction(ACTION_ALL_OFF))
                    .setButtonColors(
                        ButtonColors(
                            COLOR_ERROR,
                            COLOR_ON_SURFACE
                        )
                    )
                    .setSize(DimensionBuilders.dp(48f), DimensionBuilders.dp(48f))
                    .build()
            )
            .build()
    }

    /**
     * Create preset buttons
     */
    private fun createPresetButtons(): LayoutElementBuilders.LayoutElement {
        return Column.Builder()
            .addContent(
                CompactChip.Builder(this, "😌 Chill", createAction(ACTION_PRESET_CHILL), this)
                    .setChipColors(
                        ChipColors(
                            COLOR_SURFACE,
                            COLOR_ON_SURFACE
                        )
                    )
                    .build()
            )
            .addContent(
                CompactChip.Builder(this, "🎉 Party", createAction(ACTION_PRESET_PARTY), this)
                    .setChipColors(
                        ChipColors(
                            COLOR_SURFACE,
                            COLOR_ON_SURFACE
                        )
                    )
                    .build()
            )
            .build()
    }

    /**
     * Create open app button
     */
    private fun createOpenAppButton(): LayoutElementBuilders.LayoutElement {
        return CompactChip.Builder(this, "Open App", createAction(ACTION_OPEN_APP), this)
            .setChipColors(
                ChipColors(
                    COLOR_SECONDARY,
                    COLOR_ON_SURFACE
                )
            )
            .build()
    }

    /**
     * Create an action for the given action ID
     */
    private fun createAction(actionId: String): ActionBuilders.LaunchAction {
        return ActionBuilders.LaunchAction.Builder()
            .setAndroidActivity(
                ActionBuilders.AndroidActivity.Builder()
                    .setPackageName(packageName)
                    .setClassName("com.sugarmunch.wear.presentation.MainActivity")
                    .addKeyToExtraMapping(
                        "action",
                        ActionBuilders.stringExtra(actionId)
                    )
                    .build()
            )
            .build()
    }
}

/**
 * Preview function for tile layout
 */
@Preview(device = androidx.wear.tiles.tooling.preview.WearDevices.SMALL_ROUND)
fun tilePreview(@PreviewParameter(SugarMunchTilePreviewDataProvider::class) data: TilePreviewData) {
    // Preview is handled by Android Studio Tile Preview
}

/**
 * Preview data provider
 */
class SugarMunchTilePreviewDataProvider : androidx.wear.tiles.tooling.preview.PreviewParameterProvider<TilePreviewData> {
    override val values = sequenceOf(
        TilePreviewData(
            tile = TileBuilders.Tile.Builder()
                .setResourcesVersion("1")
                .setTimeline(
                    TimelineBuilders.Timeline.Builder()
                        .addTimelineEntry(
                            TimelineBuilders.TimelineEntry.Builder()
                                .setLayout(
                                    LayoutElementBuilders.Layout.Builder()
                                        .setRoot(
                                            Column.Builder()
                                                .addContent(
                                                    Text.Builder(null as Context?, "🍬 SugarMunch")
                                                        .build()
                                                )
                                                .build()
                                        )
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build()
        )
    )
}
