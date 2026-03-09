package com.sugarmunch.app.theme.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.sugarmunch.app.data.LocalPreferencesRepository
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.components.LayeredThemeRenderer
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.theme.engine.ThemeRuntimeSnapshot
import com.sugarmunch.app.theme.layers.BlendMode
import com.sugarmunch.app.theme.model.ThemeCategory
import com.sugarmunch.app.theme.presets.ThemePresets
import com.sugarmunch.app.theme.profile.AppThemeOverride
import com.sugarmunch.app.theme.profile.ImportedFontAsset
import com.sugarmunch.app.theme.profile.ThemeBackgroundKind
import com.sugarmunch.app.theme.profile.ThemeFontAxisValue
import com.sugarmunch.app.theme.profile.ThemeFontRef
import com.sugarmunch.app.theme.profile.ThemeFontSource
import com.sugarmunch.app.theme.profile.ThemeGradientStopSpec
import com.sugarmunch.app.theme.profile.ThemeLayerKind
import com.sugarmunch.app.theme.profile.ThemeLayerSpec
import com.sugarmunch.app.theme.profile.ThemeMeshPointSpec
import com.sugarmunch.app.theme.profile.ThemeMeshSpec
import com.sugarmunch.app.theme.profile.ThemeProfile
import com.sugarmunch.app.theme.profile.ThemeTransportEnvelope
import com.sugarmunch.app.theme.profile.parseThemeImportCandidate
import com.sugarmunch.app.theme.profile.toBase64Payload
import com.sugarmunch.app.theme.profile.toCandyTheme
import com.sugarmunch.app.theme.profile.toComposeColor
import com.sugarmunch.app.theme.profile.toDeepLinkUri
import com.sugarmunch.app.theme.profile.toHexString
import com.sugarmunch.app.theme.profile.toJson
import com.sugarmunch.app.theme.profile.toThemeFontRef
import com.sugarmunch.app.theme.profile.toThemeProfile
import com.sugarmunch.app.ui.studio.ColorWheelPicker
import com.sugarmunch.app.ui.typography.SugarFontFamily
import com.sugarmunch.app.ui.typography.TypeScale
import com.sugarmunch.app.ui.typography.toDynamicTypographyConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.abs
import kotlin.math.max

private enum class ThemeStudioTab(val label: String) {
    OVERVIEW("Overview"),
    COLORS("Colors"),
    LAYERS("Layers"),
    TYPOGRAPHY("Typography"),
    MESH("Mesh"),
    SHARING("Sharing"),
    PER_APP("Per-App")
}

private enum class ThemeColorRole(val label: String) {
    PRIMARY("Primary"),
    SECONDARY("Secondary"),
    TERTIARY("Tertiary"),
    ACCENT("Accent"),
    SURFACE("Surface"),
    BACKGROUND("Background")
}

