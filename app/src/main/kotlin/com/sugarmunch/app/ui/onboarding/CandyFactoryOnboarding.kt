package com.sugarmunch.app.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.theme.model.ThemeCategory
import kotlinx.coroutines.launch

/**
 * Candy Factory Onboarding - 7-Screen Welcome Flow
 * Interactive onboarding experience for SugarMunch Extreme
 */

// ═════════════════════════════════════════════════════════════════
// MODELS
// ═════════════════════════════════════════════════════════════════

data class OnboardingPage(
    val title: String,
    val description: String,
    val illustration: OnboardingIllustration,
    val backgroundColor: List<Color>
)

enum class OnboardingIllustration {
    FACTORY_ENTRANCE,
    CANDY_SELECTION,
    INTENSITY_SLIDER,
    CARD_STYLE_SHOWCASE,
    FAB_DEMONSTRATION,
    SUGAR_RUSH_MODE,
    CELEBRATION_FINALE
}

// ═════════════════════════════════════════════════════════════════
// MAIN ONBOARDING COMPOSABLE
// ═════════════════════════════════════════════════════════════════

@Composable
fun CandyFactoryOnboarding(
    onComplete: () -> Unit,
    onRequestOverlayPermission: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 7 })
    val scope = rememberCoroutineScope()
    
    val pages = remember {
        listOf(
            OnboardingPage(
                title = "Welcome to the Candy Factory",
                description = "Your ultimate sugar-themed app store experience awaits!",
                illustration = OnboardingIllustration.FACTORY_ENTRANCE,
                backgroundColor = listOf(Color(0xFFFF69B4), Color(0xFFFF1493))
            ),
            OnboardingPage(
                title = "Choose Your Candy",
                description = "Pick from 80+ delicious themes - Chocolate, Gummy, Lollipop, and more!",
                illustration = OnboardingIllustration.CANDY_SELECTION,
                backgroundColor = listOf(Color(0xFF8D6E63), Color(0xFFFFD54F))
            ),
            OnboardingPage(
                title = "Set Your Sweetness",
                description = "Customize intensity levels for the perfect sugar rush experience",
                illustration = OnboardingIllustration.INTENSITY_SLIDER,
                backgroundColor = listOf(Color(0xFF00BFFF), Color(0xFF00FFA3))
            ),
            OnboardingPage(
                title = "Pick Your Style",
                description = "20+ card styles from Candy Wrapper to Sugar Glass",
                illustration = OnboardingIllustration.CARD_STYLE_SHOWCASE,
                backgroundColor = listOf(Color(0xFF9370DB), Color(0xFFFF69B4))
            ),
            OnboardingPage(
                title = "Enable the Magic",
                description = "Grant overlay permission for the Candy Dispenser FAB",
                illustration = OnboardingIllustration.FAB_DEMONSTRATION,
                backgroundColor = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
            ),
            OnboardingPage(
                title = "Sugar Rush Mode",
                description = "Experience maximum intensity with extreme visual effects",
                illustration = OnboardingIllustration.SUGAR_RUSH_MODE,
                backgroundColor = listOf(Color(0xFF39FF14), Color(0xFFFF3F34))
            ),
            OnboardingPage(
                title = "Ready to Explore!",
                description = "Let's dive into the sweetest app store experience!",
                illustration = OnboardingIllustration.CELEBRATION_FINALE,
                backgroundColor = listOf(Color(0xFFFF69B4), Color(0xFF00BFFF), Color(0xFFFFD700))
            )
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(pages[pagerState.currentPage].backgroundColor)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Skip button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onComplete) {
                    Text("Skip", color = Color.White)
                }
            }
            
            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(pages[page])
            }
            
            // Page indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(7) { index ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (pagerState.currentPage == index) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) Color.White
                                else Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
            
            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        if (pagerState.currentPage > 0) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    },
                    enabled = pagerState.currentPage > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = if (pagerState.currentPage > 0) 0.9f else 0.3f)
                    )
                ) {
                    Text("Back", color = Color(0xFFC71585))
                }
                
                Button(
                    onClick = {
                        if (pagerState.currentPage < 6) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            if (pagerState.currentPage == 4) {
                                onRequestOverlayPermission()
                            }
                            onComplete()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    )
                ) {
                    Text(
                        if (pagerState.currentPage < 6) "Next" else "Let's Go!",
                        color = Color(0xFFC71585)
                    )
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// PAGE CONTENT
// ═════════════════════════════════════════════════════════════════

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    val infiniteTransition = rememberInfiniteTransition(label = "onboarding")
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration
        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(y = (-bounce).dp)
        ) {
            when (page.illustration) {
                OnboardingIllustration.FACTORY_ENTRANCE -> FactoryEntranceIllustration()
                OnboardingIllustration.CANDY_SELECTION -> CandySelectionIllustration()
                OnboardingIllustration.INTENSITY_SLIDER -> IntensitySliderIllustration()
                OnboardingIllustration.CARD_STYLE_SHOWCASE -> CardStyleShowcaseIllustration()
                OnboardingIllustration.FAB_DEMONSTRATION -> FabDemonstrationIllustration()
                OnboardingIllustration.SUGAR_RUSH_MODE -> SugarRushModeIllustration()
                OnboardingIllustration.CELEBRATION_FINALE -> CelebrationFinaleIllustration()
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Title
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// ═════════════════════════════════════════════════════════════════
// ILLUSTRATIONS
// ═════════════════════════════════════════════════════════════════

@Composable
private fun FactoryEntranceIllustration() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        
        // Factory building
        drawRect(
            color = Color(0xFFFFB6C1),
            topLeft = Offset(center.x - 80f, center.y - 60f),
            size = Size(160f, 120f)
        )
        
        // Roof
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(center.x - 100f, center.y - 60f)
                lineTo(center.x, center.y - 120f)
                lineTo(center.x + 100f, center.y - 60f)
                close()
            },
            color = Color(0xFFFF69B4)
        )
        
        // Door
        drawRect(
            color = Color(0xFF8D6E63),
            topLeft = Offset(center.x - 30f, center.y),
            size = Size(60f, 60f)
        )
        
        // Windows
        repeat(3) { i ->
            drawRect(
                color = Color(0xFFFFD700),
                topLeft = Offset(center.x - 60f + i * 60f, center.y - 40f),
                size = Size(40f, 40f)
            )
        }
        
        // Candy cane chimneys
        drawCandyCane(Offset(center.x - 50f, center.y - 140f), 20f)
        drawCandyCane(Offset(center.x + 50f, center.y - 140f), 20f)
        
        // Steam
        drawSteam(center.x, center.y - 180f)
    }
}

