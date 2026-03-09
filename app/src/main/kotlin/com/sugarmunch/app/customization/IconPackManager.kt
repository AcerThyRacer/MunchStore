package com.sugarmunch.app.customization

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Icon pack data model
 */
data class IconPack(
    val id: String,
    val name: String,
    val author: String,
    val description: String,
    val iconCount: Int,
    val previewIcons: List<String>,
    val iconPaths: Map<String, String>, // packageName -> iconPath
    val maskShape: IconMaskShape = IconMaskShape.CIRCLE,
    val backgroundColor: String? = null
)

enum class IconMaskShape {
    CIRCLE,
    SQUIRCLE,
    ROUNDED_SQUARE,
    SQUARE,
    HEXAGON,
    OCTAGON,
    PEBBLE,
    CUSTOM
}

/**
 * IconPackManager - Manage icon packs and custom icons.
 * Features:
 * - Import icon packs (adaptive icons)
 * - Per-app icon selection
 * - Icon mask shapes
 * - Custom icon upload
 * - Icon shadow/glow effects
 */
class IconPackManager(private val context: Context) {

    private val _iconPacks = MutableStateFlow<List<IconPack>>(emptyList())
    val iconPacks: StateFlow<List<IconPack>> = _iconPacks.asStateFlow()

    private val _customIcons = MutableStateFlow<Map<String, String>>(emptyMap()) // packageName -> iconPath
    val customIcons: StateFlow<Map<String, String>> = _customIcons.asStateFlow()

    private val _activeIconPack = MutableStateFlow<IconPack?>(null)
    val activeIconPack: StateFlow<IconPack?> = _activeIconPack.asStateFlow()

    private val iconCache = mutableMapOf<String, Bitmap>()