private enum class FontSlot(val label: String) {
    HEADING("Heading"),
    BODY("Body"),
    CAPTION("Caption")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhaseOneThemeStudioScreen(
    appId: String?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val gson = remember { Gson() }
    val scope = rememberCoroutineScope()
    val prefs = LocalPreferencesRepository.current
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
    val allProfiles by themeManager.allProfiles.collectAsState()
    val importedFonts by themeManager.importedFonts.collectAsState()
    val isDynamicThemingEnabled by themeManager.isDynamicThemingEnabled.collectAsState()
    val appOverride = if (appId != null) {
        prefs.getAppThemeOverride(appId).collectAsState(initial = null).value
    } else null

    var selectedTab by rememberSaveable { mutableStateOf(ThemeStudioTab.OVERVIEW.ordinal) }
    var editingProfile by remember(runtime.profile.id) { mutableStateOf(runtime.profile.ensureEditable()) }
    var activeColorRole by rememberSaveable { mutableStateOf(ThemeColorRole.PRIMARY.ordinal) }
    var selectedFontSlot by rememberSaveable { mutableStateOf(FontSlot.HEADING.ordinal) }
    var savedSwatches by rememberSaveable { mutableStateOf(listOf<String>()) }
    var recentSwatches by rememberSaveable { mutableStateOf(listOf<String>()) }
    var paletteSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var generatedQrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(editingProfile) {
        themeManager.setPreviewProfile(editingProfile)
    }
    DisposableEffect(Unit) {
        onDispose { themeManager.clearPreviewProfile() }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                paletteSuggestions = extractPalette(context, uri)
                if (paletteSuggestions.isNotEmpty()) {
                    snackbarHostState.showSnackbar("Palette extracted from image")
                }
            }
        }
    }
    val fontPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val font = ImportedFontAsset(
                    id = "font-${System.currentTimeMillis()}",
                    displayName = uri.lastPathSegment?.substringAfterLast('/') ?: "Imported Font",
                    uri = uri.toString(),
                    isVariable = true
                )
                themeManager.saveImportedFont(font)
                snackbarHostState.showSnackbar("Imported ${font.displayName}")
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                val raw = readTextFromUri(context, uri)
                val candidate = parseThemeImportCandidate(raw, gson)
                if (candidate != null) {
                    editingProfile = candidate.profile.ensureEditable()
                    snackbarHostState.showSnackbar("Imported ${candidate.profile.name} (${candidate.sourceLabel})")
                } else {
                    snackbarHostState.showSnackbar("Could not parse theme import")
                }
            }
        }
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            scope.launch {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(ThemeTransportEnvelope(profile = editingProfile).toJson(gson).toByteArray())
                }
                snackbarHostState.showSnackbar("Theme exported")
            }
        }
    }
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val contents = result.contents ?: return@rememberLauncherForActivityResult
        scope.launch {
            val candidate = parseThemeImportCandidate(contents, gson)
            if (candidate != null) {
                editingProfile = candidate.profile.ensureEditable()
                snackbarHostState.showSnackbar("Imported theme from QR")
            } else {
                snackbarHostState.showSnackbar("QR code did not contain a valid theme")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (appId == null) "Theme Studio" else "Theme Studio: $appId")
                        Text(
                            editingProfile.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = runtime.colors.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        editingProfile = editingProfile.copy(
                            id = "custom-${System.currentTimeMillis()}",
                            name = "${editingProfile.name} Copy",
                            category = ThemeCategory.CUSTOM,
                            metadata = editingProfile.metadata.copy(
                                builtIn = false,
                                sourceProfileId = editingProfile.id
                            )
                        )
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate")
                    }
                    IconButton(onClick = {
                        val saved = editingProfile.ensureEditable()
                        themeManager.saveThemeProfile(saved, activate = appId == null)
                        if (appId != null) {
                            themeManager.applyAppThemeOverride(
                                appId,
                                (appOverride ?: AppThemeOverride()).copy(
                                    enabled = true,
                                    themeProfileId = saved.id
                                )
                            )
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PreviewCard(
                    profile = editingProfile,
                    runtime = runtime,
                    appId = appId
                )
            }
            item {
                ScrollableTabRow(selectedTabIndex = selectedTab) {
                    ThemeStudioTab.entries.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(tab.label) }
                        )
                    }
                }
            }
            item {
                when (ThemeStudioTab.entries[selectedTab]) {
                    ThemeStudioTab.OVERVIEW -> OverviewTab(
                        editingProfile = editingProfile,
                        allProfiles = allProfiles,
                        runtime = runtime,
                        isDynamicThemingEnabled = isDynamicThemingEnabled,
                        onDynamicThemingChanged = themeManager::setDynamicThemingEnabled,
                        onProfileSelected = { selected ->
                            editingProfile = selected.ensureEditable()
                            themeManager.setThemeById(selected.id)
                        },
                        onIntensityChanged = { theme, background, particle, animation ->
                            themeManager.setThemeIntensity(theme)
                            themeManager.setBackgroundIntensity(background)
                            themeManager.setParticleIntensity(particle)
                            themeManager.setAnimationIntensity(animation)
                        }
                    )
                    ThemeStudioTab.COLORS -> ColorsTab(
                        profile = editingProfile,
                        activeRole = ThemeColorRole.entries[activeColorRole],
                        savedSwatches = savedSwatches,
                        recentSwatches = recentSwatches,
                        paletteSuggestions = paletteSuggestions,
                        onRoleChanged = { activeColorRole = it.ordinal },
                        onPickColor = { color ->
                            editingProfile = editingProfile.updatePalette(ThemeColorRole.entries[activeColorRole], color.toHexString())
                            recentSwatches = (listOf(color.toHexString()) + recentSwatches).distinct().take(12)
                        },
                        onSaveSwatch = { swatch ->
                            savedSwatches = (savedSwatches + swatch).distinct().take(20)
                        },
                        onExtractPalette = {
                            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onApplySwatch = { swatch ->
                            editingProfile = editingProfile.updatePalette(ThemeColorRole.entries[activeColorRole], swatch)
                        }
                    )
                    ThemeStudioTab.LAYERS -> LayersTab(
                        profile = editingProfile,
                        onProfileChanged = { editingProfile = it }
                    )
                    ThemeStudioTab.TYPOGRAPHY -> TypographyTab(
                        profile = editingProfile,
                        importedFonts = importedFonts,
                        selectedFontSlot = FontSlot.entries[selectedFontSlot],
                        onSelectedFontSlotChanged = { selectedFontSlot = it.ordinal },
                        onImportFont = {
                            fontPicker.launch(arrayOf("font/ttf", "font/otf", "*/*"))
                        },
                        onProfileChanged = { editingProfile = it }
                    )
                    ThemeStudioTab.MESH -> MeshTab(
                        profile = editingProfile,
                        onProfileChanged = { editingProfile = it }
                    )
                    ThemeStudioTab.SHARING -> SharingTab(
                        profile = editingProfile,
                        gson = gson,
                        qrBitmap = generatedQrBitmap,
                        onGenerateQr = {
                            generatedQrBitmap = generateQrBitmap(ThemeTransportEnvelope(profile = editingProfile).toBase64Payload(gson))
                        },
                        onExportJson = { exportLauncher.launch("${editingProfile.name}.json") },
                        onImportJson = { importLauncher.launch(arrayOf("application/json", "text/plain")) },
                        onShare = {
                            shareTheme(context, editingProfile, gson)
                        },
                        onScanQr = {
                            scanLauncher.launch(
                                ScanOptions().apply {
                                    setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                                    setPrompt("Scan a SugarMunch theme QR")
                                    setBeepEnabled(false)
                                }
                            )
                        }
                    )
                    ThemeStudioTab.PER_APP -> PerAppTab(
                        appId = appId,
                        allProfiles = allProfiles,
                        currentOverride = appOverride,
                        onApplyOverride = { override ->
                            if (appId != null) {
                                themeManager.applyAppThemeOverride(appId, override)
                            }
                        },
                        onClearOverride = {
                            if (appId != null) {
                                themeManager.clearAppThemeOverride(appId)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewCard(
    profile: ThemeProfile,
    runtime: ThemeRuntimeSnapshot,
    appId: String?
) {
    val context = LocalContext.current
    val previewTypography = remember(profile) {
        profile.typography.toDynamicTypographyConfig(context, emptyList()).toTypography()
    }
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = runtime.colors.surface.copy(alpha = 0.92f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            AnimatedThemeBackground(
                appId = appId,
                previewProfile = profile,
                previewTheme = profile.toCandyTheme(),
                previewBackgroundIntensity = runtime.backgroundIntensity,
                previewParticleIntensity = runtime.particleIntensity
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(profile.name, style = previewTypography.headlineMedium, color = runtime.colors.onSurface)
                    Text(
                        text = profile.description.ifBlank { "Canonical theme profile preview" },
                        style = previewTypography.bodyMedium,
                        color = runtime.colors.onSurface.copy(alpha = 0.8f)
                    )
                }
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = runtime.colors.surface.copy(alpha = 0.78f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Layered runtime preview", style = previewTypography.titleMedium, color = runtime.colors.onSurface)
                        Text(
                            "Theme ${runtime.themeIntensity.formatOneDecimal()}  •  Background ${runtime.backgroundIntensity.formatOneDecimal()}  •  App override ${if (runtime.isOverrideActive) "On" else "Off"}",
                            style = previewTypography.bodySmall,
                            color = runtime.colors.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewTab(
    editingProfile: ThemeProfile,
    allProfiles: List<ThemeProfile>,
    runtime: ThemeRuntimeSnapshot,
    isDynamicThemingEnabled: Boolean,
    onDynamicThemingChanged: (Boolean) -> Unit,
    onProfileSelected: (ThemeProfile) -> Unit,
    onIntensityChanged: (Float, Float, Float, Float) -> Unit
) {
    var genPrompt by remember { mutableStateOf("") }
    val genEngine = remember { com.sugarmunch.app.ai.GenerativeThemeEngine() }
    val themeManager = remember { ThemeManager.getInstance(LocalContext.current) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StudioSectionCard("AI Generative Theme", "Describe your perfect theme") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = genPrompt,
                    onValueChange = { genPrompt = it },
                    label = { Text("e.g. Cyberpunk candy factory on Mars") },
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    if (genPrompt.isNotBlank()) {
                        val generatedTheme = genEngine.generateThemeFromPrompt(genPrompt)
                        val profile = generatedTheme.toThemeProfile(builtIn = false)
                        onProfileSelected(profile)
                        themeManager.saveThemeProfile(profile, activate = true)
                    }
                }) {
                    Text("Generate")
                }
            }
        }
        
        StudioSectionCard("Runtime Controls", "Global convergence and intensity state") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Dynamic time-of-day theming", fontWeight = FontWeight.SemiBold)
                    Text("Keeps the canonical runtime synced to time presets", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = isDynamicThemingEnabled, onCheckedChange = onDynamicThemingChanged)
            }
            Divider(Modifier.padding(vertical = 12.dp))
            IntensitySlider("Theme", runtime.themeIntensity) { onIntensityChanged(it, runtime.backgroundIntensity, runtime.particleIntensity, runtime.animationIntensity) }
            IntensitySlider("Background", runtime.backgroundIntensity) { onIntensityChanged(runtime.themeIntensity, it, runtime.particleIntensity, runtime.animationIntensity) }
            IntensitySlider("Particles", runtime.particleIntensity) { onIntensityChanged(runtime.themeIntensity, runtime.backgroundIntensity, it, runtime.animationIntensity) }
            IntensitySlider("Animation", runtime.animationIntensity) { onIntensityChanged(runtime.themeIntensity, runtime.backgroundIntensity, runtime.particleIntensity, it) }
        }
        StudioSectionCard("Profiles", "Built-ins and custom canonical profiles") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                allProfiles.forEach { profile ->
                    FilterChip(
                        selected = profile.id == runtime.profile.id,
                        onClick = { onProfileSelected(profile) },
                        label = { Text(profile.name) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Editing: ${editingProfile.name}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ColorsTab(
    profile: ThemeProfile,
    activeRole: ThemeColorRole,
    savedSwatches: List<String>,
    recentSwatches: List<String>,
    paletteSuggestions: List<String>,
    onRoleChanged: (ThemeColorRole) -> Unit,
    onPickColor: (Color) -> Unit,
    onSaveSwatch: (String) -> Unit,
    onExtractPalette: () -> Unit,
    onApplySwatch: (String) -> Unit
) {
    val selectedColor = profile.colorForRole(activeRole).toComposeColor()
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StudioSectionCard("Palette Tools", "Harmony, swatches, WCAG checks, photo extraction") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeColorRole.entries.forEach { role ->
                    FilterChip(selected = activeRole == role, onClick = { onRoleChanged(role) }, label = { Text(role.label) })
                }
            }
            Spacer(Modifier.height(12.dp))
            ColorWheelPicker(selectedColor = selectedColor, onColorSelected = onPickColor)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = { onSaveSwatch(selectedColor.toHexString()) }, label = { Text("Save swatch") }, leadingIcon = { Icon(Icons.Default.Save, null) })
                AssistChip(onClick = onExtractPalette, label = { Text("Extract from image") }, leadingIcon = { Icon(Icons.Default.Upload, null) })
            }
            Spacer(Modifier.height(12.dp))
            ContrastWarnings(profile)
        }
        SwatchSection("Saved Swatches", savedSwatches, onApplySwatch)
        SwatchSection("Recent", recentSwatches, onApplySwatch)
        SwatchSection("Harmony Suggestions", harmonyFor(selectedColor) + paletteSuggestions, onApplySwatch)
    }
}

@Composable
private fun LayersTab(
    profile: ThemeProfile,
    onProfileChanged: (ThemeProfile) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StudioSectionCard("Layer Renderer", "Blend modes, ordering, duplication, and safe caps") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(profile.palette.backgroundHex.toComposeColor(), RoundedCornerShape(20.dp))
            ) {
                LayeredThemeRenderer(
                    profile = profile,
                    backgroundIntensity = profile.intensityDefaults.background,
                    particleIntensity = profile.intensityDefaults.particle,
                    reduceMotion = false
                )
            }
            Spacer(Modifier.height(12.dp))
            Text("Particle-heavy layers are capped to a safer render budget during preview.", style = MaterialTheme.typography.bodySmall)
        }
        profile.layers.forEachIndexed { index, layer ->
            LayerEditorRow(
                layer = layer,
                canMoveUp = index > 0,
                canMoveDown = index < profile.layers.lastIndex,
                onToggle = { enabled -> onProfileChanged(profile.updateLayer(index) { copy(enabled = enabled) }) },
                onOpacity = { opacity -> onProfileChanged(profile.updateLayer(index) { copy(opacity = opacity) }) },
                onBlendMode = { blend -> onProfileChanged(profile.updateLayer(index) { copy(blendMode = blend) }) },
                onMoveUp = { onProfileChanged(profile.moveLayer(index, index - 1)) },
                onMoveDown = { onProfileChanged(profile.moveLayer(index, index + 1)) },
                onDuplicate = { onProfileChanged(profile.copy(layers = profile.layers + layer.copy(id = "${layer.id}-${System.currentTimeMillis()}", name = "${layer.name} Copy"))) },
                onDelete = { onProfileChanged(profile.copy(layers = profile.layers.filterNot { it.id == layer.id })) }
            )
        }
        OutlinedButton(onClick = {
            onProfileChanged(profile.copy(layers = profile.layers + profile.defaultLayerSpec()))
        }) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add Layer")
        }
    }
}

