package com.sugarmunch.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.google.gson.Gson
import com.sugarmunch.app.ai.SmartCacheManager
import com.sugarmunch.app.ai.UsagePredictor
import com.sugarmunch.app.auth.AuthManager
import com.sugarmunch.app.backup.proton.ProtonDriveManager
import com.sugarmunch.app.data.ManifestRepository
import com.sugarmunch.app.data.PreferencesRepository
import com.sugarmunch.app.data.local.AppDao
import com.sugarmunch.app.data.local.AppDatabase
import com.sugarmunch.app.data.local.FolderDao
import com.sugarmunch.app.data.repository.FolderRepository
import com.sugarmunch.app.performance.EffectOptimizer
import com.sugarmunch.app.performance.PerformanceMonitor
import com.sugarmunch.app.download.SmartDownloadManager
import com.sugarmunch.app.effects.v2.engine.EffectEngineV2
import com.sugarmunch.app.plugin.security.PluginSecurity
import com.sugarmunch.app.plugin.store.PluginStore
import com.sugarmunch.app.rewards.DailyRewardsManager
import com.sugarmunch.app.ai.neural.NeuralThemeEngine
import com.sugarmunch.app.ai.neural.MoodDetector
import com.sugarmunch.app.ai.neural.WallpaperAnalyzer
import com.sugarmunch.app.ai.neural.ColorPsychology
import com.sugarmunch.app.ai.neural.WeatherReactiveTheme
import com.sugarmunch.app.physics.quantum.PhysicsEngine
import com.sugarmunch.app.physics.quantum.SpringSystem
import com.sugarmunch.app.physics.quantum.FluidSimulation
import com.sugarmunch.app.physics.quantum.ParticleRenderer
import com.sugarmunch.app.physics.quantum.MotionTrails
import com.sugarmunch.app.physics.quantum.QuantumAnimationEngine
import com.sugarmunch.app.holographic.DepthEngine
import com.sugarmunch.app.holographic.ParallaxController
import com.sugarmunch.app.holographic.HolographicShader
import com.sugarmunch.app.holographic.LightSystem
import com.sugarmunch.app.holographic.ReflectionMapper
import com.sugarmunch.app.widgets.infinite.InfiniteCanvas
import com.sugarmunch.app.widgets.infinite.WidgetStackManager
import com.sugarmunch.app.widgets.infinite.WidgetBuilder
import com.sugarmunch.app.ai.sentient.SentientAssistant
import com.sugarmunch.app.ai.sentient.AppPredictor
import com.sugarmunch.app.ai.sentient.VoiceCustomizer
import com.sugarmunch.app.ai.sentient.RoutineDetector
import com.sugarmunch.app.ai.sentient.AttentionTracker
import com.sugarmunch.app.gestures.dimensional.DimensionalGestureEngine
import com.sugarmunch.app.gestures.dimensional.AirGestureRecognizer
import com.sugarmunch.app.gestures.dimensional.GestureLibrary
import com.sugarmunch.app.gestures.dimensional.MultiTouchProcessor
import com.sugarmunch.app.gestures.dimensional.PressureSensor
import com.sugarmunch.app.gestures.dimensional.GestureMacroRecorder
import com.sugarmunch.app.ar.reality.RealityOverlayEngine
import com.sugarmunch.app.ar.reality.ARIconSystem
import com.sugarmunch.app.ar.reality.SpatialFolderManager
import com.sugarmunch.app.ar.reality.WorldAnchorSystem
import com.sugarmunch.app.ar.reality.ContextLayerRenderer
import com.sugarmunch.app.ar.reality.ARSearchEngine
import com.sugarmunch.app.security.ApkSignatureVerifier
import com.sugarmunch.app.BuildConfig
import com.sugarmunch.app.trading.MarketManager
import com.sugarmunch.app.trading.TradeManager
import com.sugarmunch.app.util.BatteryOptimizationManager
import com.sugarmunch.app.util.AppCoroutineScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by androidx.datastore.preferences.preferencesDataStore(name = "sugarmunch_preferences")
private val Context.authDataStore: DataStore<Preferences> by androidx.datastore.preferences.preferencesDataStore(name = "auth_preferences")

