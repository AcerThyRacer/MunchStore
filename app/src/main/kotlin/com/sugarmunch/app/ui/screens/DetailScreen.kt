package com.sugarmunch.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sugarmunch.app.bounties.BountyRepository
import com.sugarmunch.app.data.LocalPreferencesRepository
import com.sugarmunch.app.phaseone.PhaseOneUtilities
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.components.ThemeAwareCard
import com.sugarmunch.app.theme.engine.ThemeRuntimeSnapshot
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.theme.presets.ThemePresets
import com.sugarmunch.app.theme.profile.toThemeProfile
import com.sugarmunch.app.ui.components.Haptics
import com.sugarmunch.app.ui.typography.toDynamicTypographyConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    appId: String,
    onBack: () -> Unit,
    onPreviewClick: () -> Unit = {},
    onUtilityStudioClick: (String) -> Unit = {},
    viewModel: DetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = DetailViewModelFactory(appId, LocalContext.current)
    )
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    
    // Theme integration
    val themeManager = remember { ThemeManager.getInstance(context) }
    val fallbackTheme = remember { ThemePresets.getDefault() }
    val runtime by themeManager.observeThemeRuntime(appId).collectAsState(
        initial = ThemeRuntimeSnapshot(
            profile = fallbackTheme.toThemeProfile(),
            theme = fallbackTheme,
            colors = fallbackTheme.getColorsForIntensity(1f),
            typography = fallbackTheme.toThemeProfile().typography.toDynamicTypographyConfig(context, emptyList()),
            themeIntensity = 1f,
            backgroundIntensity = 1f,
            particleIntensity = 1f,
            animationIntensity = 1f
        )
    )
    val colors = runtime.colors

    LaunchedEffect(appId) {
        viewModel.loadApp()
    }
    
    LaunchedEffect(state) {
        if (state is DetailState.Success) {
            val successState = state as DetailState.Success
            if (successState.installComplete) {
                showSuccessDialog = true
                dialogMessage = "${successState.app.name} has been installed successfully!"
                themeManager.boostIntensity(3000, 0.5f)
                Haptics.performSuccess(context)
            }
            if (successState.installError != null) {
                showErrorDialog = true
                dialogMessage = successState.installError
                Haptics.performError(context)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "App details",
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
                    containerColor = Color.Transparent,
                    titleContentColor = colors.onSurface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Dynamic theme background
            AnimatedThemeBackground(appId = appId)
            
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                colors.background.copy(alpha = 0.95f),
                                colors.background.copy(alpha = 0.85f)
                            )
                        )
                    )
            )
            
            when (val s = state) {
                is DetailState.Loading -> {
                    DetailLoadingState(colors = colors)
                }
                is DetailState.Error -> {
                    DetailErrorState(
                        message = s.message,
                        colors = colors
                    )
                }
                is DetailState.Success -> {
                    DetailSuccessState(
                        state = s,
                        colors = colors,
                        animIntensity = runtime.animationIntensity,
                        onDownloadClick = { viewModel.downloadAndInstall() },
                        onPreviewClick = onPreviewClick,
                        onUtilityStudioClick = onUtilityStudioClick,
                        context = context
                    )
                }
            }
        }
    }
    
    // Sweet success dialog
    SweetSuccessDialog(
        showDialog = showSuccessDialog,
        type = SweetDialogType.SUCCESS,
        title = "Sweet Success!",
        message = dialogMessage,
        buttonText = "Yay! \uD83C\uDF6C",
        onDismiss = {
            showSuccessDialog = false
            viewModel.resetInstallState()
        }
    )
    
    // Error dialog
    SweetSuccessDialog(
        showDialog = showErrorDialog,
        type = SweetDialogType.ERROR,
        title = "Oops!",
        message = dialogMessage,
        buttonText = "Okay",
        onDismiss = {
            showErrorDialog = false
            viewModel.resetInstallState()
        }
    )
    
    // Confetti celebration (theme colors)
    CandyConfettiOverlay(
        show = showSuccessDialog,
        colors = listOf(colors.primary, colors.secondary, colors.tertiary, colors.accent)
    )
}

@Composable
private fun DetailLoadingState(colors: com.sugarmunch.app.theme.model.AdjustedColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = colors.primary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Loading candy...",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface
        )
    }
}

@Composable
private fun DetailErrorState(
    message: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.error
        )
    }
}