@Composable
private fun TypographyTab(
    profile: ThemeProfile,
    importedFonts: List<ImportedFontAsset>,
    selectedFontSlot: FontSlot,
    onSelectedFontSlotChanged: (FontSlot) -> Unit,
    onImportFont: () -> Unit,
    onProfileChanged: (ThemeProfile) -> Unit
) {
    val context = LocalContext.current
    val typography = remember(profile, importedFonts) {
        profile.typography.toDynamicTypographyConfig(context, importedFonts).toTypography()
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StudioSectionCard("Font Sources", "Bundled variable fonts plus SAF-imported files") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FontSlot.entries.forEach { slot ->
                    FilterChip(
                        selected = selectedFontSlot == slot,
                        onClick = { onSelectedFontSlotChanged(slot) },
                        label = { Text(slot.label) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SugarFontFamily.entries.forEach { font ->
                    FilterChip(
                        selected = profile.typography.fontFor(selectedFontSlot).id == font.name,
                        onClick = {
                            onProfileChanged(profile.updateFont(selectedFontSlot, font.toThemeFontRef()))
                        },
                        label = { Text(font.displayName) }
                    )
                }
            }
            if (importedFonts.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("Imported fonts", fontWeight = FontWeight.SemiBold)
                importedFonts.forEach { font ->
                    AssistChip(
                        onClick = {
                            onProfileChanged(
                                profile.updateFont(
                                    selectedFontSlot,
                                    ThemeFontRef(
                                        source = ThemeFontSource.IMPORTED,
                                        id = font.id,
                                        displayName = font.displayName,
                                        uri = font.uri
                                    )
                                )
                            )
                        },
                        label = { Text(font.displayName) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onImportFont) {
                Icon(Icons.Default.FolderOpen, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Import Font")
            }
        }
        StudioSectionCard("Axis Controls", "Weight, width, slant, and optical size") {
            AxisSlider("Weight", profile.typography.fontFor(selectedFontSlot).axisValue("wght", 500f), 100f..900f) {
                onProfileChanged(profile.updateFontAxis(selectedFontSlot, "wght", it))
            }
            AxisSlider("Width", profile.typography.fontFor(selectedFontSlot).axisValue("wdth", 100f), 60f..140f) {
                onProfileChanged(profile.updateFontAxis(selectedFontSlot, "wdth", it))
            }
            AxisSlider("Slant", profile.typography.fontFor(selectedFontSlot).axisValue("slnt", 0f), -10f..10f) {
                onProfileChanged(profile.updateFontAxis(selectedFontSlot, "slnt", it))
            }
            AxisSlider("Optical Size", profile.typography.fontFor(selectedFontSlot).axisValue("opsz", 16f), 8f..48f) {
                onProfileChanged(profile.updateFontAxis(selectedFontSlot, "opsz", it))
            }
        }
        StudioSectionCard("Preview", "Headings, body, and labels before save") {
            Text("Candy Headlines", style = typography.headlineLarge)
            Text("Compose variable fonts and imported typefaces render through the same canonical typography spec.", style = typography.bodyLarge)
            Text("Buttons / labels", style = typography.labelLarge)
        }
    }
}

@Composable
private fun MeshTab(
    profile: ThemeProfile,
    onProfileChanged: (ThemeProfile) -> Unit
) {
    val mesh = profile.mesh ?: defaultMesh(profile)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StudioSectionCard("Mesh Editor", "Draggable control points, color assignment, randomize, reset") {
            MeshEditorCanvas(
                mesh = mesh,
                onMeshChanged = { updated -> onProfileChanged(profile.copy(mesh = updated, background = profile.background.copy(kind = ThemeBackgroundKind.MESH))) }
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onProfileChanged(profile.copy(mesh = randomizeMesh(profile))) }) {
                    Text("Randomize")
                }
                OutlinedButton(onClick = { onProfileChanged(profile.copy(mesh = defaultMesh(profile))) }) {
                    Text("Reset")
                }
            }
        }
        mesh.points.forEachIndexed { index, point ->
            StudioSectionCard("Point ${index + 1}", point.colorHex) {
                AxisSlider("X", point.xFraction, 0f..1f) {
                    onProfileChanged(profile.copy(mesh = mesh.copy(points = mesh.points.update(index) { copy(xFraction = it) })))
                }
                AxisSlider("Y", point.yFraction, 0f..1f) {
                    onProfileChanged(profile.copy(mesh = mesh.copy(points = mesh.points.update(index) { copy(yFraction = it) })))
                }
            }
        }
    }
}

@Composable
private fun SharingTab(
    profile: ThemeProfile,
    gson: Gson,
    qrBitmap: Bitmap?,
    onGenerateQr: () -> Unit,
    onExportJson: () -> Unit,
    onImportJson: () -> Unit,
    onShare: () -> Unit,
    onScanQr: () -> Unit
) {
    val deepLink = remember(profile.id) { profile.toDeepLinkUri(gson).toString() }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StudioSectionCard("Transport", "Versioned JSON, share text, QR, scan, and deep links") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onExportJson) {
                    Icon(Icons.Default.Download, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Export JSON")
                }
                OutlinedButton(onClick = onImportJson) {
                    Icon(Icons.Default.Upload, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Import")
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onShare) {
                    Icon(Icons.Default.Share, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Share")
                }
                OutlinedButton(onClick = onScanQr) {
                    Icon(Icons.Default.QrCode2, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Scan QR")
                }
                OutlinedButton(onClick = onGenerateQr) {
                    Icon(Icons.Default.QrCode2, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Generate QR")
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = deepLink,
                onValueChange = {},
                readOnly = true,
                label = { Text("Deep link payload") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (qrBitmap != null) {
            StudioSectionCard("QR Preview", "Portable profile payload") {
                androidx.compose.foundation.Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Theme QR",
                    modifier = Modifier.size(220.dp)
                )
            }
        }
    }
}

@Composable
private fun PerAppTab(
    appId: String?,
    allProfiles: List<ThemeProfile>,
    currentOverride: AppThemeOverride?,
    onApplyOverride: (AppThemeOverride) -> Unit,
    onClearOverride: () -> Unit
) {
    if (appId == null) {
        StudioSectionCard("Per-App Overrides", "Open Theme Studio from an app route to edit scoped overrides.") {
            Text("Detail, Preview, and Utility Studio now resolve route-aware theme overrides automatically.")
        }
        return
    }
    val overrideState = currentOverride ?: AppThemeOverride(enabled = false)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StudioSectionCard("Override Controls", "Profile, accent, and intensity precedence for $appId") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable override", fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = overrideState.enabled,
                    onCheckedChange = { onApplyOverride(overrideState.copy(enabled = it)) }
                )
            }
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                allProfiles.forEach { profile ->
                    FilterChip(
                        selected = overrideState.themeProfileId == profile.id,
                        onClick = { onApplyOverride(overrideState.copy(enabled = true, themeProfileId = profile.id)) },
                        label = { Text(profile.name) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            IntensitySlider("Theme", overrideState.themeIntensity ?: 1f) {
                onApplyOverride(overrideState.copy(enabled = true, themeIntensity = it))
            }
            IntensitySlider("Background", overrideState.backgroundIntensity ?: 1f) {
                onApplyOverride(overrideState.copy(enabled = true, backgroundIntensity = it))
            }
            IntensitySlider("Particles", overrideState.particleIntensity ?: 1f) {
                onApplyOverride(overrideState.copy(enabled = true, particleIntensity = it))
            }
            OutlinedButton(onClick = onClearOverride) {
                Icon(Icons.Default.Delete, null)
                Spacer(Modifier.width(8.dp))
                Text("Reset App Override")
            }
        }
    }
}

@Composable
private fun StudioSectionCard(
    title: String,
    subtitle: String,
    content: @Composable Column.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                Spacer(Modifier.height(4.dp))
                content()
            }
        )
    }
}

@Composable
private fun IntensitySlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Text("$label: ${value.formatOneDecimal()}")
        Slider(value = value, onValueChange = onValueChange, valueRange = 0f..2f)
    }
}

