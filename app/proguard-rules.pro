# ═════════════════════════════════════════════════════════════
# SugarMunch ProGuard Rules
# ═════════════════════════════════════════════════════════════
#
# This file contains ProGuard/Rules for code obfuscation and optimization.
# Rules are organized by category with specific keep rules for required classes.
#
# IMPORTANT: Avoid overly broad keep rules that expose your code structure.
# Only keep classes that are absolutely necessary for reflection, serialization, etc.

# ═════════════════════════════════════════════════════════════
# BASIC OBFUSCATION SETTINGS
# ═════════════════════════════════════════════════════════════

# Enable optimization
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# Enable code shrinking, obfuscation, and optimization
-shrink
-obfuscate
-optimize

# Set obfuscation level
-optimizationpasses 7

# Use mixed case for obfuscated class names (harder to reverse engineer)
-useuniqueclassmembernames

# Remove unused attributes
-allowaccessmodification

# Merge classes where possible
-classmerging

# ═════════════════════════════════════════════════════════════
# PACKAGE OBFUSCATION - SECURITY TRADE-OFF
# ═════════════════════════════════════════════════════════════

# SECURITY: Package names are now obfuscated for better protection against reverse engineering.
# Previously used -dontobfuscatepackage which made class hierarchies visible.
#
# If you need package names preserved for specific reflection scenarios:
# -adaptclassstrings com.sugarmunch.app.specificpackage.**
# -keep class com.sugarmunch.app.specificpackage.** { *; }
#
# For debugging crashes: Use the mapping.txt file generated in build/outputs/mapping/
# with retrace.sh (Android SDK) to deobfuscate stack traces.
#
# Removed for security (previously was):
# -dontobfuscatepackage

# Keep SourceFile and LineNumberTable for crash debugging (maps to obfuscated names)
# Remove these lines for maximum obfuscation (but crash stacks become harder to read)
-keepattributes SourceFile,LineNumberTable

# Rename source file attribute to something generic
-renamesourcefileattribute SourceFile

# ═════════════════════════════════════════════════════════════
# KOTLIN & COROUTINES
# ═════════════════════════════════════════════════════════════

# Keep Kotlin metadata for reflection
-keepattributes *Annotation*, RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations

# Keep Kotlin companion objects
-keepclassmembers class ** {
    public static ** Companion;
}

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses, Signature
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ═════════════════════════════════════════════════════════════
# COMPOSE
# ═════════════════════════════════════════════════════════════

# Keep Compose compiler generated classes
-keep class androidx.compose.** { *; }
-keep class android.compose.** { *; }

# Keep Compose runtime
-keepclassmembers class androidx.compose.runtime.** { *; }

# Keep Compose UI
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material.** { *; }
-keep class androidx.compose.material3.** { *; }

# Keep Compose animation classes
-keep class androidx.compose.animation.** { *; }

# ═════════════════════════════════════════════════════════════
# HILT & DEPENDENCY INJECTION
# ═════════════════════════════════════════════════════════════

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }

# Keep Hilt workers
-keep class * extends androidx.hilt.work.HiltWorker { *; }

# Keep classes with Hilt annotations
-keepclassmembers,allowobfuscation class * {
    @javax.inject.* *;
    @dagger.* *;
    @hilt.* *;
}

# ═════════════════════════════════════════════════════════════
# ROOM DATABASE
# ═════════════════════════════════════════════════════════════

# Keep Room entities and DAOs
-keep class com.sugarmunch.app.data.model.** { *; }
-keep class com.sugarmunch.app.data.local.dao.** { *; }

# Keep Room generated classes
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class *
-keepclassmembers class * extends androidx.room.RoomDatabase {
    *** *();
    void *(...);
}

# Keep Room annotations
-keepattributes *Annotation*
-keep class androidx.room.** { *; }

# ═════════════════════════════════════════════════════════════
# RETROFIT & OKHTTP
# ═════════════════════════════════════════════════════════════

# Keep Retrofit annotations
-keepattributes RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations

# Keep Retrofit service interfaces
-keep,allowobfuscation,allowshrinking interface * {
    @retrofit2.http.* <methods>;
}

