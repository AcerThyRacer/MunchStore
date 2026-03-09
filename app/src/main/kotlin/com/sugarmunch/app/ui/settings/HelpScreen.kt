package com.sugarmunch.app.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager

/**
 * FAQ data class
 */
data class FAQ(
    val id: String,
    val question: String,
    val answer: String,
    val category: FaqCategory,
    val tags: List<String> = emptyList()
)

enum class FaqCategory(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    GETTING_STARTED("Getting Started", Icons.Default.School),
    THEMES("Themes", Icons.Default.Palette),
    EFFECTS("Effects", Icons.Default.AutoAwesome),
    AUTOMATION("Automation", Icons.Default.Build),
    SHOP("Shop", Icons.Default.ShoppingCart),
    SOCIAL("Social", Icons.Default.People),
    TROUBLESHOOTING("Troubleshooting", Icons.Default.BugReport)
}

/**
 * Help article data class
 */
data class HelpArticle(
    val id: String,
    val title: String,
    val summary: String,
    val category: FaqCategory,
    val readTimeMinutes: Int,
    val videoUrl: String? = null
)

/**
 * Support contact data class
 */
data class SupportContact(
    val name: String,
    val type: ContactType,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

enum class ContactType {
    EMAIL,
    DISCORD,
    TWITTER,
    REDDIT,
    WEBSITE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    val uriHandler = LocalUriHandler.current
    
    var selectedCategory by remember { mutableStateOf<FaqCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showContactDialog by remember { mutableStateOf(false) }
    var expandedFaqId by remember { mutableStateOf<String?>(null) }
    
    // Sample FAQs - In production, these would come from a repository
    val faqs = remember {
        listOf(
            FAQ(
                id = "1",
                question = "How do I apply a theme to an app?",
                answer = "To apply a theme: 1) Browse the catalog and select an app, 2) Tap on the app to open its detail screen, 3) Choose a theme from the available options, 4) Adjust intensity sliders to your preference, 5) Tap 'Apply' to activate the theme.",
                category = FaqCategory.GETTING_STARTED,
                tags = listOf("theme", "apply", "beginner")
            ),
            FAQ(
                id = "2",
                question = "How do I create custom themes?",
                answer = "Go to Theme Settings > Theme Builder. You can customize colors, gradients, particles, and animations. Use the 20+ individual sliders for fine-grained control. Save your creation with a custom name to reuse later.",
                category = FaqCategory.THEMES,
                tags = listOf("custom", "create", "builder")
            ),
            FAQ(
                id = "3",
                question = "What are Sugar Points?",
                answer = "Sugar Points (SP) are the in-app currency used to purchase premium themes, effects, and items. Earn SP by completing daily rewards, achievements, quests, clan wars, and trading with other users.",
                category = FaqCategory.SHOP,
                tags = listOf("currency", "points", "rewards")
            ),
            FAQ(
                id = "4",
                question = "How do I join a clan?",
                answer = "Navigate to the Clan screen from the main menu. Browse available clans or search for specific ones. Tap 'Request to Join' on a clan's page. Once accepted by clan leaders, you'll become a member and can participate in clan wars and chat.",
                category = FaqCategory.SOCIAL,
                tags = listOf("clan", "join", "social")
            ),
            FAQ(
                id = "5",
                question = "Why are effects laggy on my device?",
                answer = "Try these solutions: 1) Lower the effect intensity in settings, 2) Enable 'Performance Mode' in Accessibility settings, 3) Reduce the number of active effects, 4) Close background apps, 5) Restart SugarMunch. If issues persist, your device may not support advanced effects.",
                category = FaqCategory.TROUBLESHOOTING,
                tags = listOf("performance", "lag", "effects")
            ),
            FAQ(
                id = "6",
                question = "How do I export my custom themes?",
                answer = "Go to Settings > Account > Export My Data. Choose 'Export Themes' to save your custom themes as JSON files. You can share these files with others or import them on another device.",
                category = FaqCategory.THEMES,
                tags = listOf("export", "backup", "share")
            ),
            FAQ(
                id = "7",
                question = "What is Automation and how do I use it?",
                answer = "Automation lets you create rules that automatically change themes/effects based on triggers like time of day, app usage, battery level, or location. Go to Automation screen > Create Task > Set trigger and action > Save. Your automation will run automatically.",
                category = FaqCategory.AUTOMATION,
                tags = listOf("automation", "rules", "triggers")
            ),
            FAQ(
                id = "8",
                question = "How do I enable accessibility features?",
                answer = "Go to Settings > Accessibility. You'll find options for high contrast, colorblind modes, text scaling, reduced motion, large touch targets, and more. Enable the features you need for a more comfortable experience.",
                category = FaqCategory.GETTING_STARTED,
                tags = listOf("accessibility", "vision", "motor")
            )
        )
    }
    
    // Sample help articles
    val helpArticles = remember {
        listOf(
            HelpArticle(
                id = "a1",
                title = "Complete Guide to Theme Customization",
                summary = "Learn how to create stunning custom themes with advanced controls",
                category = FaqCategory.THEMES,
                readTimeMinutes = 8,
                videoUrl = "https://youtube.com/watch?v=example1"
            ),
            HelpArticle(
                id = "a2",
                title = "Mastering the Effect Composer",
                summary = "Create breathtaking visual effects with our node-based editor",
                category = FaqCategory.EFFECTS,
                readTimeMinutes = 12,
                videoUrl = "https://youtube.com/watch?v=example2"
            ),
            HelpArticle(
                id = "a3",
                title = "Automation Tips and Tricks",
                summary = "Automate your theming experience with smart rules",
                category = FaqCategory.AUTOMATION,
                readTimeMinutes = 6
            )
        )
    }
    
    // Support contacts
    val supportContacts = remember {
        listOf(
            SupportContact(
                name = "Email Support",
                type = ContactType.EMAIL,
                value = "support@sugarmunch.app",
                icon = Icons.Default.Email
            ),
            SupportContact(
                name = "Discord Community",
                type = ContactType.DISCORD,
                value = "https://discord.gg/sugarmunch",
                icon = Icons.Default.Forum
            ),
            SupportContact(
                name = "Twitter",
                type = ContactType.TWITTER,
                value = "@SugarMunchApp",
                icon = Icons.Default.Share
            ),
            SupportContact(
                name = "Reddit",
                type = ContactType.REDDIT,
                value = "r/SugarMunch",
                icon = Icons.Default.People
            )
        )
    }

    // Filter FAQs
    val filteredFaqs = remember(faqs, selectedCategory, searchQuery) {
        faqs.filter { faq ->
            val matchesCategory = selectedCategory == null || faq.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                faq.question.contains(searchQuery, ignoreCase = true) ||
                faq.answer.contains(searchQuery, ignoreCase = true) ||
                faq.tags.any { it.contains(searchQuery, ignoreCase = true) }
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Help & Support",
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
            AnimatedThemeBackground()

            LazyColumn(
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search Bar
                item {
                    SearchBarCard(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        colors = colors
                    )
                }

                // Category Filters
                item {
                    CategoryFilterRow(
                        selectedCategory = selectedCategory,
                        onCategorySelect = { selectedCategory = it },
                        colors = colors
                    )
                }

                // Quick Links
                item {
                    QuickLinksCard(
                        colors = colors,
                        onTutorialsClick = { /* Show tutorials */ },
                        onVideoGuidesClick = {
                            uriHandler.openUri("https://youtube.com/sugarmunch")
                        },
                        onReportBugClick = { showContactDialog = true },
                        onFeatureRequestClick = { showContactDialog = true }
                    )
                }

                // FAQ Section
                item {
                    SectionHeader(
                        title = "Frequently Asked Questions",
                        icon = Icons.Default.Help,
                        colors = colors
                    )
                }

                // FAQs
                items(filteredFaqs) { faq ->
                    FaqItem(
                        faq = faq,
                        isExpanded = expandedFaqId == faq.id,
                        onToggle = {
                            expandedFaqId = if (expandedFaqId == faq.id) null else faq.id
                        },
                        colors = colors
                    )
                }

                if (filteredFaqs.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No FAQs Found",
                            description = "Try adjusting your search or category filter",
                            icon = Icons.Default.SearchOff,
                            colors = colors
                        )
                    }
                }

                // Help Articles
                item {
                    SectionHeader(
                        title = "Help Articles & Guides",
                        icon = Icons.Default.Article,
                        colors = colors
                    )
                }

                items(helpArticles) { article ->
                    HelpArticleCard(
                        article = article,
                        colors = colors,
                        onClick = { /* Open article */ }
                    )
                }

                // Contact Support
                item {
                    SectionHeader(
                        title = "Contact Support",
                        icon = Icons.Default.Support,
                        colors = colors
                    )
                }

                item {
                    SupportContactsCard(
                        contacts = supportContacts,
                        colors = colors,
                        onContactClick = { contact ->
                            when (contact.type) {
                                ContactType.EMAIL -> {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "message/rfc822"
                                        putExtra(Intent.EXTRA_EMAIL, arrayOf(contact.value))
                                        putExtra(Intent.EXTRA_SUBJECT, "SugarMunch Support Request")
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Send Email"))
                                }
                                ContactType.DISCORD,
                                ContactType.TWITTER,
                                ContactType.REDDIT,
                                ContactType.WEBSITE -> {
                                    uriHandler.openUri(contact.value)
                                }
                            }
                        }
                    )
                }

                // Community Links
                item {
                    CommunityLinksCard(
                        colors = colors,
                        onRedditClick = { uriHandler.openUri("https://reddit.com/r/SugarMunch") },
                        onDiscordClick = { uriHandler.openUri("https://discord.gg/sugarmunch") },
                        onTwitterClick = { uriHandler.openUri("https://twitter.com/SugarMunchApp") }
                    )
                }

                // App Version
                item {
                    AppVersionCard(
                        version = "2026.1.0",
                        build = "20260101",
                        colors = colors
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBarCard(
    query: String,
    onQueryChange: (String) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = colors.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Search help articles, FAQs...")
                }
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = colors.primary
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = colors.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { })
        )
    }
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: FaqCategory?,
    onCategorySelect: (FaqCategory?) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelect(null) },
                label = { Text("All") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primary
                )
            )
            
            FaqCategory.entries.take(4).forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelect(category) },
                    label = { Text(category.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.primary
                    )
                )
            }
        }
    }
}

