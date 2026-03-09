package com.sugarmunch.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sugarmunch.app.data.AppEntry
import com.sugarmunch.app.data.CandyTrailEntry
import com.sugarmunch.app.data.ManifestRepository
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.ui.screens.CandyAppCardV2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrailDetailScreen(
    trailId: String,
    onBack: () -> Unit,
    onAppClick: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: TrailDetailViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return TrailDetailViewModel(ManifestRepository(context), trailId) as T
            }
        }
    )
    val trail by viewModel.trail.collectAsState()
    val apps by viewModel.apps.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)

    LaunchedEffect(trailId) {
        viewModel.load()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        trail?.name ?: "Candy Trail",
                        color = colors.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedThemeBackground()
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.primary)
                }
            } else if (trail == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Trail not found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurface
                    )
                }
            } else {
                val t = trail!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "trail_header") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = colors.surface.copy(alpha = 0.95f)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(colors.primary, colors.secondary)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        t.icon,
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        t.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = colors.onSurface
                                    )
                                    Text(
                                        t.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.onSurface.copy(alpha = 0.8f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    item(key = "apps_header") {
                        Text(
                            "Apps on this trail",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(apps, key = { it.id }) { app ->
                        CandyAppCardV2(
                            app = app,
                            colors = colors,
                            cardStyle = "default",
                            accentOverride = null,
                            intensity = 1f,
                            onClick = { onAppClick(app.id) }
                        )
                    }
                }
            }
        }
    }
}

class TrailDetailViewModel(
    private val repository: ManifestRepository,
    private val trailId: String
) : ViewModel() {
    private val _trail = kotlinx.coroutines.flow.MutableStateFlow<CandyTrailEntry?>(null)
    val trail: kotlinx.coroutines.flow.StateFlow<CandyTrailEntry?> = _trail

    private val _apps = kotlinx.coroutines.flow.MutableStateFlow<List<AppEntry>>(emptyList())
    val apps: kotlinx.coroutines.flow.StateFlow<List<AppEntry>> = _apps

    private val _isLoading = kotlinx.coroutines.flow.MutableStateFlow(true)
    val isLoading: kotlinx.coroutines.flow.StateFlow<Boolean> = _isLoading

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.fetchApps().onSuccess { allApps ->
                val trails = repository.getTrails()
                _trail.value = trails.find { it.id == trailId }
                _trail.value?.let { t ->
                    _apps.value = allApps.filter { it.id in t.appIds }
                }
            }
            _isLoading.value = false
        }
    }
}
