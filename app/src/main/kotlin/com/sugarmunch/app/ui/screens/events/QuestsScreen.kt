package com.sugarmunch.app.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.events.ChallengeFrequency
import com.sugarmunch.app.events.ChallengeProgressManager
import com.sugarmunch.app.events.EventChallenge
import com.sugarmunch.app.events.EventChallenges
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val challengeManager = remember { ChallengeProgressManager.getInstance(context) }
    val scope = rememberCoroutineScope()

    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)

    val featuredEventId = remember { featuredEventIdForMonth() }
    val featuredChallenges = remember(featuredEventId) {
        EventChallenges.getChallengesForEvent(featuredEventId)
    }
    val dailyChallenges = remember {
        EventChallenges.ALL_CHALLENGES.filter { it.frequency == ChallengeFrequency.DAILY }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Daily", "Featured")
    val displayedChallenges = if (selectedTab == 0) dailyChallenges else featuredChallenges

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Quests", color = colors.onSurface)
                        Text(
                            if (selectedTab == 0) "Refresh your streak every day"
                            else featuredEventId.replace('_', ' ').replaceFirstChar(Char::titlecase),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                colors.background.copy(alpha = 0.95f),
                                colors.background.copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                QuestHeaderCard(
                    title = if (selectedTab == 0) "Daily Quest Rotation" else "Featured Event Quests",
                    subtitle = if (selectedTab == 0) {
                        "Claim rewards, keep your streak alive, and build pass progress."
                    } else {
                        "These quests are driven by the current seasonal event challenge catalog."
                    },
                    colors = colors
                )

                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedChallenges, key = { it.id }) { challenge ->
                        val progress by challengeManager.getProgressFlow(challenge.id)
                            .collectAsState(initial = null)

                        QuestCard(
                            challenge = challenge,
                            progressPercent = progress?.progressPercent ?: 0f,
                            currentValue = progress?.currentValue ?: 0,
                            isCompleted = progress?.isCompleted == true,
                            isClaimed = progress?.isClaimed == true,
                            onClaim = {
                                scope.launch {
                                    challengeManager.claimChallengeReward(challenge.id)
                                }
                            },
                            colors = colors
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestHeaderCard(
    title: String,
    subtitle: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.95f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = colors.primary
                )
                Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun QuestCard(
    challenge: EventChallenge,
    progressPercent: Float,
    currentValue: Int,
    isCompleted: Boolean,
    isClaimed: Boolean,
    onClaim: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.95f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    Text(
                        challenge.icon,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                    Column {
                        Text(
                            challenge.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            challenge.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        "+${challenge.rewardPoints} pts",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = colors.primary,
                trackColor = colors.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = colors.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        "$currentValue / ${challenge.targetValue}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.75f)
                    )
                }

                when {
                    isClaimed -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text(
                                "Claimed",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    isCompleted -> {
                        Button(onClick = onClaim) {
                            Text("Claim")
                        }
                    }

                    else -> {
                        OutlinedButton(onClick = {}, enabled = false) {
                            Text("In Progress")
                        }
                    }
                }
            }
        }
    }
}

private fun featuredEventIdForMonth(): String {
    return when (Calendar.getInstance().get(Calendar.MONTH)) {
        Calendar.OCTOBER -> "halloween_spooktacular"
        Calendar.NOVEMBER, Calendar.DECEMBER, Calendar.JANUARY -> "winter_wonderland"
        Calendar.FEBRUARY -> "valentines_sweetheart"
        Calendar.MARCH, Calendar.APRIL, Calendar.MAY -> "spring_blossom"
        else -> "summer_splash"
    }
}