@Composable
private fun AxisSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Text("$label: ${value.formatOneDecimal()}")
        Slider(value = value, onValueChange = onValueChange, valueRange = range)
    }
}

@Composable
private fun LayerEditorRow(
    layer: ThemeLayerSpec,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onToggle: (Boolean) -> Unit,
    onOpacity: (Float) -> Unit,
    onBlendMode: (BlendMode) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    StudioSectionCard(layer.name, layer.kind.name.replace('_', ' ')) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Switch(checked = layer.enabled, onCheckedChange = onToggle)
            IconButton(onClick = onMoveUp, enabled = canMoveUp) { Icon(Icons.Default.ArrowUpward, null) }
            IconButton(onClick = onMoveDown, enabled = canMoveDown) { Icon(Icons.Default.ArrowDownward, null) }
            IconButton(onClick = onDuplicate) { Icon(Icons.Default.ContentCopy, null) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null) }
        }
        IntensitySlider("Opacity", layer.opacity) { onOpacity(it.coerceIn(0f, 1f)) }
        OutlinedButton(onClick = { expanded = !expanded }) {
            Text(if (expanded) "Hide Blend Modes" else "Blend Mode")
        }
        if (expanded) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BlendMode.entries.forEach { mode ->
                    FilterChip(selected = mode == layer.blendMode, onClick = { onBlendMode(mode) }, label = { Text(mode.displayName) })
                }
            }
        }
    }
}

