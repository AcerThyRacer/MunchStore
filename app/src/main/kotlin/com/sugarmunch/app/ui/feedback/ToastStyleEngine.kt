package com.sugarmunch.app.ui.feedback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens
import kotlinx.coroutines.delay

enum class ToastStyle {
    CANDY_BANNER,
    FLOATING_BUBBLE,
    SLIDE_IN_CARD,
    NEON_FLASH
}

enum class ToastType {
    SUCCESS, ERROR, WARNING, INFO, ACHIEVEMENT, REWARD
}

data class SugarToastData(
    val message: String,
    val type: ToastType = ToastType.INFO,
    val style: ToastStyle = ToastStyle.FLOATING_BUBBLE,
    val icon: ImageVector? = null,
    val duration: Long = 3000L,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    internal val id: Long = System.nanoTime()
)

class SugarToastState {
    private val _toasts = mutableStateListOf<SugarToastData>()
    val toasts: List<SugarToastData> get() = _toasts

    fun show(toast: SugarToastData) {
        _toasts.add(toast)
    }

    fun show(
        message: String,
        type: ToastType = ToastType.INFO,
        style: ToastStyle = ToastStyle.FLOATING_BUBBLE
    ) {
        show(SugarToastData(message = message, type = type, style = style))
    }

    fun dismiss(toast: SugarToastData) {
        _toasts.remove(toast)
    }
}

@Composable
fun rememberSugarToastState(): SugarToastState {
    return remember { SugarToastState() }
}

val LocalSugarToast = staticCompositionLocalOf<SugarToastState> {
    error("No SugarToastState provided. Wrap your content with a CompositionLocalProvider.")
}

@Composable
fun SugarToastHost(
    state: SugarToastState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        state.toasts.forEach { toast ->
            when (toast.style) {
                ToastStyle.CANDY_BANNER -> CandyBannerToast(toast, state)
                ToastStyle.FLOATING_BUBBLE -> FloatingBubbleToast(toast, state)
                ToastStyle.SLIDE_IN_CARD -> SlideInCardToast(toast, state)
                ToastStyle.NEON_FLASH -> NeonFlashToast(toast, state)
            }
        }
    }
}

private fun ToastType.backgroundColor(): Color = when (this) {
    ToastType.SUCCESS -> Color(0xFF4CAF50)
    ToastType.ERROR -> Color(0xFFE53935)
    ToastType.WARNING -> Color(0xFFFF9800)
    ToastType.INFO -> Color(0xFF2196F3)
    ToastType.ACHIEVEMENT -> Color(0xFFFFD700)
    ToastType.REWARD -> Color(0xFFFF69B4)
}

private fun ToastType.contentColor(): Color = when (this) {
    ToastType.ACHIEVEMENT -> Color(0xFF3E2723)
    else -> Color.White
}

private fun ToastType.defaultIcon(): ImageVector = when (this) {
    ToastType.SUCCESS -> Icons.Filled.CheckCircle
    ToastType.ERROR -> Icons.Filled.Close
    ToastType.WARNING -> Icons.Filled.Warning
    ToastType.INFO -> Icons.Filled.Info
    ToastType.ACHIEVEMENT -> Icons.Filled.Star
    ToastType.REWARD -> Icons.Filled.Star
}

// ── CANDY_BANNER ────────────────────────────────────────────────────────────────

