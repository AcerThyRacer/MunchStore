package com.sugarmunch.app.ui.studio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

// ── HSL ↔ Color conversion utilities ──────────────────────────────────────────

private fun hslToColor(hue: Float, saturation: Float, lightness: Float): Color {
    val c = (1f - kotlin.math.abs(2f * lightness - 1f)) * saturation
    val x = c * (1f - kotlin.math.abs((hue / 60f) % 2f - 1f))
    val m = lightness - c / 2f
    val (r, g, b) = when {
        hue < 60f  -> Triple(c, x, 0f)
        hue < 120f -> Triple(x, c, 0f)
        hue < 180f -> Triple(0f, c, x)
        hue < 240f -> Triple(0f, x, c)
        hue < 300f -> Triple(x, 0f, c)
        else       -> Triple(c, 0f, x)
    }
    return Color(
        red = (r + m).coerceIn(0f, 1f),
        green = (g + m).coerceIn(0f, 1f),
        blue = (b + m).coerceIn(0f, 1f)
    )
}

private fun colorToHsl(color: Color): Triple<Float, Float, Float> {
    val r = color.red
    val g = color.green
    val b = color.blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val l = (max + min) / 2f
    if (max == min) return Triple(0f, 0f, l)
    val d = max - min
    val s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)
    val h = when (max) {
        r -> ((g - b) / d + (if (g < b) 6f else 0f)) * 60f
        g -> ((b - r) / d + 2f) * 60f
        else -> ((r - g) / d + 4f) * 60f
    }
    return Triple(h.coerceIn(0f, 360f), s.coerceIn(0f, 1f), l.coerceIn(0f, 1f))
}

// ── Color wheel composable ────────────────────────────────────────────────────

@Composable
fun ColorWheelPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val (initH, initS, initL) = remember(selectedColor) { colorToHsl(selectedColor) }
    var hue by remember(selectedColor) { mutableFloatStateOf(initH) }
    var saturation by remember(selectedColor) { mutableFloatStateOf(initS) }
    var lightness by remember(selectedColor) { mutableFloatStateOf(initL) }
    var hexInput by remember(selectedColor) {
        mutableStateOf(selectedColor.toHexString())
    }
    val focusManager = LocalFocusManager.current

    fun emitColor() {
        val c = hslToColor(hue, saturation, lightness)
        hexInput = c.toHexString()
        onColorSelected(c)
    }

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        // Hue wheel + saturation / lightness square
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(240.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            handleWheelTouch(
                                offset, size.width.toFloat(), size.height.toFloat(),
                                onHueChange = { hue = it; emitColor() },
                                onSatLightChange = { s, l -> saturation = s; lightness = l; emitColor() }
                            )
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            handleWheelTouch(
                                change.position, size.width.toFloat(), size.height.toFloat(),
                                onHueChange = { hue = it; emitColor() },
                                onSatLightChange = { s, l -> saturation = s; lightness = l; emitColor() }
                            )
                        }
                    }
            ) {
                drawHueWheel()
                drawSatLightSquare(hue)
                drawIndicators(hue, saturation, lightness)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Lightness slider
        Text("Lightness", style = MaterialTheme.typography.labelMedium)
        Slider(
            value = lightness,
            onValueChange = { lightness = it; emitColor() },
            valueRange = 0f..1f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = SliderDefaults.colors(
                thumbColor = hslToColor(hue, saturation, lightness),
                activeTrackColor = hslToColor(hue, saturation, 0.5f)
            )
        )

        Spacer(Modifier.height(12.dp))

        // Preview swatch + hex input
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(hslToColor(hue, saturation, lightness))
                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            OutlinedTextField(
                value = hexInput,
                onValueChange = { value ->
                    hexInput = value
                    val parsed = value.toComposeColorOrNull()
                    if (parsed != null) {
                        val (h, s, l) = colorToHsl(parsed)
                        hue = h; saturation = s; lightness = l
                        onColorSelected(parsed)
                    }
                },
                label = { Text("Hex") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )
        }

        Spacer(Modifier.height(12.dp))

        // RGB sliders for fine-tuning
        val currentColor = hslToColor(hue, saturation, lightness)
        RgbSlider("R", currentColor.red) { r ->
            val c = currentColor.copy(red = r)
            val (h, s, l) = colorToHsl(c)
            hue = h; saturation = s; lightness = l; emitColor()
        }
        RgbSlider("G", currentColor.green) { g ->
            val c = currentColor.copy(green = g)
            val (h, s, l) = colorToHsl(c)
            hue = h; saturation = s; lightness = l; emitColor()
        }
        RgbSlider("B", currentColor.blue) { b ->
            val c = currentColor.copy(blue = b)
            val (h, s, l) = colorToHsl(c)
            hue = h; saturation = s; lightness = l; emitColor()
        }
    }
}

@Composable
private fun RgbSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(20.dp),
            textAlign = TextAlign.Center
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = (value * 255).toInt().toString(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End
        )
    }
}

// ── Canvas drawing helpers ────────────────────────────────────────────────────