@Composable
private fun SwatchSection(
    title: String,
    swatches: List<String>,
    onApply: (String) -> Unit
) {
    if (swatches.isEmpty()) return
    StudioSectionCard(title, "Tap to apply") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            swatches.distinct().forEach { hex ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(hex.toComposeColor(), CircleShape)
                        .clickable { onApply(hex) }
                )
            }
        }
    }
}

@Composable
private fun ContrastWarnings(profile: ThemeProfile) {
    val primaryContrast = contrastRatio(profile.palette.primaryHex.toComposeColor(), profile.palette.onPrimaryHex.toComposeColor())
    val surfaceContrast = contrastRatio(profile.palette.surfaceHex.toComposeColor(), profile.palette.onSurfaceHex.toComposeColor())
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("WCAG quick check", fontWeight = FontWeight.SemiBold)
        Text("Primary/on-primary: ${primaryContrast.formatOneDecimal()}${if (primaryContrast < 4.5f) "  •  Warning" else ""}")
        Text("Surface/on-surface: ${surfaceContrast.formatOneDecimal()}${if (surfaceContrast < 4.5f) "  •  Warning" else ""}")
    }
}

@Composable
private fun MeshEditorCanvas(
    mesh: ThemeMeshSpec,
    onMeshChanged: (ThemeMeshSpec) -> Unit
) {
    var draggingIndex by remember { mutableIntStateOf(-1) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .pointerInput(mesh) {
                detectDragGestures(
                    onDragStart = { start ->
                        draggingIndex = mesh.points.indexOfClosest(start.x / size.width, start.y / size.height)
                    },
                    onDragEnd = { draggingIndex = -1 }
                ) { change, dragAmount ->
                    change.consume()
                    if (draggingIndex >= 0) {
                        val current = mesh.points[draggingIndex]
                        val next = current.copy(
                            xFraction = (current.xFraction + dragAmount.x / size.width).coerceIn(0f, 1f),
                            yFraction = (current.yFraction + dragAmount.y / size.height).coerceIn(0f, 1f)
                        )
                        onMeshChanged(mesh.copy(points = mesh.points.update(draggingIndex) { next }))
                    }
                }
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            mesh.points.forEachIndexed { index, point ->
                drawCircle(
                    color = point.colorHex.toComposeColor().copy(alpha = 0.65f),
                    radius = size.minDimension * 0.2f,
                    center = Offset(point.xFraction * size.width, point.yFraction * size.height)
                )
                drawCircle(
                    color = Color.White,
                    radius = if (index == draggingIndex) 16f else 12f,
                    center = Offset(point.xFraction * size.width, point.yFraction * size.height)
                )
            }
        }
    }
}

