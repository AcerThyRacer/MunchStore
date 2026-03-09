package com.sugarmunch.app.ai

import androidx.compose.ui.graphics.Color
import com.sugarmunch.app.theme.model.CandyTheme
import com.sugarmunch.app.theme.model.IntensityLevels
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class GenerativeThemeEngine @Inject constructor() {

    /**
     * Simulates an AI Generative Engine that takes a prompt and builds a unique theme.
     */
    fun generateThemeFromPrompt(prompt: String): CandyTheme {
        val lowerPrompt = prompt.lowercase()
        
        // Simple heuristic generation based on keywords
        val baseColor = when {
            lowerPrompt.contains("cyberpunk") || lowerPrompt.contains("neon") -> Color(0xFF00FFCC)
            lowerPrompt.contains("mars") || lowerPrompt.contains("desert") -> Color(0xFFFF5722)
            lowerPrompt.contains("forest") || lowerPrompt.contains("nature") -> Color(0xFF4CAF50)
            lowerPrompt.contains("ocean") || lowerPrompt.contains("water") -> Color(0xFF03A9F4)
            lowerPrompt.contains("dark") || lowerPrompt.contains("night") -> Color(0xFF121212)
            lowerPrompt.contains("candy") || lowerPrompt.contains("sweet") -> Color(0xFFFF4081)
            else -> Color(Random.nextLong(0xFF000000, 0xFFFFFFFF))
        }

        val secondaryColor = when {
            lowerPrompt.contains("thunderstorm") || lowerPrompt.contains("storm") -> Color(0xFF9C27B0)
            lowerPrompt.contains("sun") || lowerPrompt.contains("bright") -> Color(0xFFFFEB3B)
            lowerPrompt.contains("fire") -> Color(0xFFFF9800)
            else -> Color(Random.nextLong(0xFF000000, 0xFFFFFFFF))
        }

        val name = "Gen: ${prompt.take(15)}..."

        return CandyTheme(
            id = "gen_${System.currentTimeMillis()}",
            name = name,
            primaryHex = String.format("#%06X", (0xFFFFFF and baseColor.value.toInt())),
            secondaryHex = String.format("#%06X", (0xFFFFFF and secondaryColor.value.toInt())),
            accentHex = String.format("#%06X", (0xFFFFFF and Color.White.value.toInt())),
            isDark = lowerPrompt.contains("dark") || lowerPrompt.contains("night"),
            intensityLevels = IntensityLevels(
                theme = 1.0f,
                background = if (lowerPrompt.contains("glassmorphism")) 1.5f else 1.0f,
                particles = if (lowerPrompt.contains("storm") || lowerPrompt.contains("particles")) 2.0f else 1.0f,
                animations = 1.0f
            ),
            backgroundStyle = "mesh", // Use advanced mesh by default for gen themes
            particleStyle = if (lowerPrompt.contains("candy")) "confetti" else "sparkles",
            animationStyle = "smooth"
        )
    }
}
