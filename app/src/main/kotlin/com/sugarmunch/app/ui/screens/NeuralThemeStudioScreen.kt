package com.sugarmunch.app.ui.screens

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sugarmunch.app.ai.neural.*
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import kotlinx.coroutines.launch

/**
 * Neural Theme Studio Screen
 * AI-powered theme generation and customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuralThemeStudioScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val neuralEngine = remember { NeuralThemeEngine.getInstance(context) }
    val scope = rememberCoroutineScope()

    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)

    val wallpaperColors by neuralEngine.currentWallpaperColors.collectAsState()
    val detectedMood by neuralEngine.detectedMood.collectAsState()
    val suggestedThemes by neuralEngine.suggestedThemes.collectAsState()
    val themeDNA by neuralEngine.themeDNA.collectAsState()
    val isGenerating by neuralEngine.isGeneratingTheme.collectAsState()
    val generationProgress by neuralEngine.generationProgress.collectAsState()

    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var showDNAMixer by remember { mutableStateOf(false) }
    var textPrompt by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Neural Theme Studio") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDNAMixer = true }) {
                        Icon(Icons.Dna, contentDescription = "DNA Mixer")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedThemeBackground()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Wallpaper Analysis
                item {
                    WallpaperAnalysisCard(
                        wallpaperColors = wallpaperColors,
                        onImageSelected = { url ->
                            selectedImageUrl = url
                            scope.launch {
                                // Load and analyze bitmap
                                val bitmap = loadBitmapFromUrl(context, url)
                                bitmap?.let {
                                    neuralEngine.analyzeWallpaper(it)
                                }
                            }
                        }
                    )
                }

                // Mood Selection
                item {
                    MoodSelectionCard(
                        selectedMood = detectedMood,
                        onMoodSelected = { mood ->
                            neuralEngine.setMood(mood)
                        }
                    )
                }

                // Text-to-Theme
                item {
                    TextToThemeCard(
                        textPrompt = textPrompt,
                        onTextChange = { textPrompt = it },
                        onGenerate = {
                            scope.launch {
                                neuralEngine.generateThemeFromText(textPrompt)
                            }
                        },
                        isGenerating = isGenerating
                    )
                }

                // Generation Progress
                if (isGenerating) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Generating Theme...",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                LinearProgressIndicator(
                                    progress = generationProgress,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "${(generationProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                // Suggested Themes
                if (suggestedThemes.isNotEmpty()) {
                    item {
                        Text(
                            text = "Suggested Themes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(suggestedThemes) { suggestion ->
                        ThemeSuggestionCard(
                            suggestion = suggestion,
                            onApply = {
                                // Apply suggested theme
                            }
                        )
                    }
                }

                // Theme DNA
                if (themeDNA != null) {
                    item {
                        ThemeDNACard(
                            dna = themeDNA!!,
                            onExport = {
                                val exported = neuralEngine.exportThemeDNA(themeDNA!!)
                                // Share exported DNA
                            },
                            onMutate = {
                                val mutated = themeDNA!!.mutate(0.2f)
                                // Apply mutated theme
                            }
                        )
                    }
                }

                // Time-Based Themes
                item {
                    TimeBasedThemesCard(
                        currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY),
                        timeThemes = listOf(
                            "Sunrise" to "5-8 AM",
                            "Morning" to "9-11 AM",
                            "Noon" to "12-2 PM",
                            "Afternoon" to "3-5 PM",
                            "Sunset" to "6-8 PM",
                            "Evening" to "9-10 PM",
                            "Night" to "11-4 AM"
                        ),
                        onThemeSelected = { hour ->
                            val theme = neuralEngine.generateTimeBasedTheme(hour)
                            themeManager.setTheme(theme)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WallpaperAnalysisCard(
    wallpaperColors: WallpaperColorPalette,
    onImageSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Wallpaper Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Color palette preview
            if (wallpaperColors.dominantColors.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    wallpaperColors.dominantColors.take(5).forEach { color ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(androidx.compose.ui.graphics.Color(color))
                        )
                    }
                }
            } else {
                Button(
                    onClick = { /* Open image picker */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Wallpaper")
                }
            }
        }
    }
}

@Composable
private fun MoodSelectionCard(
    selectedMood: UserMood,
    onMoodSelected: (UserMood) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "How are you feeling?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(UserMood.entries) { mood ->
                    FilterChip(
                        selected = selectedMood == mood,
                        onClick = { onMoodSelected(mood) },
                        label = { Text(mood.name) },
                        leadingIcon = if (selectedMood == mood) {
                            {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
private fun TextToThemeCard(
    textPrompt: String,
    onTextChange: (String) -> Unit,
    onGenerate: () -> Unit,
    isGenerating: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Describe Your Theme",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = textPrompt,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., 'cyberpunk neon Tokyo night'") },
                maxLines = 3
            )

            Button(
                onClick = onGenerate,
                modifier = Modifier.fillMaxWidth(),
                enabled = textPrompt.isNotEmpty() && !isGenerating
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.AutoAwesome, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isGenerating) "Generating..." else "Generate Theme")
            }
        }
    }
}

@Composable
private fun ThemeSuggestionCard(
    suggestion: ThemeSuggestion,
    onApply: () -> Unit
) {
    Card(
        onClick = onApply,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = suggestion.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = suggestion.description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Apply"
            )
        }
    }
}

@Composable
private fun ThemeDNACard(
    dna: ThemeDNA,
    onExport: () -> Unit,
    onMutate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Theme DNA",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Generation: ${dna.metadata.generation}",
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onExport,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export")
                }

                Button(
                    onClick = onMutate,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Biotech, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mutate")
                }
            }
        }
    }
}

@Composable
private fun TimeBasedThemesCard(
    currentHour: Int,
    timeThemes: List<Pair<String, String>>,
    onThemeSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Time-Based Themes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            timeThemes.forEach { (name, timeRange) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onThemeSelected(currentHour) }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(name)
                    Text(
                        text = timeRange,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

private suspend fun loadBitmapFromUrl(context: android.content.Context, url: String): Bitmap? {
    return try {
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .build()
        val result = coil.imageEngine.execute(request)
        (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
    } catch (e: Exception) {
        null
    }
}