private fun ThemeProfile.ensureEditable(): ThemeProfile {
    return if (metadata.builtIn) {
        copy(
            id = "custom-${System.currentTimeMillis()}",
            name = "$name Remix",
            category = ThemeCategory.CUSTOM,
            metadata = metadata.copy(
                builtIn = false,
                sourceProfileId = id
            )
        )
    } else {
        copy(category = ThemeCategory.CUSTOM)
    }
}

private fun ThemeProfile.colorForRole(role: ThemeColorRole): String = when (role) {
    ThemeColorRole.PRIMARY -> palette.primaryHex
    ThemeColorRole.SECONDARY -> palette.secondaryHex
    ThemeColorRole.TERTIARY -> palette.tertiaryHex
    ThemeColorRole.ACCENT -> palette.accentHex
    ThemeColorRole.SURFACE -> palette.surfaceHex
    ThemeColorRole.BACKGROUND -> palette.backgroundHex
}

private fun ThemeProfile.updatePalette(role: ThemeColorRole, colorHex: String): ThemeProfile {
    val updatedPalette = when (role) {
        ThemeColorRole.PRIMARY -> palette.copy(primaryHex = colorHex)
        ThemeColorRole.SECONDARY -> palette.copy(secondaryHex = colorHex)
        ThemeColorRole.TERTIARY -> palette.copy(tertiaryHex = colorHex)
        ThemeColorRole.ACCENT -> palette.copy(accentHex = colorHex)
        ThemeColorRole.SURFACE -> palette.copy(surfaceHex = colorHex)
        ThemeColorRole.BACKGROUND -> palette.copy(backgroundHex = colorHex)
    }
    return copy(palette = updatedPalette)
}

