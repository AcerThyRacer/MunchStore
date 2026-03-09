package com.sugarmunch.app.ui.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Single source of truth for all spacing, sizing, and layout tokens.
 * Built on a 4dp base grid for visual consistency across the app.
 */
object SugarDimens {
    val gridUnit = 4.dp

    object Spacing {
        val none = 0.dp
        val xxxs = 2.dp
        val xxs = 4.dp
        val xs = 8.dp
        val sm = 12.dp
        val md = 16.dp
        val lg = 20.dp
        val xl = 24.dp
        val xxl = 32.dp
        val xxxl = 48.dp
        val huge = 64.dp
    }

    object Elevation {
        val none = 0.dp
        val subtle = 1.dp
        val low = 2.dp
        val medium = 4.dp
        val high = 8.dp
        val highest = 16.dp
    }

    object Radius {
        val none = 0.dp
        val xs = 4.dp
        val sm = 8.dp
        val md = 12.dp
        val lg = 16.dp
        val xl = 24.dp
        val pill = 50.dp
        val circle = 999.dp
    }

    object IconSize {
        val xs = 16.dp
        val sm = 20.dp
        val md = 24.dp
        val lg = 32.dp
        val xl = 40.dp
        val xxl = 48.dp
        val hero = 64.dp
    }

    object TouchTarget {
        val minimum = 44.dp
        val standard = 48.dp
        val large = 56.dp
    }

    object Brand {
        // SugarMunch signature gradient colors
        val hotPink = Color(0xFFFF69B4)
        val mint = Color(0xFF00FFA3)
        val yellow = Color(0xFFFFD700)
        val deepPurple = Color(0xFF1A1A2E)
        val candyOrange = Color(0xFFFFA500)
        val bubblegumBlue = Color(0xFF00BFFF)
    }

    object Height {
        val button = 48.dp
        val buttonSmall = 36.dp
        val buttonLarge = 56.dp
        val topBar = 64.dp
        val bottomNav = 64.dp
        val listItem = 56.dp
        val listItemLarge = 72.dp
        val chip = 32.dp
        val textField = 56.dp
        val fab = 56.dp
        val fabSmall = 40.dp
    }

    object MaxWidth {
        val compact = 600.dp
        val medium = 840.dp
        val expanded = 1200.dp
    }
}
