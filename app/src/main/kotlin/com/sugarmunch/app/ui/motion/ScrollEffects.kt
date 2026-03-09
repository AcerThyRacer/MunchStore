package com.sugarmunch.app.ui.motion

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * EXTREME Scroll Effects for SugarMunch
 * Parallax, wave animations, magnetic edges, and trail effects
 */

/**
 * Scroll effect type
 */
enum class ScrollEffectType {
    PARALLAX,
    WAVE,
    MAGNETIC,
    TRAIL,
    FADE,
    SCALE,
    ROTATE,
    SKEW
}

/**
 * Parallax scroll effect
 * Creates depth layers that move at different speeds
 */
@Composable
fun ParallaxScrollList(
    items: List<String>,
    modifier: Modifier = Modifier,
    parallaxStrength: Float = 0.5f,
    layerCount: Int = 3,
    content: @Composable (String, Float) -> Unit
) {
    val listState = rememberLazyListState()
    val scrollOffset = remember { mutableStateOf(0f) }
    
    LaunchedEffect(listState.firstVisibleItemScrollOffset) {
        scrollOffset.value = listState.firstVisibleItemIndex * 100f + 
                            listState.firstVisibleItemScrollOffset
    }
    
    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        items(items) { item ->
            val parallaxOffset = scrollOffset.value * parallaxStrength
            
            content(item, parallaxOffset)
        }
    }
}

/**
 * Wave scroll effect
 * Items wave in as they enter viewport
 */
@Composable
fun WaveScrollList(
    items: List<String>,
    modifier: Modifier = Modifier,
    waveAmplitude: Float = 50f,
    waveFrequency: Float = 0.1f,
    content: @Composable (String, Float) -> Unit
) {
    val listState = rememberLazyListState()
    val waveOffset = remember { Animatable(0f) }
    
    LaunchedEffect(listState.firstVisibleItemIndex) {
        waveOffset.animateTo(
            waveOffset.value + 360f,
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        )
    }
    
    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        items(items) { item ->
            val wavePhase = waveOffset.value * waveFrequency
            val waveY = kotlin.math.sin(wavePhase) * waveAmplitude
            
            content(item, waveY)
        }
    }
}

/**
 * Magnetic edge effect
 * Items are attracted to screen edges
 */
@Composable
fun MagneticEdgeList(
    items: List<String>,
    modifier: Modifier = Modifier,
    magneticStrength: Float = 0.3f,
    edgeDistance: Float = 200f,
    content: @Composable (String, Offset) -> Unit
) {
    val listState = rememberLazyListState()
    
    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        items(items) { item ->
            // Calculate magnetic offset based on position
            val magneticOffset = Offset(
                x = 0f, // Would calculate based on edge proximity
                y = 0f
            )
            
            content(item, magneticOffset)
        }
    }
}

/**
 * Trail effect
 * Items leave a visual trail as they scroll
 */
@Composable
fun TrailScrollList(
    items: List<String>,
    modifier: Modifier = Modifier,
    trailLength: Int = 5,
    trailFade: Float = 0.3f,
    content: @Composable (String, List<Float>) -> Unit
) {
    val listState = rememberLazyListState()
    val trailPositions = remember { mutableStateListOf<List<Float>>() }
    
    LaunchedEffect(listState.firstVisibleItemScrollOffset) {
        val currentPosition = listState.firstVisibleItemIndex.toFloat() + 
                             (listState.firstVisibleItemScrollOffset / 100f)
        
        trailPositions.add(0, listOf(currentPosition))
        if (trailPositions.size > trailLength) {
            trailPositions.removeAt(trailPositions.lastIndex)
        }
    }
    
    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        items(items) { item ->
            content(item, trailPositions.flatten())
        }
    }
}

/**
 * Fade scroll effect
 * Items fade in/out based on scroll position
 */