private fun ThemeProfile.updateLayer(index: Int, transform: ThemeLayerSpec.() -> ThemeLayerSpec): ThemeProfile {
    return copy(layers = layers.update(index) { it.transform() })
}

private fun ThemeProfile.moveLayer(from: Int, to: Int): ThemeProfile {
    if (from == to || from !in layers.indices || to !in layers.indices) return this
    val mutable = layers.toMutableList()
    val layer = mutable.removeAt(from)
    mutable.add(to, layer)
    return copy(layers = mutable)
}

private fun ThemeProfile.defaultLayerSpec(): ThemeLayerSpec {
    return ThemeLayerSpec(
        id = "layer-${System.currentTimeMillis()}",
        name = "Overlay ${layers.size + 1}",
        kind = ThemeLayerKind.COLOR_OVERLAY,
        overlayHex = palette.accentHex,
        opacity = 0.35f
    )
}

private fun ThemeProfile.updateFont(slot: FontSlot, ref: ThemeFontRef): ThemeProfile {
    val updatedTypography = when (slot) {
        FontSlot.HEADING -> typography.copy(headingFont = ref)
        FontSlot.BODY -> typography.copy(bodyFont = ref)
        FontSlot.CAPTION -> typography.copy(captionFont = ref)
    }
    return copy(typography = updatedTypography)
}

