package com.sugarmunch.app.ui.screens.clan

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.clan.ClanManager
import com.sugarmunch.app.clan.model.*
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClanListScreen(
    onBack: () -> Unit,
    onClanSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val clanManager = remember { ClanManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(ClanFilter.ALL) }
    var clans by remember { mutableStateOf<List<ClanPreview>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf<Clan?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    
    // Load clans
    LaunchedEffect(searchQuery, selectedFilter) {
        isLoading = true
        clans = when {
            searchQuery.isNotEmpty() -> {
                clanManager.searchClans(searchQuery).mapNotNull { 
                    clanManager.getClanDetails(it.id) 
                }
            }
            selectedFilter == ClanFilter.RECOMMENDED -> {
                clanManager.getRecommendedClans().mapNotNull {
                    clanManager.getClanDetails(it.id)
                }
            }
            selectedFilter == ClanFilter.TOP -> {
                clanManager.getTopClans(50).mapNotNull {
                    clanManager.getClanDetails(it.id)
                }
            }
            else -> {
                clanManager.getTopClans(100).mapNotNull {
                    clanManager.getClanDetails(it.id)
                }
            }
        }
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Find a Clan",
                        color = colors.onSurface,
                        style = MaterialTheme.typography.headlineSmall
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = colors.primary,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Create Clan") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedThemeBackground()
            
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Search Bar
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    colors = colors,
                    modifier = Modifier.padding(16.dp)
                )
                
                // Filter Chips
                FilterChipsRow(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it },
                    colors = colors
                )
                
                // Results Count
                Text(
                    text = "${clans.size} clans found",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                // Clan List
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                } else if (clans.isEmpty()) {
                    EmptyClanList(
                        colors = colors,
                        isSearching = searchQuery.isNotEmpty()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(clans) { preview ->
                            ClanCard(
                                preview = preview,
                                colors = colors,
                                onClick = { onClanSelected(preview.clan.id) },
                                onJoin = { showJoinDialog = preview.clan }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Join Dialog
    showJoinDialog?.let { clan ->
        JoinClanDialog(
            clan = clan,
            colors = colors,
            onDismiss = { showJoinDialog = null },
            onConfirm = { message ->
                scope.launch {
                    val result = clanManager.joinClan(clan.id, message)
                    showJoinDialog = null
                    // Show result snackbar or navigate
                }
            }
        )
    }
    
    // Create Clan Dialog
    if (showCreateDialog) {
        CreateClanDialog(
            colors = colors,
            onDismiss = { showCreateDialog = false },
            onCreate = { name, tag, description ->
                scope.launch {
                    val result = clanManager.createClan(
                        name = name,
                        tag = tag,
                        description = description
                    )
                    if (result.success) {
                        showCreateDialog = false
                        // Navigate to clan screen
                    }
                }
            }
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search clans by name or tag...") },
        leadingIcon = {
            Icon(Icons.Default.Search, null, tint = colors.onSurface.copy(alpha = 0.5f))
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, null, tint = colors.onSurface.copy(alpha = 0.5f))
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surface.copy(alpha = 0.9f),
            unfocusedContainerColor = colors.surface.copy(alpha = 0.9f),
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.surfaceVariant
        )
    )
}

@Composable
private fun FilterChipsRow(
    selectedFilter: ClanFilter,
    onFilterSelected: (ClanFilter) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ClanFilter.values().forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.displayName) },
                leadingIcon = if (selectedFilter == filter) {
                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primary.copy(alpha = 0.2f),
                    selectedLabelColor = colors.primary
                )
            )
        }
    }
}

@Composable
private fun ClanCard(
    preview: ClanPreview,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit,
    onJoin: () -> Unit
) {
    val clan = preview.clan
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clan Emblem
                Surface(
                    shape = CircleShape,
                    color = Color(android.graphics.Color.parseColor(clan.primaryColor)).copy(alpha = 0.3f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = clan.emblem,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "[${clan.tag}]",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = clan.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = clan.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Member count
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = colors.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${preview.memberCount}/${clan.maxMembers}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.6f)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Trophies
                        Text(
                            text = "🏆 ${clan.trophies}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.6f)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Level
                        Text(
                            text = "⭐ Lv.${clan.level}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                // Join Button
                if (preview.isJoinable) {
                    Button(
                        onClick = onJoin,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Join")
                    }
                } else {
                    // Show join policy badge
                    val (icon, label) = when (clan.joinPolicy) {
                        ClanJoinPolicy.OPEN -> "🔓" to "Open"
                        ClanJoinPolicy.REQUEST -> "🔔" to "Apply"
                        ClanJoinPolicy.INVITE_ONLY -> "🔒" to "Invite"
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(icon, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                label,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            // Requirements hint
            if (clan.minLevelToJoin > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🔒 Requires Level ${clan.minLevelToJoin}+",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun EmptyClanList(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    isSearching: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isSearching) "🔍" else "🛡️",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (isSearching) "No clans found" else "No clans available",
            style = MaterialTheme.typography.titleLarge,
            color = colors.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (isSearching) {
                "Try a different search term or check your spelling"
            } else {
                "Be the first to create a clan!"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun JoinClanDialog(
    clan: Clan,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var message by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(clan.emblem, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Join [${clan.tag}]?")
            }
        },
        text = {
            Column {
                Text(
                    "You are about to request to join ${clan.name}.",
                    color = colors.onSurface
                )
                
                if (clan.joinPolicy == ClanJoinPolicy.REQUEST) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message (optional)") },
                        placeholder = { Text("Tell them why you want to join...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.surfaceVariant
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(message) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text(
                    when (clan.joinPolicy) {
                        ClanJoinPolicy.OPEN -> "Join Now"
                        ClanJoinPolicy.REQUEST -> "Send Request"
                        ClanJoinPolicy.INVITE_ONLY -> "Join"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.onSurface)
            }
        },
        containerColor = colors.surface
    )
}

@Composable
private fun CreateClanDialog(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    val isValid = name.length >= 3 && tag.length in 2..5
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Clan") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Clan Name *") },
                    placeholder = { Text("Enter clan name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.surfaceVariant
                    )
                )
                
                OutlinedTextField(
                    value = tag,
                    onValueChange = { tag = it.uppercase().take(5) },
                    label = { Text("Clan Tag * (2-5 chars)") },
                    placeholder = { Text("TAG") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.surfaceVariant
                    )
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Describe your clan...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.surfaceVariant
                    )
                )
                
                Text(
                    text = "Creating a clan costs 100 Sugar Points",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name.trim(), tag.trim(), description.trim()) },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text("Create Clan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.onSurface)
            }
        },
        containerColor = colors.surface
    )
}

enum class ClanFilter(val displayName: String) {
    ALL("All Clans"),
    RECOMMENDED("Recommended"),
    TOP("Top Ranked"),
    OPEN("Open to Join")
}