@Composable
fun FadeScrollList(
    items: List<String>,
    modifier: Modifier = Modifier,
    fadeRange: Float = 100f,
    content: @Composable (String, Float) -> Unit
) {
    val listState = rememberLazyListState()
    
    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        items(items) { item ->
            val itemPosition = listState.layoutInfo.visibleItemsInfo
                .find { it.index == items.indexOf(item) }
            
            val alpha = if (itemPosition != null) {
                val distanceFromCenter = abs(itemPosition.offset - 300)
                max(0f, 1f - (distanceFromCenter / fadeRange))
            } else {
                1f
            }
            
            content(item, alpha)
        }
    }
}

/**
 * Scale scroll effect
 * Items scale based on scroll position
 */
@Composable
fun ScaleScrollList(
    items: List<String>,
    modifier: Modifier = Modifier,
    minScale: Float = 0.8f,
    maxScale: Float = 1.1f,
    content: @Composable (String, Float) -> Unit
) {
    val listState = rememberLazyListState()
    
    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        items(items) { item ->
            val itemPosition = listState.layoutInfo.visibleItemsInfo
                .find { it.index == items.indexOf(item) }
            
            val scale = if (itemPosition != null) {
                val distanceFromCenter = abs(itemPosition.offset - 300)
                minScale + (maxScale - minScale) * max(0f, 1f - distanceFromCenter / 500f)
            } else {
                1f
            }
            
            content(item, scale)
        }
    }
}

/**
 * Extreme scroll item with multiple effects
 */
@Composable
fun ExtremeScrollItem(
    item: String,
    scrollProgress: Float,
    effectType: ScrollEffectType = ScrollEffectType.WAVE,
    modifier: Modifier = Modifier
) {
    val animatedScale = remember { Animatable(0.8f) }
    val animatedAlpha = remember { Animatable(0.5f) }
    val animatedOffset = remember { Animatable(0f) }
    
    LaunchedEffect(scrollProgress) {
        when (effectType) {
            ScrollEffectType.SCALE -> {
                animatedScale.animateTo(
                    0.8f + scrollProgress * 0.3f,
                    animationSpec = tween(200)
                )
            }
            ScrollEffectType.FADE -> {
                animatedAlpha.animateTo(
                    0.5f + scrollProgress * 0.5f,
                    animationSpec = tween(200)
                )
            }
            ScrollEffectType.WAVE -> {
                animatedOffset.animateTo(
                    kotlin.math.sin(scrollProgress * 10) * 20f,
                    animationSpec = tween(200)
                )
            }
            else -> {}
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(animatedScale.value)
            .alpha(animatedAlpha.value)
            .graphicsLayer {
                translationY = animatedOffset.value
            },
        shape = RoundedCornerShape(SugarDimens.Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = SugarDimens.Brand.hotPink.copy(alpha = 0.1f + scrollProgress * 0.2f)
        )
    ) {
        Text(
            text = item,
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Nested scroll connection for coordinated effects
 */
class ExtremeScrollConnection(
    private val onScroll: (Float, Velocity) -> Unit
) : NestedScrollConnection {
    
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        onScroll(available.y, Velocity.Zero)
        return Offset.Zero
    }
    
    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        onScroll(consumed.y, Velocity.Zero)
        return Offset.Zero
    }
}

/**
 * Get scroll effect modifier
 */
fun Modifier.scrollEffect(
    scrollProgress: Float,
    effectType: ScrollEffectType
): Modifier {
    return when (effectType) {
        ScrollEffectType.PARALLAX -> this.graphicsLayer {
            translationX = scrollProgress * 0.3f
        }
        ScrollEffectType.WAVE -> this.graphicsLayer {
            translationY = kotlin.math.sin(scrollProgress * 0.1f) * 30f
        }
        ScrollEffectType.SCALE -> this.scale(0.8f + scrollProgress * 0.3f)
        ScrollEffectType.FADE -> this.alpha(0.5f + scrollProgress * 0.5f)
        ScrollEffectType.ROTATE -> this.graphicsLayer {
            rotationZ = scrollProgress * 2f
        }
        ScrollEffectType.SKEW -> this.graphicsLayer {
            skewX = scrollProgress * 0.5f
        }
        ScrollEffectType.MAGNETIC -> this.graphicsLayer {
            translationX = kotlin.math.sin(scrollProgress * 0.05f) * 10f
        }
        ScrollEffectType.TRAIL -> this
    }
}
