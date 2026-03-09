package com.sugarmunch.app.ui.customization

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens
import java.util.UUID

/**
 * EXTREME User Profile Management Screen
 * Multiple profiles, switching, and sharing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileManagementScreen(
    onNavigateBack: () -> Unit,
    profiles: List<UserProfile>,
    activeProfileId: String?,
    onProfileSelected: (String) -> Unit,
    onProfileAdded: (UserProfile) -> Unit,
    onProfileUpdated: (UserProfile) -> Unit,
    onProfileDeleted: (String) -> Unit
) {
    var showCreateProfile by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<UserProfile?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profiles") },
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { showCreateProfile = true }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Add,
                            contentDescription = "Add Profile"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.lg)
        ) {
            // Active Profile
            if (activeProfileId != null) {
                val activeProfile = profiles.find { it.id == activeProfileId }
                if (activeProfile != null) {
                    item {
                        ActiveProfileCard(
                            profile = activeProfile,
                            onEdit = { editingProfile = activeProfile }
                        )
                    }
                }
            }

            // Profile List
            item {
                Text(
                    text = "All Profiles",
                    style = MaterialTheme.typography.titleMedium,
                    color = SugarDimens.Brand.hotPink
                )
            }

            items(profiles) { profile ->
                ProfileRow(
                    profile = profile,
                    isActive = profile.id == activeProfileId,
                    onSelect = { onProfileSelected(profile.id) },
                    onEdit = { editingProfile = profile },
                    onDelete = { onProfileDeleted(profile.id) }
                )
            }

            // Profile Sharing
            item {
                ProfileSharingSection(
                    profiles = profiles
                )
            }

            // Profile Statistics
            item {
                ProfileStatisticsSection(
                    profiles = profiles
                )
            }
        }
    }

    // Create/Edit Profile Dialog
    if (showCreateProfile || editingProfile != null) {
        ProfileEditorDialog(
            profile = editingProfile,
            onDismiss = {
                showCreateProfile = false
                editingProfile = null
            },
            onSave = { profile ->
                if (editingProfile != null) {
                    onProfileUpdated(profile.copy(id = editingProfile!!.id))
                } else {
                    onProfileAdded(profile.copy(id = UUID.randomUUID().toString()))
                }
                showCreateProfile = false
                editingProfile = null
            }
        )
    }
}

@Composable
private fun ActiveProfileCard(
    profile: UserProfile,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = SugarDimens.Brand.hotPink.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(android.graphics.Color.parseColor(profile.iconColor))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.iconId.first().toString(),
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Active Profile",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = SugarDimens.Brand.hotPink
                        )
                    }
                }
                AssistChip(
                    onClick = onEdit,
                    label = { Text("Edit") },
                    leadingIcon = {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            if (profile.description.isNotEmpty()) {
                Text(
                    text = profile.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.lg)
            ) {
                ProfileStat(
                    label = "Usage",
                    value = "${profile.usageCount} times"
                )
                ProfileStat(
                    label = "Last Used",
                    value = formatTimestamp(profile.lastUsedAt)
                )
            }
        }
    }
}

@Composable
private fun ProfileRow(
    profile: UserProfile,
    isActive: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(SugarDimens.Radius.md),
        color = if (isActive) {
            SugarDimens.Brand.hotPink.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SugarDimens.Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(android.graphics.Color.parseColor(profile.iconColor))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.iconId.first().toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                }
                Column {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = profile.category.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                if (isActive) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Active") },
                        leadingIcon = {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.Edit,
                        contentDescription = "Edit"
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSharingSection(
    profiles: List<UserProfile>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Text(
                text = "Profile Sharing",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.mint
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                OutlinedButton(
                    onClick = { /* Export profile */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Export as File")
                }
                OutlinedButton(
                    onClick = { /* Export as code */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Export as Code")
                }
            }

            OutlinedButton(
                onClick = { /* Import profile */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Import Profile")
            }

            OutlinedButton(
                onClick = { /* Browse community */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Browse Community Profiles")
            }
        }
    }
}

@Composable
private fun ProfileStatisticsSection(
    profiles: List<UserProfile>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.candyOrange
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ProfileStat(
                    label = "Total Profiles",
                    value = profiles.size.toString()
                )
                ProfileStat(
                    label = "Total Usage",
                    value = profiles.sumOf { it.usageCount }.toString()
                )
                ProfileStat(
                    label = "Favorites",
                    value = profiles.count { it.isFavorite }.toString()
                )
            }
        }
    }
}

@Composable
private fun ProfileStat(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = SugarDimens.Brand.hotPink
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ProfileEditorDialog(
    profile: UserProfile?,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var description by remember { mutableStateOf(profile?.description ?: "") }
    var category by remember { mutableStateOf(profile?.category ?: ProfileCategory.CUSTOM) }
    var iconColor by remember { mutableStateOf(profile?.iconColor ?: "#FFFF69B4") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (profile == null) "Create Profile" else "Edit Profile") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Profile Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Text("Category", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
                ) {
                    ProfileCategory.entries.take(4).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedProfile = (profile ?: UserProfile(
                        id = "",
                        name = name
                    )).copy(
                        name = name,
                        description = description,
                        category = category,
                        iconColor = iconColor
                    )
                    onSave(updatedProfile)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> "${diff / 86400000}d ago"
    }
}