@Composable
private fun CandyBannerToast(toast: SugarToastData, state: SugarToastState) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(toast.id) {
        visible = true
        delay(toast.duration)
        visible = false
        delay(300)
        state.dismiss(toast)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(tween(350)) { -it } + fadeIn(tween(250)),
            exit = slideOutVertically(tween(300)) { -it } + fadeOut(tween(200))
        ) {
            val bg = toast.type.backgroundColor()
            val fg = toast.type.contentColor()
            val shape = RoundedCornerShape(
                bottomStart = SugarDimens.Radius.md,
                bottomEnd = SugarDimens.Radius.md
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bg, shape)
                    .clip(shape)
                    .padding(
                        horizontal = SugarDimens.Spacing.md,
                        vertical = SugarDimens.Spacing.sm
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                Icon(
                    imageVector = toast.icon ?: toast.type.defaultIcon(),
                    contentDescription = null,
                    tint = fg,
                    modifier = Modifier.size(SugarDimens.IconSize.md)
                )
                Text(
                    text = toast.message,
                    color = fg,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f)
                )
                toast.actionLabel?.let { label ->
                    TextButton(onClick = {
                        toast.onAction?.invoke()
                        state.dismiss(toast)
                    }) {
                        Text(label, color = fg.copy(alpha = 0.9f), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ── FLOATING_BUBBLE ─────────────────────────────────────────────────────────────

@Composable
private fun FloatingBubbleToast(toast: SugarToastData, state: SugarToastState) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(toast.id) {
        visible = true
        delay(toast.duration)
        visible = false
        delay(300)
        state.dismiss(toast)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                initialScale = 0.8f
            ) + fadeIn(tween(200)),
            exit = scaleOut(tween(200), targetScale = 0.8f) + fadeOut(tween(150))
        ) {
            val bg = toast.type.backgroundColor()
            val fg = toast.type.contentColor()

            Surface(
                shape = RoundedCornerShape(SugarDimens.Radius.pill),
                color = bg,
                shadowElevation = SugarDimens.Elevation.medium,
                modifier = Modifier
                    .padding(bottom = SugarDimens.Spacing.xl)
                    .widthIn(max = 320.dp)
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = SugarDimens.Spacing.md,
                        vertical = SugarDimens.Spacing.sm
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
                ) {
                    Icon(
                        imageVector = toast.icon ?: toast.type.defaultIcon(),
                        contentDescription = null,
                        tint = fg,
                        modifier = Modifier.size(SugarDimens.IconSize.sm)
                    )
                    Text(
                        text = toast.message,
                        color = fg,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// ── SLIDE_IN_CARD ───────────────────────────────────────────────────────────────

@Composable
private fun SlideInCardToast(toast: SugarToastData, state: SugarToastState) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(toast.id) {
        visible = true
        delay(toast.duration)
        visible = false
        delay(300)
        state.dismiss(toast)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(tween(400)) { it } + fadeIn(tween(300)),
            exit = slideOutHorizontally(tween(300)) { it } + fadeOut(tween(200))
        ) {
            val bg = toast.type.backgroundColor()
            val fg = toast.type.contentColor()

            Surface(
                shape = RoundedCornerShape(SugarDimens.Radius.md),
                color = bg,
                shadowElevation = SugarDimens.Elevation.high,
                modifier = Modifier
                    .padding(top = SugarDimens.Spacing.lg, end = SugarDimens.Spacing.sm)
                    .widthIn(max = 300.dp)
            ) {
                Row(
                    modifier = Modifier.padding(SugarDimens.Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = toast.icon ?: toast.type.defaultIcon(),
                        contentDescription = null,
                        tint = fg,
                        modifier = Modifier.size(SugarDimens.IconSize.md)
                    )
                    Spacer(Modifier.width(SugarDimens.Spacing.sm))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = toast.message,
                            color = fg,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        toast.actionLabel?.let { label ->
                            Text(
                                text = label,
                                color = fg.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier
                                    .padding(top = SugarDimens.Spacing.xxs)
                                    .clickable {
                                        toast.onAction?.invoke()
                                        state.dismiss(toast)
                                    }
                            )
                        }
                    }
                    IconButton(onClick = { state.dismiss(toast) }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Dismiss",
                            tint = fg.copy(alpha = 0.7f),
                            modifier = Modifier.size(SugarDimens.IconSize.sm)
                        )
                    }
                }
            }
        }
    }
}

// ── NEON_FLASH ──────────────────────────────────────────────────────────────────

@Composable
private fun NeonFlashToast(toast: SugarToastData, state: SugarToastState) {
    val effectiveDuration = minOf(toast.duration, 1500L)
    var visible by remember { mutableStateOf(false) }

    val neonAlpha by animateFloatAsState(
        targetValue = if (visible) 0.3f else 1f,
        animationSpec = tween(durationMillis = effectiveDuration.toInt()),
        label = "neonGlow"
    )

    LaunchedEffect(toast.id) {
        visible = true
        delay(effectiveDuration)
        visible = false
        delay(200)
        state.dismiss(toast)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(100)),
            exit = fadeOut(tween(150))
        ) {
            val base = toast.type.backgroundColor()
            val fg = toast.type.contentColor()
            val borderColor = base.copy(alpha = neonAlpha)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SugarDimens.Spacing.sm, vertical = SugarDimens.Spacing.xs)
                    .border(2.dp, borderColor, RoundedCornerShape(SugarDimens.Radius.sm))
                    .shadow(SugarDimens.Elevation.medium, RoundedCornerShape(SugarDimens.Radius.sm))
                    .background(
                        base.copy(alpha = 0.15f),
                        RoundedCornerShape(SugarDimens.Radius.sm)
                    )
                    .padding(
                        horizontal = SugarDimens.Spacing.md,
                        vertical = SugarDimens.Spacing.sm
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                Icon(
                    imageVector = toast.icon ?: toast.type.defaultIcon(),
                    contentDescription = null,
                    tint = base,
                    modifier = Modifier.size(SugarDimens.IconSize.sm)
                )
                Text(
                    text = toast.message,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
    }
}