private fun ThemeProfile.updateFontAxis(slot: FontSlot, tag: String, value: Float): ThemeProfile {
    val current = typography.fontFor(slot)
    val updated = current.copy(
        axes = current.axes.filterNot { it.tag == tag } + ThemeFontAxisValue(tag = tag, value = value)
    )
    return updateFont(slot, updated)
}

private fun com.sugarmunch.app.theme.profile.ThemeTypographySpec.fontFor(slot: FontSlot): ThemeFontRef = when (slot) {
    FontSlot.HEADING -> headingFont
    FontSlot.BODY -> bodyFont
    FontSlot.CAPTION -> captionFont
}

private fun ThemeFontRef.axisValue(tag: String, default: Float): Float {
    return axes.firstOrNull { it.tag == tag }?.value ?: default
}

private fun defaultMesh(profile: ThemeProfile): ThemeMeshSpec {
    return ThemeMeshSpec(
        points = listOf(
            ThemeMeshPointSpec("p1", 0.18f, 0.2f, profile.palette.primaryHex, driftX = 0.3f, driftY = 0.2f),
            ThemeMeshPointSpec("p2", 0.78f, 0.28f, profile.palette.secondaryHex, driftX = 0.2f, driftY = 0.25f),
            ThemeMeshPointSpec("p3", 0.32f, 0.76f, profile.palette.tertiaryHex, driftX = 0.28f, driftY = 0.18f),
            ThemeMeshPointSpec("p4", 0.72f, 0.78f, profile.palette.accentHex, driftX = 0.24f, driftY = 0.24f)
        ),
        animationSpeed = 1f,
        complexity = 4,
        amplitude = 0.12f,
        seed = 42L
    )
}

private fun randomizeMesh(profile: ThemeProfile): ThemeMeshSpec {
    val colors = listOf(profile.palette.primaryHex, profile.palette.secondaryHex, profile.palette.tertiaryHex, profile.palette.accentHex)
    return ThemeMeshSpec(
        points = List(4) { index ->
            ThemeMeshPointSpec(
                id = "rand-$index",
                xFraction = 0.1f + (index * 0.2f) + (Math.random().toFloat() * 0.2f),
                yFraction = 0.1f + (Math.random().toFloat() * 0.8f),
                colorHex = colors[index % colors.size],
                influence = 1f,
                driftX = 0.12f + Math.random().toFloat() * 0.2f,
                driftY = 0.12f + Math.random().toFloat() * 0.2f
            )
        },
        animationSpeed = 0.8f + Math.random().toFloat() * 0.8f,
        complexity = 4,
        amplitude = 0.08f + Math.random().toFloat() * 0.1f,
        seed = System.currentTimeMillis()
    )
}

private suspend fun extractPalette(context: android.content.Context, uri: android.net.Uri): List<String> {
    return withContext(Dispatchers.IO) {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
        val palette = Palette.from(bitmap).maximumColorCount(8).generate()
        listOfNotNull(
            palette.vibrantSwatch?.rgb,
            palette.mutedSwatch?.rgb,
            palette.lightVibrantSwatch?.rgb,
            palette.darkVibrantSwatch?.rgb,
            palette.dominantSwatch?.rgb
        ).map { Color(it).toHexString() }.distinct()
    }
}

private suspend fun readTextFromUri(context: android.content.Context, uri: android.net.Uri): String {
    return withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream)).readText()
        }.orEmpty()
    }
}

private fun shareTheme(context: android.content.Context, profile: ThemeProfile, gson: Gson) {
    val payload = ThemeTransportEnvelope(profile = profile).toJson(gson)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, payload)
    }
    context.startActivity(Intent.createChooser(intent, "Share theme"))
}

private fun generateQrBitmap(payload: String): Bitmap {
    val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 512, 512)
    val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
    for (x in 0 until 512) {
        for (y in 0 until 512) {
            bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bitmap
}

private fun harmonyFor(color: Color): List<String> {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(color.toArgb(), hsl)
    return listOf(30f, 150f, 180f).map { shift ->
        val clone = hsl.copyOf()
        clone[0] = (clone[0] + shift) % 360f
        Color(ColorUtils.HSLToColor(clone)).toHexString()
    }
}

private fun contrastRatio(first: Color, second: Color): Float {
    val l1 = first.luminance() + 0.05f
    val l2 = second.luminance() + 0.05f
    return max(l1, l2) / minOf(l1, l2)
}

private fun Float.formatOneDecimal(): String = String.format("%.1f", this)

private fun <T> List<T>.update(index: Int, transform: (T) -> T): List<T> {
    return mapIndexed { currentIndex, item ->
        if (currentIndex == index) transform(item) else item
    }
}

private fun List<ThemeMeshPointSpec>.indexOfClosest(xFraction: Float, yFraction: Float): Int {
    return indices.minByOrNull { index ->
        val point = this[index]
        abs(point.xFraction - xFraction) + abs(point.yFraction - yFraction)
    } ?: -1
}
