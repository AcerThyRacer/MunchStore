package com.sugarmunch.tv.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.NavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.NavigationDrawerItemDefaults
import androidx.tv.material3.NavigationDrawerScope
import androidx.tv.material3.Text
import com.sugarmunch.tv.navigation.TvNavDestination
import com.sugarmunch.tv.ui.TvCatalogScreen
import com.sugarmunch.tv.ui.TvDetailScreen
import com.sugarmunch.tv.ui.TvEffectsScreen
import com.sugarmunch.tv.ui.TvSearchScreen
import com.sugarmunch.tv.ui.TvSettingsScreen
import com.sugarmunch.tv.ui.TvThemeScreen
import com.sugarmunch.tv.ui.theme.TvTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for Android TV interface
 * Features:
 * - Leanback theme with proper TV navigation
 * - Side navigation drawer with D-pad support
 * - BrowseFragment-style layout with rows
 * - Focus management for remote control
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: TvMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TvTheme {
                SugarMunchTvApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SugarMunchTvApp(
    viewModel: TvMainViewModel
) {
    var selectedDestination by remember { mutableStateOf<TvNavDestination>(TvNavDestination.Catalog) }
    var selectedAppId by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        TvNavigationDrawer(
            selectedDestination = selectedDestination,
            onDestinationSelected = { destination ->
                selectedDestination = destination
                selectedAppId = null // Reset detail view when switching tabs
            }
        ) {
            // Main content area
            TvContent(
                destination = selectedDestination,
                selectedAppId = selectedAppId,
                onAppSelected = { appId ->
                    selectedAppId = appId
                },
                onBackFromDetail = {
                    selectedAppId = null
                },
                viewModel = viewModel,
                modifier = Modifier.focusRequester(focusRequester)
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvNavigationDrawer(
    selectedDestination: TvNavDestination,
    onDestinationSelected: (TvNavDestination) -> Unit,
    content: @Composable () -> Unit
) {
    NavigationDrawer(
        drawerContent = {
            TvNavigationDrawerContent(
                selectedDestination = selectedDestination,
                onDestinationSelected = onDestinationSelected
            )
        }
    ) {
        content()
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun NavigationDrawerScope.TvNavigationDrawerContent(
    selectedDestination: TvNavDestination,
    onDestinationSelected: (TvNavDestination) -> Unit
) {
    val destinations = TvNavDestination.entries.filter { it.visibleInDrawer }

    destinations.forEach { destination ->
        NavigationDrawerItem(
            selected = selectedDestination == destination,
            onClick = { onDestinationSelected(destination) },
            leadingContent = {
                destination.icon?.let { icon ->
                    androidx.tv.material3.Icon(
                        imageVector = icon,
                        contentDescription = destination.label
                    )
                }
            },
            content = { Text(text = destination.label) },
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                selectedContentColor = MaterialTheme.colorScheme.primary,
                focusedSelectedContainerColor = MaterialTheme.colorScheme.primary,
                focusedSelectedContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    }
}

@Composable
fun TvContent(
    destination: TvNavDestination,
    selectedAppId: String?,
    onAppSelected: (String) -> Unit,
    onBackFromDetail: () -> Unit,
    viewModel: TvMainViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp)
    ) {
        when {
            // Show detail screen if an app is selected
            selectedAppId != null -> {
                TvDetailScreen(
                    appId = selectedAppId,
                    onBack = onBackFromDetail,
                    viewModel = viewModel
                )
            }
            // Otherwise show the main destination content
            else -> {
                when (destination) {
                    TvNavDestination.Catalog -> TvCatalogScreen(
                        onAppSelected = onAppSelected,
                        viewModel = viewModel
                    )
                    TvNavDestination.Search -> TvSearchScreen(
                        onAppSelected = onAppSelected,
                        viewModel = viewModel
                    )
                    TvNavDestination.Effects -> TvEffectsScreen(
                        viewModel = viewModel
                    )
                    TvNavDestination.Themes -> TvThemeScreen(
                        viewModel = viewModel
                    )
                    TvNavDestination.Settings -> TvSettingsScreen(
                        viewModel = viewModel
                    )
                    else -> TvCatalogScreen(
                        onAppSelected = onAppSelected,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
