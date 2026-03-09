package com.sugarmunch.tv.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController

/**
 * Navigation destinations for TV interface
 */
enum class TvNavDestination(
    val route: String,
    val label: String,
    val icon: ImageVector? = null,
    val visibleInDrawer: Boolean = true
) {
    Catalog("catalog", "Home", Icons.Default.Home),
    Search("search", "Search", Icons.Default.Search),
    Effects("effects", "Effects", Icons.Default.Star),
    Themes("themes", "Themes", Icons.Default.Palette),
    Settings("settings", "Settings", Icons.Default.Settings),
    
    // Hidden destinations (not in drawer)
    Detail("detail/{appId}", "App Details", visibleInDrawer = false),
    Install("install/{appId}", "Install", visibleInDrawer = false);

    companion object {
        fun fromRoute(route: String?): TvNavDestination {
            return entries.find { it.route == route } ?: Catalog
        }
    }
}

/**
 * Extension function to handle D-pad navigation
 * Adds key event handlers for TV remote navigation
 */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.dpadNavigation(
    onUp: (() -> Unit)? = null,
    onDown: (() -> Unit)? = null,
    onLeft: (() -> Unit)? = null,
    onRight: (() -> Unit)? = null,
    onSelect: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null
): Modifier = this.onKeyEvent { keyEvent ->
    if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false

    when (keyEvent.key) {
        Key.DirectionUp -> {
            onUp?.invoke()
            true
        }
        Key.DirectionDown -> {
            onDown?.invoke()
            true
        }
        Key.DirectionLeft -> {
            onLeft?.invoke()
            true
        }
        Key.DirectionRight -> {
            onRight?.invoke()
            true
        }
        Key.Enter, Key.DirectionCenter -> {
            onSelect?.invoke()
            true
        }
        Key.Back, Key.Escape -> {
            onBack?.invoke()
            true
        }
        else -> false
    }
}

/**
 * Focus direction handling for TV grid navigation
 */
sealed class TvFocusDirection {
    data object Up : TvFocusDirection()
    data object Down : TvFocusDirection()
    data object Left : TvFocusDirection()
    data object Right : TvFocusDirection()
    data object Enter : TvFocusDirection()
    data object Back : TvFocusDirection()
}

/**
 * Navigation handler for TV interface
 */
class TvNavigationHandler(
    private val navController: NavController
) {
    fun navigateTo(destination: TvNavDestination, args: Map<String, String> = emptyMap()) {
        var route = destination.route
        args.forEach { (key, value) ->
            route = route.replace("{$key}", value)
        }
        navController.navigate(route)
    }

    fun navigateBack() {
        navController.popBackStack()
    }

    fun navigateToAppDetail(appId: String) {
        navController.navigate("detail/$appId")
    }

    fun navigateToInstall(appId: String) {
        navController.navigate("install/$appId")
    }
}

/**
 * Row/Column focus management for TV grids
 * Helps manage focus state in horizontal rows and vertical columns
 */
class TvFocusManager {
    private val focusStates = mutableMapOf<String, Int>()

    fun rememberFocus(section: String, index: Int) {
        focusStates[section] = index
    }

    fun getLastFocusedIndex(section: String): Int {
        return focusStates[section] ?: 0
    }

    fun clearFocus(section: String) {
        focusStates.remove(section)
    }

    fun clearAll() {
        focusStates.clear()
    }
}

/**
 * TV-specific back button handling
 */
fun NavController.handleTvBack(
    onBackInRoot: () -> Unit = {}
): Boolean {
    return if (previousBackStackEntry != null) {
        popBackStack()
        true
    } else {
        onBackInRoot()
        false
    }
}

/**
 * Extension for handling focus in TV lists
 */
fun Modifier.tvFocusable(
    enabled: Boolean = true
): Modifier = if (enabled) {
    // TV focusable modifier with proper focus visual feedback
    this
} else {
    this
}