private fun handleWheelTouch(
    offset: Offset,
    width: Float,
    height: Float,
    onHueChange: (Float) -> Unit,
    onSatLightChange: (Float, Float) -> Unit
) {
    val cx = width / 2f
    val cy = height / 2f
    val outerRadius = min(cx, cy)
    val ringWidth = outerRadius * 0.22f
    val innerRadius = outerRadius - ringWidth

    val dx = offset.x - cx
    val dy = offset.y - cy
    val dist = hypot(dx, dy)

    if (dist >= innerRadius && dist <= outerRadius) {
        // On the hue ring
        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        if (angle < 0) angle += 360f
        onHueChange(angle)
    } else if (dist < innerRadius) {
        // Inside – saturation/lightness square region
        val squareHalf = innerRadius * 0.7f
        val s = ((dx + squareHalf) / (2f * squareHalf)).coerceIn(0f, 1f)
        val l = 1f - ((dy + squareHalf) / (2f * squareHalf)).coerceIn(0f, 1f)
        onSatLightChange(s, l)
    }
}

private fun DrawScope.drawHueWheel() {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val outerRadius = min(cx, cy)
    val ringWidth = outerRadius * 0.22f
    val midRadius = outerRadius - ringWidth / 2f
    val steps = 360

    for (i in 0 until steps) {
        val angle = i.toFloat()
        val radians = Math.toRadians(angle.toDouble())
        val color = hslToColor(angle, 1f, 0.5f)
        val startX = cx + (midRadius - ringWidth / 2f) * cos(radians).toFloat()
        val startY = cy + (midRadius - ringWidth / 2f) * sin(radians).toFloat()
        val endX = cx + (midRadius + ringWidth / 2f) * cos(radians).toFloat()
        val endY = cy + (midRadius + ringWidth / 2f) * sin(radians).toFloat()
        drawLine(color, Offset(startX, startY), Offset(endX, endY), strokeWidth = 3f)
    }
}

private fun DrawScope.drawSatLightSquare(hue: Float) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val outerRadius = min(cx, cy)
    val ringWidth = outerRadius * 0.22f
    val innerRadius = outerRadius - ringWidth
    val squareHalf = innerRadius * 0.7f
    val step = 4

    var x = (cx - squareHalf).toInt()
    while (x < (cx + squareHalf).toInt()) {
        var y = (cy - squareHalf).toInt()
        while (y < (cy + squareHalf).toInt()) {
            val s = ((x - (cx - squareHalf)) / (2f * squareHalf)).coerceIn(0f, 1f)
            val l = 1f - ((y - (cy - squareHalf)) / (2f * squareHalf)).coerceIn(0f, 1f)
            drawRect(
                color = hslToColor(hue, s, l),
                topLeft = Offset(x.toFloat(), y.toFloat()),
                size = androidx.compose.ui.geometry.Size(step.toFloat(), step.toFloat())
            )
            y += step
        }
        x += step
    }
}

private fun DrawScope.drawIndicators(hue: Float, saturation: Float, lightness: Float) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val outerRadius = min(cx, cy)
    val ringWidth = outerRadius * 0.22f
    val midRadius = outerRadius - ringWidth / 2f

    // Hue indicator on the ring
    val hueRad = Math.toRadians(hue.toDouble())
    val hx = cx + midRadius * cos(hueRad).toFloat()
    val hy = cy + midRadius * sin(hueRad).toFloat()
    drawCircle(Color.White, radius = ringWidth / 2.2f, center = Offset(hx, hy))
    drawCircle(
        Color.Black, radius = ringWidth / 2.2f, center = Offset(hx, hy),
        style = Stroke(width = 2f)
    )

    // Crosshair in the saturation/lightness square
    val innerRadius = outerRadius - ringWidth
    val squareHalf = innerRadius * 0.7f
    val sx = cx - squareHalf + saturation * 2f * squareHalf
    val sy = cy - squareHalf + (1f - lightness) * 2f * squareHalf
    drawCircle(Color.White, radius = 8f, center = Offset(sx, sy), style = Stroke(width = 3f))
    drawCircle(Color.Black, radius = 8f, center = Offset(sx, sy), style = Stroke(width = 1.5f))
}

// ── Color palette row ─────────────────────────────────────────────────────────

@Composable
fun ColorPaletteRow(
    colors: List<Color>,
    selectedIndex: Int,
    onColorTapped: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        colors.forEachIndexed { index, color ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .size(if (isSelected) 48.dp else 40.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (isSelected) {
                            Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        } else {
                            Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        }
                    )
                    .clickable { onColorTapped(index) }
            )
        }
    }
}

// ── Harmony palette generation ────────────────────────────────────────────────

fun generateHarmonyPalette(seedColor: Color): List<Color> {
    val (h, s, l) = colorToHsl(seedColor)

    val primary = seedColor
    val secondary = hslToColor((h + 180f) % 360f, s, l)                       // complementary
    val tertiary = hslToColor((h + 30f) % 360f, s, l)                         // analogous
    val surface = hslToColor(h, (s * 0.15f).coerceIn(0f, 1f), 0.95f)          // desaturated light
    val background = hslToColor(h, (s * 0.08f).coerceIn(0f, 1f), 0.98f)       // very light
    val accent = hslToColor((h + 120f) % 360f, s.coerceAtLeast(0.5f), 0.5f)   // triadic

    return listOf(primary, secondary, tertiary, surface, background, accent)
}

// ── Hex string helpers (private, used by the picker) ──────────────────────────

private fun String.toComposeColorOrNull(): Color? = try {
    toComposeColor()
} catch (_: Exception) {
    null
}