@Composable
private fun CandySelectionIllustration() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        
        // Carousel base
        drawCircle(
            color = Color(0xFF8D6E63),
            radius = 100f,
            center = center
        )
        
        // Candy displays
        repeat(6) { i ->
            val angle = (i * 60f).toRadians()
            val x = center.x + kotlin.math.cos(angle) * 60f
            val y = center.y + kotlin.math.sin(angle) * 60f
            
            drawCandy(Offset(x, y), 25f, CandyColors[i % CandyColors.size])
        }
        
        // Center pedestal
        drawCircle(
            color = Color(0xFFFFD700),
            radius = 30f,
            center = center
        )
    }
}

@Composable
private fun IntensitySliderIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "slider")
    val sliderValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "slider"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        
        // Slider track
        drawRoundRect(
            color = Color.White.copy(alpha = 0.3f),
            topLeft = Offset(center.x - 100f, center.y - 10f),
            size = Size(200f, 20f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
        )
        
        // Slider fill
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF00FFA3), Color(0xFFFF69B4), Color(0xFFFFD700))
            ),
            topLeft = Offset(center.x - 100f, center.y - 10f),
            size = Size(200f * sliderValue, 20f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
        )
        
        // Slider thumb
        val thumbX = center.x - 100f + 200f * sliderValue
        drawCircle(
            color = Color.White,
            radius = 20f,
            center = Offset(thumbX, center.y)
        )
        
        // Intensity particles
        repeat((sliderValue * 10).toInt()) { i ->
            drawCircle(
                color = CandyColors[i % CandyColors.size].copy(alpha = 0.5f),
                radius = 5f,
                center = Offset(center.x - 80f + i * 20f, center.y - 40f)
            )
        }
    }
}

@Composable
private fun CardStyleShowcaseIllustration() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        
        // Card stack
        repeat(3) { i ->
            val offset = i * 15f
            drawRoundRect(
                color = CandyColors[i % CandyColors.size].copy(alpha = 0.7f),
                topLeft = Offset(center.x - 80f + offset, center.y - 50f),
                size = Size(120f, 80f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f)
            )
            
            // Card content lines
            drawRect(
                color = Color.White.copy(alpha = 0.5f),
                topLeft = Offset(center.x - 60f + offset, center.y - 30f),
                size = Size(80f, 8f)
            )
            drawRect(
                color = Color.White.copy(alpha = 0.3f),
                topLeft = Offset(center.x - 60f + offset, center.y - 10f),
                size = Size(60f, 6f)
            )
        }
    }
}