# Keep OkHttp
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# ═════════════════════════════════════════════════════════════
# GSON & SERIALIZATION
# ═════════════════════════════════════════════════════════════

# Keep Gson serialization classes
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.sugarmunch.app.data.model.** { *; }
-keep class com.sugarmunch.app.shop.** { *; }
-keep class com.sugarmunch.app.rewards.** { *; }
-keep class com.sugarmunch.app.events.** { *; }

# Keep fields with Gson annotations
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ═════════════════════════════════════════════════════════════
# FIREBASE & CRASHLYTICS
# ═════════════════════════════════════════════════════════════

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Crashlytics
-keep class com.google.android.gms.common.** { *; }
-keep class com.google.firebase.crashlytics.** { *; }

# Keep Firebase model classes for Firestore
-keep class com.sugarmunch.app.clan.model.** { *; }
-keep class com.sugarmunch.app.features.model.** { *; }

# ═════════════════════════════════════════════════════════════
# WORK MANAGER
# ═════════════════════════════════════════════════════════════

# Keep WorkManager workers
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }

# Keep WorkManager annotations
-keepattributes *Annotation*

# ═════════════════════════════════════════════════════════════
# BROADCAST RECEIVERS & SERVICES
# ═════════════════════════════════════════════════════════════

# Keep broadcast receivers
-keep class * extends android.content.BroadcastReceiver { *; }

# Keep services
-keep class * extends android.app.Service { *; }

# Keep content providers
-keep class * extends android.content.ContentProvider { *; }

# ═════════════════════════════════════════════════════════════
# CUSTOM VIEWS & UI
# ═════════════════════════════════════════════════════════════

# Keep custom views (needed for XML inflation)
-keep class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
    *** get*();
}

# Keep adapters
-keep class * extends android.widget.Adapter { *; }

# Keep fragments
-keep class * extends androidx.fragment.app.Fragment { *; }

# Keep activities
-keep class * extends android.app.Activity { *; }

# ═════════════════════════════════════════════════════════════
# ENUMS & PARCELABLES
# ═════════════════════════════════════════════════════════════

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ═════════════════════════════════════════════════════════════
# REFLECTION & DYNAMIC LOADING
# ═════════════════════════════════════════════════════════════

# Keep classes accessed via reflection
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep plugin classes (for dynamic loading)
-keep class com.sugarmunch.app.plugin.** { *; }
-keep interface com.sugarmunch.app.plugin.** { *; }

# ═════════════════════════════════════════════════════════════
# NATIVE METHODS (JNI)
# ═════════════════════════════════════════════════════════════

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# ═════════════════════════════════════════════════════════════
# LOGGING (REMOVE IN PRODUCTION)
# ═════════════════════════════════════════════════════════════

# Remove logging calls in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep error and warning logs for crash analysis
-keepclassmembers class android.util.Log {
    public static *** e(...);
    public static *** w(...);
    public static *** wtf(...);
}

# ═════════════════════════════════════════════════════════════
# SECURITY - DO NOT REMOVE
# ═════════════════════════════════════════════════════════════

# Keep security-related classes
-keep class com.sugarmunch.app.security.** { *; }
-keep interface com.sugarmunch.app.security.** { *; }

# Keep signature verification
-keep class com.sugarmunch.app.security.ApkSignatureVerifier { *; }
-keep class com.sugarmunch.app.security.SecurityConfig { *; }

# Keep encryption classes
-keep class com.sugarmunch.app.p2p.TransferEncryption { *; }

# ═════════════════════════════════════════════════════════════
# DEBUGGING & CRASH REPORTING
# ═════════════════════════════════════════════════════════════

# Keep line numbers for crash reports (optional - increases APK size)
-keepattributes SourceFile,LineNumberTable

# Emit mapping file for deobfuscating crash reports (upload to Crashlytics on release)
-printmapping build/outputs/mapping/release/mapping.txt

# Optional: use -applymapping when doing incremental release builds for stable obfuscation.
# Do not enable on first/clean build (mapping file may not exist).
# -applymapping build/outputs/mapping/release/mapping.txt
