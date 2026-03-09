import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android)
    alias(libs.ksp)
    alias(libs.plugins.detekt)
}

val keystorePropertiesFile: File = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

fun hasSigningVars(): Boolean {
    return providers.environmentVariable("SIGNING_KEY_ALIAS").orNull != null &&
        providers.environmentVariable("SIGNING_KEY_PASSWORD").orNull != null &&
        providers.environmentVariable("SIGNING_STORE_FILE").orNull != null &&
        providers.environmentVariable("SIGNING_STORE_PASSWORD").orNull != null
}

base {
    val versionCode = project.property("VERSION_CODE").toString().toInt()
    archivesName = "sugargallery-$versionCode"
}

android {
    compileSdk = project.libs.versions.app.build.compileSDKVersion.get().toInt()
    
    defaultConfig {
        applicationId = project.property("APP_ID").toString()
        minSdk = project.libs.versions.app.build.minimumSDK.get().toInt()
        targetSdk = project.libs.versions.app.build.targetSDK.get().toInt()
        versionName = project.property("VERSION_NAME").toString()
        versionCode = project.property("VERSION_CODE").toString().toInt()
        
        // SugarGallery specific - support all candy themes
        vectorDrawables.useSupportLibrary = true
    }
    
    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            register("release") {
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
            }
        } else if (hasSigningVars()) {
            register("release") {
                keyAlias = providers.environmentVariable("SIGNING_KEY_ALIAS").get()
                keyPassword = providers.environmentVariable("SIGNING_KEY_PASSWORD").get()
                storeFile = file(providers.environmentVariable("SIGNING_STORE_FILE").get())
                storePassword = providers.environmentVariable("SIGNING_STORE_PASSWORD").get()
            }
        } else {
            logger.warn("Warning: No signing config found. Build will be unsigned.")
        }
    }
    
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    
    buildTypes {
        debug {
            applicationIdSuffix = ".sugargallery.debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePropertiesFile.exists() || hasSigningVars()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    
    flavorDimensions.add("licensing")
    productFlavors {
        register("foss")
        register("gplay")
    }
    
    sourceSets {
        getByName("main").java.directories.add("src/main/kotlin")
    }
    
    compileOptions {
        val currentJavaVersionFromLibs = JavaVersion.valueOf(libs.versions.app.build.javaVersion.get())
        sourceCompatibility = currentJavaVersionFromLibs
        targetCompatibility = currentJavaVersionFromLibs
    }
    
    dependenciesInfo {
        includeInApk = false
    }
    
    androidResources {
        @Suppress("UnstableApiUsage")
        generateLocaleConfig = true
    }
    
    tasks.withType<KotlinCompile> {
        compilerOptions.jvmTarget.set(
            JvmTarget.fromTarget(project.libs.versions.app.build.kotlinJVMTarget.get())
        )
    }
    
    namespace = project.property("APP_ID").toString()
    
    lint {
        checkReleaseBuilds = false
        abortOnError = true
        warningsAsErrors = false
        baseline = file("lint-baseline.xml")
        lintConfig = rootProject.file("lint.xml")
    }
    
    packaging {
        resources {
            excludes += "META-INF/library_release.kotlin_module"
        }
    }
    
    bundle {
        language {
            enableSplit = false
        }
    }
}

detekt {
    baseline = file("detekt-baseline.xml")
    config.setFrom("$rootDir/detekt.yml")
    buildUponDefaultConfig = true
    allRules = false
}

dependencies {
    // Fossify Commons - base functionality
    implementation(libs.fossify.commons)
    
    // AndroidX
    implementation(libs.androidx.print)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    
    // Image loading and processing
    implementation(libs.android.image.cropper)
    implementation(libs.exif)
    implementation(libs.android.gif.drawable)
    implementation(libs.androidsvg.aar)
    implementation(libs.picasso) {
        exclude(group = "com.squareup.okhttp3", module = "okhttp")
    }
    ksp(libs.glide.compiler)
    implementation(libs.glide)
    
    // Media playback
    implementation(libs.androidx.media3.exoplayer)
    
    // Image format support
    implementation(libs.sanselan)
    implementation(libs.awebp)
    implementation(libs.apng)
    implementation(libs.avif)
    implementation(libs.avif.integration)
    implementation(libs.jxl.integration)
    implementation(libs.zjupure.webpdecoder)
    
    // Photo filters and editing
    implementation(libs.androidphotofilters)
    
    // UI components
    implementation(libs.gestureviews)
    implementation(libs.subsamplingscaleimageview)
    implementation(libs.androidphotofilters)
    
    // SugarMunch Theme Integration
    implementation(project(":app")) // Link to main SugarMunch app for theme sharing
    
    // Utilities
    implementation(libs.okio)
    compileOnly(libs.okhttp)
    
    // Detekt
    detektPlugins(libs.compose.detekt)
}