/**
 * Hilt Dependency Injection Module - App Level
 * Provides singleton dependencies for the entire application
 *
 * This module replaces manual singleton patterns with Hilt-managed singletons,
 * providing better testability and lifecycle management.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provide Gson instance for JSON serialization
     */
    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    /**
     * Provide OkHttpClient with logging interceptor
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Provide Retrofit instance
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provide AppDatabase (Room)
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    /**
     * Provide AppDao
     */
    @Provides
    @Singleton
    fun provideAppDao(database: AppDatabase) = database.appDao()

    /**
     * Provide FolderDao
     */
    @Provides
    @Singleton
    fun provideFolderDao(database: AppDatabase) = database.folderDao()

    /**
     * Provide CachedAppDao
     */
    @Provides
    @Singleton
    fun provideCachedAppDao(database: AppDatabase) = database.cachedAppDao()

    /**
     * Provide PredictionDao
     */
    @Provides
    @Singleton
    fun providePredictionDao(database: AppDatabase) = database.predictionDao()

    /**
     * Provide AppUsageDao
     */
    @Provides
    @Singleton
    fun provideAppUsageDao(database: AppDatabase) = database.appUsageDao()

    // ManifestRepository is provided via @Inject constructor - no manual provider needed

    /**
     * Provide SmartDownloadManager - Migrated to Hilt @Singleton
     * Note: Constructor is now public, getInstance() deprecated
     */
    @Provides
    @Singleton
    fun provideSmartDownloadManager(@ApplicationContext context: Context): SmartDownloadManager {
        return SmartDownloadManager(context)
    }

    /**
     * Provide DailyRewardsManager - Migrated to Hilt @Singleton
     * Note: Constructor is now public, getInstance() deprecated
     */
    @Provides
    @Singleton
    fun provideDailyRewardsManager(@ApplicationContext context: Context): DailyRewardsManager {
        return DailyRewardsManager(context)
    }

    /**
     * Provide ApkSignatureVerifier
     */
    @Provides
    @Singleton
    fun provideApkSignatureVerifier(@ApplicationContext context: Context): ApkSignatureVerifier {
        return ApkSignatureVerifier(context)
    }

    /**
     * Provide PluginSecurity. Prefer injecting this instead of PluginSecurity.getInstance(context).
     */
    @Provides
    @Singleton
    fun providePluginSecurity(@ApplicationContext context: Context): PluginSecurity {
        return PluginSecurity(context)
    }

    /**
     * Provide BatteryOptimizationManager - Migrated to Hilt @Singleton
     */
    @Provides
    @Singleton
    fun provideBatteryOptimizationManager(@ApplicationContext context: Context): BatteryOptimizationManager {
        return BatteryOptimizationManager(context)
    }

    /**
     * Provide EffectEngineV2
     */
    @Provides
    @Singleton
    fun provideEffectEngineV2(
        @ApplicationContext context: Context,
        gson: Gson
    ): EffectEngineV2 {
        return EffectEngineV2(context, gson)
    }

    /**
     * Provide UsagePredictor - Migrated to Hilt @Singleton
     */
    @Provides
    @Singleton
    fun provideUsagePredictor(@ApplicationContext context: Context): UsagePredictor {
        return UsagePredictor(context)
    }

    /**
     * Provide SmartCacheManager - Migrated to Hilt @Singleton
     */
    @Provides
    @Singleton
    fun provideSmartCacheManager(@ApplicationContext context: Context): SmartCacheManager {
        return SmartCacheManager(context)
    }

    /**
     * Provide AppCoroutineScope
     */
    @Provides
    @Singleton
    fun provideAppCoroutineScope(): AppCoroutineScope {
        return AppCoroutineScope()
    }

    /**
     * Provide DataStore (sugarmunch_preferences – general app preferences)
     */
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    /**
     * Provide PreferencesRepository (UI/catalog/onboarding preferences – uses sugarmunch_prefs internally).
     * Prefer injecting this instead of constructing PreferencesRepository(context) manually.
     */
    @Provides
    @Singleton
    fun providePreferencesRepository(dataStore: DataStore<Preferences>): PreferencesRepository {
        return PreferencesRepository(dataStore)
    }

    /**
     * Provide Auth DataStore
     */
    @Provides
    @Singleton
    fun provideAuthDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.authDataStore
    }

    /**
     * Provide AuthManager
     */
    @Provides
    @Singleton
    fun provideAuthManager(
        @ApplicationContext context: Context,
        authDataStore: DataStore<Preferences>
    ): AuthManager {
        return AuthManager(context, authDataStore)
    }

    /**
     * Provide TradeManager - Migrated to Hilt @Singleton
     * Note: Constructor is now public, getInstance() deprecated
     */
    @Provides
    @Singleton
    fun provideTradeManager(
        @ApplicationContext context: Context,
        authManager: AuthManager
    ): TradeManager {
        return TradeManager(context, authManager)
    }

    /**
     * Provide MarketManager - Migrated to Hilt @Singleton
     * Note: Constructor is now public, getInstance() deprecated
     */
    @Provides
    @Singleton
    fun provideMarketManager(
        @ApplicationContext context: Context,
        authManager: AuthManager
    ): MarketManager {
        return MarketManager(context, authManager)
    }

    /**
     * Provide ProtonDriveManager
     */
    @Provides
    @Singleton
    fun provideProtonDriveManager(
        @ApplicationContext context: Context,
        authManager: AuthManager,
        authDataStore: DataStore<Preferences>
    ): ProtonDriveManager {
        return ProtonDriveManager(context, authManager, authDataStore)
    }

    /**
     * Provide FolderRepository
     */
    @Provides
    @Singleton
    fun provideFolderRepository(
        folderDao: FolderDao,
        appDao: AppDao,
        @ApplicationContext context: Context
    ): FolderRepository {
        return FolderRepository(folderDao, appDao, context)
    }

    /**
     * Provide PluginStore - Singleton instance for plugin management
     */
    @Provides
    @Singleton
    fun providePluginStore(@ApplicationContext context: Context): PluginStore {
        return PluginStore.getInstance(context)
    }

    /**
     * Provide PerformanceMonitor - Migrated to Hilt @Singleton
     */
    @Provides
    @Singleton
    fun providePerformanceMonitor(): PerformanceMonitor {
        return PerformanceMonitor()
    }

    /**
     * Provide EffectOptimizer - Migrated to Hilt @Singleton
     */
    @Provides
    @Singleton
    fun provideEffectOptimizer(@ApplicationContext context: Context): EffectOptimizer {
        return EffectOptimizer(context)
    }

    // ========== NEURAL THEME ENGINE PROVIDERS ==========

    /**
     * Provide NeuralThemeEngine - AI-powered theme generation
     */
    @Provides
    @Singleton
    fun provideNeuralThemeEngine(@ApplicationContext context: Context): NeuralThemeEngine {
        return NeuralThemeEngine.getInstance(context)
    }

    /**
     * Provide MoodDetector - User mood detection
     */
    @Provides
    @Singleton
    fun provideMoodDetector(@ApplicationContext context: Context): MoodDetector {
        return MoodDetector.getInstance(context)
    }

    /**
     * Provide WallpaperAnalyzer - Wallpaper color analysis
     */
    @Provides
    @Singleton
    fun provideWallpaperAnalyzer(): WallpaperAnalyzer {
        return WallpaperAnalyzer()
    }

    /**
     * Provide ColorPsychology - Color psychology engine
     */
    @Provides
    @Singleton
    fun provideColorPsychology(): ColorPsychology {
        return ColorPsychology()
    }

    /**
     * Provide WeatherReactiveTheme - Weather-based theming
     */
    @Provides
    @Singleton
    fun provideWeatherReactiveTheme(@ApplicationContext context: Context): WeatherReactiveTheme {
        return WeatherReactiveTheme(context)
    }

    // ========== QUANTUM ANIMATION ENGINE PROVIDERS ==========

    /**
     * Provide PhysicsEngine - Physics simulation engine
     */
    @Provides
    @Singleton
    fun providePhysicsEngine(): PhysicsEngine {
        return PhysicsEngine.getInstance()
    }

    /**
     * Provide SpringSystem - Spring physics system
     */
    @Provides
    @Singleton
    fun provideSpringSystem(): SpringSystem {
        return SpringSystem.getInstance()
    }

    /**
     * Provide FluidSimulation - Fluid dynamics simulation
     */
    @Provides
    @Singleton
    fun provideFluidSimulation(): FluidSimulation {
        return FluidSimulation()
    }

    /**
     * Provide ParticleRenderer - Particle system renderer
     */
    @Provides
    @Singleton
    fun provideParticleRenderer(): ParticleRenderer {
        return ParticleRenderer()
    }

    /**
     * Provide MotionTrails - Motion trail effects
     */
    @Provides
    @Singleton
    fun provideMotionTrails(): MotionTrails {
        return MotionTrails()
    }

    /**
     * Provide QuantumAnimationEngine - Master animation orchestrator
     */
    @Provides
    @Singleton
    fun provideQuantumAnimationEngine(): QuantumAnimationEngine {
        return QuantumAnimationEngine.getInstance()
    }

    // ========== PHASE 3: HOLOGRAPHIC UI PROVIDERS ==========

    /**
     * Provide DepthEngine - 3D depth and parallax
     */
    @Provides
    @Singleton
    fun provideDepthEngine(@ApplicationContext context: Context): DepthEngine {
        return DepthEngine.getInstance(context)
    }

    /**
     * Provide ParallaxController - Multi-layer parallax
     */
    @Provides
    @Singleton
    fun provideParallaxController(@ApplicationContext context: Context): ParallaxController {
        return ParallaxController.getInstance(context)
    }

    /**
     * Provide HolographicShader - Iridescent effects
     */
    @Provides
    @Singleton
    fun provideHolographicShader(): HolographicShader {
        return HolographicShader()
    }

    /**
     * Provide LightSystem - Dynamic lighting
     */
    @Provides
    @Singleton
    fun provideLightSystem(): LightSystem {
        return LightSystem()
    }

    /**
     * Provide ReflectionMapper - Environment reflections
     */
    @Provides
    @Singleton
    fun provideReflectionMapper(@ApplicationContext context: Context): ReflectionMapper {
        return ReflectionMapper(context)
    }

    // ========== PHASE 4: INFINITE WIDGET PROVIDERS ==========

    /**
     * Provide InfiniteCanvas - Unlimited widget canvas
     */
    @Provides
    @Singleton
    fun provideInfiniteCanvas(@ApplicationContext context: Context): InfiniteCanvas {
        return InfiniteCanvas.getInstance(context)
    }

    /**
     * Provide WidgetStackManager - Widget layering
     */
    @Provides
    @Singleton
    fun provideWidgetStackManager(): WidgetStackManager {
        return WidgetStackManager()
    }

    /**
     * Provide WidgetBuilder - Widget creation
     */
    @Provides
    @Singleton
    fun provideWidgetBuilder(): WidgetBuilder {
        return WidgetBuilder()
    }

    // ========== PHASE 5: SENTIENT ASSISTANT PROVIDERS ==========

    /**
     * Provide SentientAssistant - AI launcher intelligence
     */
    @Provides
    @Singleton
    fun provideSentientAssistant(@ApplicationContext context: Context): SentientAssistant {
        return SentientAssistant.getInstance(context)
    }

    /**
     * Provide AppPredictor - ML app prediction
     */
    @Provides
    @Singleton
    fun provideAppPredictor(@ApplicationContext context: Context): AppPredictor {
        return AppPredictor(context)
    }

    /**
     * Provide VoiceCustomizer - Voice commands
     */
    @Provides
    @Singleton
    fun provideVoiceCustomizer(@ApplicationContext context: Context): VoiceCustomizer {
        return VoiceCustomizer.getInstance(context)
    }

    /**
     * Provide RoutineDetector - Routine detection
     */
    @Provides
    @Singleton
    fun provideRoutineDetector(@ApplicationContext context: Context): RoutineDetector {
        return RoutineDetector.getInstance(context)
    }

    /**
     * Provide AttentionTracker - Eye tracking
     */
    @Provides
    @Singleton
    fun provideAttentionTracker(@ApplicationContext context: Context): AttentionTracker {
        return AttentionTracker.getInstance(context)
    }

    // ========== PHASE 6: DIMENSIONAL GESTURE PROVIDERS ==========

    /**
     * Provide DimensionalGestureEngine - Advanced gesture recognition
     */
    @Provides
    @Singleton
    fun provideDimensionalGestureEngine(@ApplicationContext context: Context): DimensionalGestureEngine {
        return DimensionalGestureEngine.getInstance(context)
    }

    /**
     * Provide AirGestureRecognizer - Camera-based gestures
     */
    @Provides
    @Singleton
    fun provideAirGestureRecognizer(@ApplicationContext context: Context): AirGestureRecognizer {
        return AirGestureRecognizer.getInstance(context)
    }

    /**
     * Provide GestureLibrary - 50+ gesture definitions
     */
    @Provides
    @Singleton
    fun provideGestureLibrary(): GestureLibrary {
        return GestureLibrary()
    }

    /**
     * Provide MultiTouchProcessor - Multi-finger processing
     */
    @Provides
    @Singleton
    fun provideMultiTouchProcessor(): MultiTouchProcessor {
        return MultiTouchProcessor()
    }

    /**
     * Provide PressureSensor - Pressure sensitivity
     */
    @Provides
    @Singleton
    fun providePressureSensor(): PressureSensor {
        return PressureSensor()
    }

    /**
     * Provide GestureMacroRecorder - Gesture recording
     */
    @Provides
    @Singleton
    fun provideGestureMacroRecorder(): GestureMacroRecorder {
        return GestureMacroRecorder()
    }

    // ========== PHASE 7: REALITY OVERLAY PROVIDERS ==========

    /**
     * Provide RealityOverlayEngine - AR integration
     */
    @Provides
    @Singleton
    fun provideRealityOverlayEngine(@ApplicationContext context: Context): RealityOverlayEngine {
        return RealityOverlayEngine.getInstance(context)
    }

    /**
     * Provide ARIconSystem - AR icon management
     */
    @Provides
    @Singleton
    fun provideARIconSystem(): ARIconSystem {
        return ARIconSystem()
    }

    /**
     * Provide SpatialFolderManager - Spatial folder management
     */
    @Provides
    @Singleton
    fun provideSpatialFolderManager(): SpatialFolderManager {
        return SpatialFolderManager()
    }

    /**
     * Provide WorldAnchorSystem - World anchor management
     */
    @Provides
    @Singleton
    fun provideWorldAnchorSystem(): WorldAnchorSystem {
        return WorldAnchorSystem()
    }

    /**
     * Provide ContextLayerRenderer - Context layer rendering
     */
    @Provides
    @Singleton
    fun provideContextLayerRenderer(): ContextLayerRenderer {
        return ContextLayerRenderer()
    }

    /**
     * Provide ARSearchEngine - AR search functionality
     */
    @Provides
    @Singleton
    fun provideARSearchEngine(): ARSearchEngine {
        return ARSearchEngine()
    }
}