@Composable
private fun FabDemonstrationIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "fab")
    val fabY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2 + fabY)
        
        // FAB
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFF69B4), Color(0xFFC71585))
            ),
            radius = 50f,
            center = center
        )
        
        // FAB icon (candy)
        drawCircle(
            color = Color.White,
            radius = 20f,
            center = center
        )
        
        // Click ripple
        drawCircle(
            color = Color.White.copy(alpha = 0.3f),
            radius = 50f + fabY,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
        )
    }
}

@Composable
private fun SugarRushModeIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "rush")
    val rushAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rush"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        
        rotate(rushAngle) {
            // Rotating lightning bolts
            repeat(8) { i ->
                val angle = (i * 45f).toRadians()
                val x = center.x + kotlin.math.cos(angle) * 80f
                val y = center.y + kotlin.math.sin(angle) * 80f
                
                drawLightning(Offset(x, y), 30f, CandyColors[i % CandyColors.size])
            }
        }
        
        // Center burst
        drawCircle(
            color = Color(0xFFFFFF00),
            radius = 40f,
            center = center
        )
    }
}

@Composable
private fun CelebrationFinaleIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "celebration")
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        
        // Confetti
        repeat(30) { i ->
            val time = infiniteTransition.targetValue
            val confettiX = center.x + kotlin.math.sin(time * 0.001f + i) * 100f
            val confettiY = center.y + kotlin.math.cos(time * 0.0015f + i * 1.5f) * 80f
            
            drawRect(
                color = CandyColors[i % CandyColors.size],
                topLeft = Offset(confettiX - 5f, confettiY - 5f),
                size = Size(10f, 10f)
            )
        }
        
        // Star burst
        drawStar(center, 60f, Color(0xFFFFD700))
        
        // Celebration text background
        drawCircle(
            color = Color.White.copy(alpha = 0.2f),
            radius = 80f,
            center = center
        )
    }
}

// ═════════════════════════════════════════════════════════════════
// HELPER DRAWING FUNCTIONS
// ═════════════════════════════════════════════════════════════════

private fun DrawScope.drawCandyCane(center: Offset, size: Float) {
    drawArc(
        color = Color(0xFFFF0000),
        startAngle = -90f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(center.x - size, center.y - size),
        size = Size(size * 2, size * 2),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
    )
    drawArc(
        color = Color.White,
        startAngle = -90f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(center.x - size + 3f, center.y - size + 3f),
        size = Size(size * 2 - 6f, size * 2 - 6f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
    )
}

private fun DrawScope.drawSteam(center: Offset, size: Float) {
    repeat(3) { i ->
        drawCircle(
            color = Color.White.copy(alpha = 0.5f - i * 0.1f),
            radius = size * (0.3f + i * 0.1f),
            center = Offset(center.x - 20f + i * 20f, center.y - i * 30f)
        )
    }
}

private fun DrawScope.drawCandy(center: Offset, size: Float, color: Color) {
    drawCircle(color = color, radius = size, center = center)
    drawCircle(
        color = Color.White.copy(alpha = 0.4f),
        radius = size * 0.3f,
        center = Offset(center.x - size * 0.3f, center.y - size * 0.3f)
    )
}

private fun DrawScope.drawLightning(center: Offset, size: Float, color: Color) {
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(center.x, center.y - size)
        lineTo(center.x + size * 0.5f, center.y)
        lineTo(center.x, center.y)
        lineTo(center.x + size * 0.3f, center.y + size)
        lineTo(center.x - size * 0.3f, center.y)
        lineTo(center.x, center.y)
        close()
    }
    drawPath(path, color = color)
}

private fun DrawScope.drawStar(center: Offset, radius: Float, color: Color) {
    val path = androidx.compose.ui.graphics.Path().apply {
        repeat(5) { i ->
            val outerAngle = (i * 72f - 90f).toRadians()
            val innerAngle = ((i * 72f) + 36f - 90f).toRadians()
            
            val outerX = center.x + kotlin.math.cos(outerAngle) * radius
            val outerY = center.y + kotlin.math.sin(outerAngle) * radius
            val innerX = center.x + kotlin.math.cos(innerAngle) * radius * 0.5f
            val innerY = center.y + kotlin.math.sin(innerAngle) * radius * 0.5f
            
            if (i == 0) moveTo(outerX, outerY) else lineTo(outerX, outerY)
            lineTo(innerX, innerY)
        }
        close()
    }
    drawPath(path, color = color)
}

private fun Float.toRadians(): Float = this * Math.PI.toFloat() / 180f

private val CandyColors = listOf(
    Color(0xFFFF69B4), Color(0xFF00FFA3), Color(0xFFFFD700),
    Color(0xFF00BFFF), Color(0xFFFF6347), Color(0xFF9370DB)
)