@Composable
private fun DetailSuccessState(
    state: DetailState.Success,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    animIntensity: Float,
    onDownloadClick: () -> Unit,
    onPreviewClick: () -> Unit,
    onUtilityStudioClick: (String) -> Unit,
    context: android.content.Context
) {
    val app = state.app
    val displayApp = remember(app) { PhaseOneUtilities.enrichApp(app) }
    val phaseOneSpec = remember(displayApp.id) { PhaseOneUtilities.specFor(displayApp.id) }
    val prefs = LocalPreferencesRepository.current
    val accentOverride by prefs.getAppAccent(app.id).collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val themeManager = remember { ThemeManager.getInstance(context) }
    val heroColors = phaseOneSpec?.heroColors ?: listOf(
        colors.primary.copy(alpha = 0.4f),
        colors.secondary.copy(alpha = 0.3f),
        colors.tertiary.copy(alpha = 0.2f)
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ThemeAwareCard(
            intensity = animIntensity,
            pulseEnabled = true
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Hero gradient strip behind icon
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = heroColors.mapIndexed { index, color ->
                                        color.copy(alpha = 0.2f + (index * 0.1f))
                                    }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // App icon with gradient background
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = heroColors + listOf(heroColors.first())
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                        if (displayApp.iconUrl != null && phaseOneSpec == null) {
                            AsyncImage(
                                model = displayApp.iconUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(
                                phaseOneSpec?.emojiIcon ?: "\uD83C\uDF6C",
                                style = MaterialTheme.typography.displayMedium
                            )
                        }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        displayApp.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    phaseOneSpec?.let { spec ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = spec.shortTagline,
                            style = MaterialTheme.typography.bodyMedium,
                            color = heroColors.first()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        "v${displayApp.version}",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        displayApp.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface.copy(alpha = 0.8f)
                    )
                    
                    displayApp.source?.let { source ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Source: $source",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    
                    displayApp.screenshots?.takeIf { it.isNotEmpty() }?.let { screenshots ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Screenshots",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            screenshots.forEach { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(120.dp, 200.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .shadow(2.dp, RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                    
                    // Set accent for this app
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Card accent",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            null to "Default",
                            colors.primary to "Primary",
                            colors.secondary to "Secondary",
                            colors.accent to "Accent"
                        ).forEach { (color, label) ->
                            val hex = color?.let { c ->
                                     "#%02X%02X%02X".format(
                                         (c.red * 255).toInt(),
                                         (c.green * 255).toInt(),
                                         (c.blue * 255).toInt()
                                     )
                                 }
                             Surface(
                                 shape = RoundedCornerShape(12.dp),
                                 color = color?.copy(alpha = 0.3f) ?: colors.surfaceVariant,
                                 onClick = {
                                     scope.launch {
                                        prefs.setAppAccent(displayApp.id, hex)
                                        Haptics.performTick(context)
                                     }
                                 }
                             ) {
                                Text(
                                    label,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurface
                                )
                            }
                        }
                    }

                    displayApp.components?.takeIf { it.isNotEmpty() }?.let { components ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Utility tags",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            components.take(3).forEachIndexed { index, component ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = heroColors[index % heroColors.size].copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = component.replace('-', ' '),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (!displayApp.previewUrl.isNullOrBlank() || phaseOneSpec != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!displayApp.previewUrl.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = onPreviewClick,
                        modifier = if (phaseOneSpec != null) Modifier.weight(1f) else Modifier.fillMaxWidth()
                    ) {
                        Text("Try preview")
                    }
                }
                if (phaseOneSpec != null) {
                    Button(
                        onClick = { onUtilityStudioClick(displayApp.id) },
                        modifier = if (!displayApp.previewUrl.isNullOrBlank()) Modifier.weight(1f) else Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open ${phaseOneSpec.studioTitle}")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        phaseOneSpec?.let { spec ->
            PhaseOneUtilityFeatureSection(
                spec = spec,
                colors = colors,
                onThemeSelected = { themeId -> themeManager.setThemeById(themeId) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Developer bounties
        DetailBountiesSection(appId = displayApp.id, colors = colors, context = context)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Download progress
        if (state.downloadProgress != null) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Downloading...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface
                        )
                        Text(
                            "${(state.downloadProgress!! * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { state.downloadProgress!! },
                        modifier = Modifier.fillMaxWidth(),
                        color = colors.primary,
                        trackColor = colors.surfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Hero CTA: gradient-style button with soft glow
        Button(
            onClick = onDownloadClick,
            enabled = !state.installing,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = colors.primary.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                disabledContainerColor = colors.primary.copy(alpha = 0.5f)
            )
        ) {
            if (state.installing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = colors.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Installing...")
            } else {
                Icon(
                    Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Download & Install")
            }
        }
    }
}

@Composable
private fun PhaseOneUtilityFeatureSection(
    spec: com.sugarmunch.app.phaseone.PhaseOneUtilitySpec,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onThemeSelected: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.95f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Phase 1 utility pack",
                style = MaterialTheme.typography.titleMedium,
                color = colors.primary
            )
            Text(
                text = "Visual upgrades, theme shortcuts, and studio-ready controls for ${spec.fallbackApp.name}.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.72f)
            )
            spec.featureHighlights.forEachIndexed { index, feature ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = spec.heroColors[index % spec.heroColors.size].copy(alpha = 0.12f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = detailFeatureIcon(feature.iconName),
                            contentDescription = null,
                            tint = spec.heroColors[index % spec.heroColors.size]
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = feature.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.onSurface
                            )
                            Text(
                                text = feature.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurface.copy(alpha = 0.72f)
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                spec.recommendedThemeIds.forEach { themeId ->
                    FilterChip(
                        selected = false,
                        onClick = { onThemeSelected(themeId) },
                        label = { Text(ThemePresets.getById(themeId)?.name ?: themeId) }
                    )
                }
            }
        }
    }
}

private fun detailFeatureIcon(iconName: String) = when (iconName) {
    "folder" -> Icons.Default.FolderOpen
    "palette" -> Icons.Default.Palette
    "star" -> Icons.Default.Star
    "qr" -> Icons.Default.QrCode2
    "wifi" -> Icons.Default.Wifi
    "auto_awesome" -> Icons.Default.AutoAwesome
    else -> Icons.Default.RocketLaunch
}

@Composable
private fun DetailBountiesSection(
    appId: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    context: android.content.Context
) {
    val bountyRepo = remember { BountyRepository(context) }
    val bounties by bountyRepo.getBountiesForApp(appId).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    if (bounties.isEmpty()) return
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Developer bounties",
                style = MaterialTheme.typography.titleMedium,
                color = colors.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            bounties.forEach { bounty ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            bounty.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface
                        )
                        Text(
                            bounty.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    if (bounty.isClaimed) {
                        Text("Claimed", style = MaterialTheme.typography.labelSmall, color = colors.success)
                    } else {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    bountyRepo.claimBounty(bounty.id)
                                }
                            }
                        ) {
                            Text("${bounty.rewardAmount} \uD83C\uDF6C")
                        }
                    }
                }
            }
        }
    }
}
