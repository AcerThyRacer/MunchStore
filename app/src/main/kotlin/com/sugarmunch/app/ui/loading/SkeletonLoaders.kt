package com.sugarmunch.app.ui.loading

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens

@Composable
fun shimmerBrush(
    targetValue: Float = 1000f,
    baseColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    highlightColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    return Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(translateAnim - targetValue * 0.3f, 0f),
        end = Offset(translateAnim, 0f)
    )
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(SugarDimens.Radius.sm)
) {
    val brush = shimmerBrush()
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

// ---------------------------------------------------------------------------
// Catalog skeleton – grid of card-shaped shimmers
// ---------------------------------------------------------------------------

@Composable
fun CatalogSkeleton(
    columns: Int = 2,
    itemCount: Int = 8,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier
            .fillMaxSize()
            .padding(SugarDimens.Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm),
        userScrollEnabled = false
    ) {
        items(itemCount) {
            CatalogCardSkeleton()
        }
    }
}

@Composable
private fun CatalogCardSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SugarDimens.Radius.md))
            .background(MaterialTheme.colorScheme.surface)
            .padding(SugarDimens.Spacing.sm)
    ) {
        // Icon placeholder
        ShimmerBox(
            modifier = Modifier
                .size(SugarDimens.IconSize.xxl)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(SugarDimens.Radius.sm)
        )

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))

        // Title line
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(14.dp)
        )

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))

        // Subtitle line
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(10.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// Detail skeleton – hero image + title + description + buttons
// ---------------------------------------------------------------------------

@Composable
fun DetailSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SugarDimens.Spacing.md)
    ) {
        // Hero image
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(SugarDimens.Radius.lg)
        )

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.lg))

        // Title
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(22.dp)
        )

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))

        // Subtitle / developer
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.35f)
                .height(14.dp)
        )

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xl))

        // Description lines
        repeat(4) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(if (it == 3) 0.5f else 1f)
                    .height(12.dp)
            )
            if (it < 3) Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))
        }

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xl))

        // Button row
        Row(horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)) {
            ShimmerBox(
                modifier = Modifier
                    .weight(1f)
                    .height(SugarDimens.Height.button),
                shape = RoundedCornerShape(SugarDimens.Radius.pill)
            )
            ShimmerBox(
                modifier = Modifier
                    .weight(1f)
                    .height(SugarDimens.Height.button),
                shape = RoundedCornerShape(SugarDimens.Radius.pill)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Effects list skeleton – horizontal cards with toggle
// ---------------------------------------------------------------------------

@Composable
fun EffectsListSkeleton(
    itemCount: Int = 6,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SugarDimens.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
    ) {
        repeat(itemCount) {
            EffectRowSkeleton()
        }
    }
}

@Composable
private fun EffectRowSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SugarDimens.Radius.md))
            .background(MaterialTheme.colorScheme.surface)
            .padding(SugarDimens.Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Effect icon
        ShimmerBox(
            modifier = Modifier.size(SugarDimens.IconSize.xl),
            shape = CircleShape
        )

        Spacer(modifier = Modifier.width(SugarDimens.Spacing.md))

        // Text block
        Column(modifier = Modifier.weight(1f)) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(14.dp)
            )
            Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxs))
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(10.dp)
            )
        }

        Spacer(modifier = Modifier.width(SugarDimens.Spacing.sm))

        // Toggle placeholder
        ShimmerBox(
            modifier = Modifier
                .width(44.dp)
                .height(24.dp),
            shape = RoundedCornerShape(SugarDimens.Radius.pill)
        )
    }
}

// ---------------------------------------------------------------------------
// Shop skeleton – points banner + item grid
// ---------------------------------------------------------------------------

@Composable
fun ShopSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SugarDimens.Spacing.md)
    ) {
        // Points banner
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = RoundedCornerShape(SugarDimens.Radius.lg)
        )

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.lg))

        // Section header
        ShimmerBox(
            modifier = Modifier
                .width(100.dp)
                .height(16.dp)
        )

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))

        // Item grid (2 columns, 6 items)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm),
            userScrollEnabled = false
        ) {
            items(6) {
                ShopItemSkeleton()
            }
        }
    }
}

@Composable
private fun ShopItemSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SugarDimens.Radius.md))
            .background(MaterialTheme.colorScheme.surface)
            .padding(SugarDimens.Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Item image
        ShimmerBox(
            modifier = Modifier
                .size(SugarDimens.IconSize.hero)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(SugarDimens.Radius.sm)
        )

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))

        // Name
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(12.dp)
        )

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxs))

        // Price tag
        ShimmerBox(
            modifier = Modifier
                .width(48.dp)
                .height(10.dp),
            shape = RoundedCornerShape(SugarDimens.Radius.xs)
        )
    }
}

// ---------------------------------------------------------------------------
// Settings skeleton – section headers + setting rows
// ---------------------------------------------------------------------------

@Composable
fun SettingsSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SugarDimens.Spacing.md)
    ) {
        repeat(3) { sectionIndex ->
            // Section header
            ShimmerBox(
                modifier = Modifier
                    .width(80.dp)
                    .height(12.dp)
            )

            Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))

            // Setting rows
            val rowCount = if (sectionIndex == 0) 4 else 3
            repeat(rowCount) {
                SettingRowSkeleton()
                if (it < rowCount - 1) {
                    Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxs))
                }
            }

            if (sectionIndex < 2) {
                Spacer(modifier = Modifier.height(SugarDimens.Spacing.xl))
            }
        }
    }
}

@Composable
private fun SettingRowSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(SugarDimens.Height.listItem)
            .padding(horizontal = SugarDimens.Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon placeholder
        ShimmerBox(
            modifier = Modifier.size(SugarDimens.IconSize.md),
            shape = CircleShape
        )

        Spacer(modifier = Modifier.width(SugarDimens.Spacing.md))

        // Label
        ShimmerBox(
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
        )

        Spacer(modifier = Modifier.width(SugarDimens.Spacing.md))

        // Value / chevron
        ShimmerBox(
            modifier = Modifier
                .width(40.dp)
                .height(14.dp),
            shape = RoundedCornerShape(SugarDimens.Radius.xs)
        )
    }
}

// ---------------------------------------------------------------------------
// Profile skeleton – avatar + name + stats + content grid
// ---------------------------------------------------------------------------

@Composable
fun ProfileSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SugarDimens.Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xl))

        // Avatar circle
        ShimmerBox(
            modifier = Modifier.size(80.dp),
            shape = CircleShape
        )

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))

        // Name
        ShimmerBox(
            modifier = Modifier
                .width(140.dp)
                .height(18.dp)
        )

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))

        // Handle / subtitle
        ShimmerBox(
            modifier = Modifier
                .width(100.dp)
                .height(12.dp)
        )

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xl))

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(3) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(36.dp)
                            .height(18.dp)
                    )
                    Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxs))
                    ShimmerBox(
                        modifier = Modifier
                            .width(48.dp)
                            .height(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxl))

        // Content grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs),
            userScrollEnabled = false
        ) {
            items(9) {
                ShimmerBox(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(SugarDimens.Radius.sm)
                )
            }
        }
    }
}