    companion object {
        private const val TAG = "IconPackManager"
        
        @Volatile
        private var INSTANCE: IconPackManager? = null

        fun getInstance(context: Context): IconPackManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: IconPackManager(context).also { INSTANCE = it }
            }
        }
    }

    /**
     * Load all available icon packs
     */
    suspend fun loadIconPacks() = withContext(Dispatchers.IO) {
        val packs = mutableListOf<IconPack>()

        // Check for installed icon pack apps
        val pm = context.packageManager
        val intent = android.content.Intent("com.sugarmunch.ICON_PACK")
        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA)

        resolveInfos.forEach { resolveInfo ->
            val packInfo = parseIconPack(resolveInfo.activityInfo.applicationInfo)
            if (packInfo != null) {
                packs.add(packInfo)
            }
        }

        // Add built-in icon packs
        packs.addAll(getBuiltInIconPacks())

        _iconPacks.value = packs
    }

    /**
     * Apply an icon pack
     */
    suspend fun applyIconPack(packId: String) = withContext(Dispatchers.IO) {
        val pack = _iconPacks.value.find { it.id == packId } ?: return@withContext
        _activeIconPack.value = pack

        // Clear icon cache to force reload
        iconCache.clear()
    }

    /**
     * Set custom icon for a specific app
     */
    suspend fun setCustomIcon(packageName: String, iconPath: String) = withContext(Dispatchers.IO) {
        val updated = _customIcons.value.toMutableMap()
        updated[packageName] = iconPath
        _customIcons.value = updated

        // Clear cached icon
        iconCache.remove(packageName)
    }

    /**
     * Remove custom icon for an app
     */
    suspend fun removeCustomIcon(packageName: String) = withContext(Dispatchers.IO) {
        val updated = _customIcons.value.toMutableMap()
        updated.remove(packageName)
        _customIcons.value = updated

        iconCache.remove(packageName)
    }

    /**
     * Get icon for an app (custom > icon pack > default)
     */
    suspend fun getIconForApp(packageName: String, defaultIcon: Drawable): Bitmap? {
        // Check cache first
        iconCache[packageName]?.let { return it }

        // Check custom icon
        _customIcons.value[packageName]?.let { path ->
            val bitmap = loadIconFromPath(path)
            if (bitmap != null) {
                iconCache[packageName] = bitmap
                return bitmap
            }
        }

        // Check icon pack
        _activeIconPack.value?.let { pack ->
            pack.iconPaths[packageName]?.let { iconPath ->
                val bitmap = loadPackIcon(pack, iconPath)
                if (bitmap != null) {
                    val processed = applyMaskAndEffects(bitmap, pack)
                    iconCache[packageName] = processed
                    return processed
                }
            }
        }

        // Return processed default icon
        val processed = processDefaultIcon(defaultIcon)
        iconCache[packageName] = processed
        return processed
    }

    /**
     * Import custom icon from file
     */
    suspend fun importCustomIcon(packageName: String, sourcePath: String): Boolean {
        return try {
            val destFile = File(context.filesDir, "icons/${packageName}.png")
            destFile.parentFile?.mkdirs()

            withContext(Dispatchers.IO) {
                File(sourcePath).copyTo(destFile, overwrite = true)
            }

            setCustomIcon(packageName, destFile.absolutePath)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import custom icon for $packageName", e)
            false
        }
    }

    /**
     * Export current icon configuration
     */
    fun exportIconConfig(): IconConfig {
        return IconConfig(
            activePackId = _activeIconPack.value?.id,
            customIcons = _customIcons.value
        )
    }

    /**
     * Import icon configuration
     */
    suspend fun importIconConfig(config: IconConfig) {
        if (config.activePackId != null) {
            applyIconPack(config.activePackId)
        }

        _customIcons.value = config.customIcons
        iconCache.clear()
    }

    // Private helper methods

    private fun parseIconPack(appInfo: android.content.pm.ApplicationInfo): IconPack? {
        return try {
            IconPack(
                id = appInfo.packageName,
                name = appInfo.loadLabel(context.packageManager).toString(),
                author = "Unknown",
                description = "",
                iconCount = 0,
                previewIcons = emptyList(),
                iconPaths = emptyMap()
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun getBuiltInIconPacks(): List<IconPack> {
        return listOf(
            IconPack(
                id = "sugarmunch_default",
                name = "SugarMunch Default",
                author = "SugarMunch",
                description = "Default SugarMunch icons with colorful gradients",
                iconCount = 0,
                previewIcons = emptyList(),
                iconPaths = emptyMap(),
                maskShape = IconMaskShape.SQUIRCLE
            ),
            IconPack(
                id = "sugarmunch_minimal",
                name = "Minimal White",
                author = "SugarMunch",
                description = "Clean white icons with subtle shadows",
                iconCount = 0,
                previewIcons = emptyList(),
                iconPaths = emptyMap(),
                maskShape = IconMaskShape.CIRCLE
            ),
            IconPack(
                id = "sugarmunch_neon",
                name = "Neon Glow",
                author = "SugarMunch",
                description = "Vibrant neon-styled icons with glow effects",
                iconCount = 0,
                previewIcons = emptyList(),
                iconPaths = emptyMap(),
                maskShape = IconMaskShape.ROUNDED_SQUARE
            )
        )
    }

    private fun loadIconFromPath(path: String): Bitmap? {
        return try {
            android.graphics.BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            null
        }
    }

    private fun loadPackIcon(pack: IconPack, iconPath: String): Bitmap? {
        // Would load from pack's resources
        return null
    }

    private fun applyMaskAndEffects(bitmap: Bitmap, pack: IconPack): Bitmap {
        val size = 108 // Standard adaptive icon size
        val result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Apply mask
        val mask = createMask(pack.maskShape, size)
        canvas.clipPath(mask)

        // Draw bitmap
        val scaled = Bitmap.createScaledBitmap(bitmap, size, size, true)
        canvas.drawBitmap(scaled, 0f, 0f, null)

        return result
    }

    private fun createMask(shape: IconMaskShape, size: Int): android.graphics.Path {
        val path = android.graphics.Path()
        val center = size / 2f
        val radius = size / 2f - 4f

        when (shape) {
            IconMaskShape.CIRCLE -> {
                path.addCircle(center, center, radius, android.graphics.Path.Direction.CW)
            }
            IconMaskShape.SQUIRCLE -> {
                // Squircle path calculation
                path.addRoundRect(4f, 4f, size - 4f, size - 4f, radius * 0.4f, radius * 0.4f, android.graphics.Path.Direction.CW)
            }
            IconMaskShape.ROUNDED_SQUARE -> {
                path.addRoundRect(4f, 4f, size - 4f, size - 4f, 16f, 16f, android.graphics.Path.Direction.CW)
            }
            IconMaskShape.SQUARE -> {
                path.addRect(4f, 4f, size - 4f, size - 4f, android.graphics.Path.Direction.CW)
            }
            IconMaskShape.HEXAGON -> {
                // Hexagon path
                val points = (0 until 6).map { i ->
                    val angle = i * 60 * Math.PI / 180 - Math.PI / 2
                    Pair(
                        (center + radius * kotlin.math.cos(angle)).toFloat(),
                        (center + radius * kotlin.math.sin(angle)).toFloat()
                    )
                }
                path.moveTo(points[0].first, points[0].second)
                points.drop(1).forEach { path.lineTo(it.first, it.second) }
                path.close()
            }
            else -> {
                path.addCircle(center, center, radius, android.graphics.Path.Direction.CW)
            }
        }

        return path
    }

    private fun processDefaultIcon(icon: Drawable): Bitmap {
        val size = 108
        val result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        if (icon is AdaptiveIconDrawable) {
            // Handle adaptive icon
            val foreground = icon.foreground
            foreground.setBounds(0, 0, size, size)
            foreground.draw(canvas)
        } else {
            // Regular drawable
            val bitmap = (icon as? BitmapDrawable)?.bitmap
                ?: Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).also {
                    val c = Canvas(it)
                    icon.setBounds(0, 0, size, size)
                    icon.draw(c)
                }

            val scaled = Bitmap.createScaledBitmap(bitmap, size, size, true)
            canvas.drawBitmap(scaled, 0f, 0f, null)
        }

        return result
    }

    fun clearCache() {
        iconCache.clear()
    }
}

/**
 * Icon configuration for export/import
 */
data class IconConfig(
    val activePackId: String?,
    val customIcons: Map<String, String>
)

/**
 * Icon effects configuration
 */
data class IconEffects(
    val shadowEnabled: Boolean = true,
    val shadowRadius: Float = 4f,
    val shadowColor: Int = android.graphics.Color.BLACK,
    val glowEnabled: Boolean = false,
    val glowRadius: Float = 8f,
    val glowColor: Int = android.graphics.Color.WHITE,
    val saturation: Float = 1f,
    val brightness: Float = 1f
)