@Composable
private fun QuickLinksCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onTutorialsClick: () -> Unit,
    onVideoGuidesClick: () -> Unit,
    onReportBugClick: () -> Unit,
    onFeatureRequestClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.primary.copy(alpha = 0.1f)
        ),
        border = CardDefaults.outlinedCardBorder.copy(
            brush = Brush.linearGradient(listOf(colors.primary, colors.primary))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Quick Links",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickLinkButton(
                    icon = Icons.Default.School,
                    label = "Tutorials",
                    onClick = onTutorialsClick,
                    colors = colors
                )

                QuickLinkButton(
                    icon = Icons.Default.PlayCircle,
                    label = "Video Guides",
                    onClick = onVideoGuidesClick,
                    colors = colors
                )

                QuickLinkButton(
                    icon = Icons.Default.BugReport,
                    label = "Report Bug",
                    onClick = onReportBugClick,
                    colors = colors
                )

                QuickLinkButton(
                    icon = Icons.Default.Lightbulb,
                    label = "Feature Request",
                    onClick = onFeatureRequestClick,
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun QuickLinkButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colors.primary
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
    }
}

@Composable
private fun FaqItem(
    faq: FAQ,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.8f)
        ),
        onClick = onToggle
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = colors.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = faq.question,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = faq.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    faq.tags.forEach { tag ->
                        AssistChip(
                            onClick = { },
                            label = { Text("#$tag", style = MaterialTheme.typography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = colors.tertiary.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpArticleCard(
    article: HelpArticle,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.8f)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f)
                )

                if (article.videoUrl != null) {
                    Icon(
                        Icons.Default.PlayCircle,
                        contentDescription = "Video available",
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text(article.category.label, style = MaterialTheme.typography.labelSmall) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = colors.primary.copy(alpha = 0.2f)
                    )
                )

                Text(
                    text = "${article.readTimeMinutes} min read",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun SupportContactsCard(
    contacts: List<SupportContact>,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onContactClick: (SupportContact) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            contacts.forEach { contact ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onContactClick(contact) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        contact.icon,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = contact.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurface
                        )
                        Text(
                            text = contact.value,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.6f),
                            textDecoration = TextDecoration.Underline
                        )
                    }

                    Icon(
                        Icons.Default.OpenInNew,
                        contentDescription = "Open",
                        tint = colors.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CommunityLinksCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onRedditClick: () -> Unit,
    onDiscordClick: () -> Unit,
    onTwitterClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.secondary.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Join Our Community",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CommunityButton(
                    icon = Icons.Default.People,
                    label = "Reddit",
                    onClick = onRedditClick,
                    colors = colors
                )

                CommunityButton(
                    icon = Icons.Default.Forum,
                    label = "Discord",
                    onClick = onDiscordClick,
                    colors = colors
                )

                CommunityButton(
                    icon = Icons.Default.Share,
                    label = "Twitter",
                    onClick = onTwitterClick,
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun CommunityButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.secondary
        )
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = colors.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun AppVersionCard(
    version: String,
    build: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "App Version",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.6f)
            )

            Text(
                text = "v$version ($build)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface
            )
        }
    }
}
