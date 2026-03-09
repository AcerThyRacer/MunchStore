# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Data Layer classes
-keep class com.google.android.gms.wearable.** { *; }
-keep class com.google.android.gms.common.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }
-keep class androidx.wear.compose.** { *; }

# Keep Horologist
-keep class com.google.android.horologist.** { *; }

# Keep our model classes
-keep class com.sugarmunch.wear.data.** { *; }

# Keep Wearable Data Layer API
-keepclassmembers class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Remove logging
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
}
