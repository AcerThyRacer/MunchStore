package com.sugarmunch.app.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfectionerScreen(
    onBack: () -> Unit,
    onAppClick: (String) -> Unit
) {
    val context = LocalContext.current
    val engine = remember { RecommendationEngine.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    var recommendations by remember { mutableStateOf<List<Recommendation>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        recommendations = engine.getRecommendations(limit = 10)
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confectioner", color = colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            AnimatedThemeBackground()
            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Recommended for you",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.primary
                        )
                        Text(
                            "Hidden gems based on your taste",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.8f)
                        )
                    }
                    items(recommendations, key = { it.app.id }) { rec ->
                        Card(
                            onClick = { onAppClick(rec.app.id) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.95f))
                        ) {
                            Row(
                                Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("\uD83C\uDF6C", style = MaterialTheme.typography.headlineSmall)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        rec.app.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        rec.reason.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
