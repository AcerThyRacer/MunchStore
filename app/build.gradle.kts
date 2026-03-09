plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.sugarmunch.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sugarmunch.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "2.0.0"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
        // OpenWeather API key for reactive themes. Set via OPENWEATHER_API_KEY env or project property.
        buildConfigField("String", "OPENWEATHER_API_KEY", "\"${project.findProperty("OPENWEATHER_API_KEY") ?: System.getenv("OPENWEATHER_API_KEY") ?: ""}\"")
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH") ?: project.findProperty("KEYSTORE_PATH") as String?
            val keystorePassword = System.getenv("KEYSTORE_PASSWORD") ?: project.findProperty("KEYSTORE_PASSWORD") as String?
            val keyAlias = System.getenv("KEY_ALIAS") ?: project.findProperty("KEY_ALIAS") as String? ?: "sugarmunch"
            val keyPassword = System.getenv("KEY_PASSWORD") ?: project.findProperty("KEY_PASSWORD") as String?
            if (keystorePath != null && keystorePassword != null && keyPassword != null) {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (signingConfigs.getByName("release").storeFile?.exists() == true) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
        create("benchmark") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            matchingFallbacks += listOf("release")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlin.time.ExperimentalTime"
        )
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/**"
        }
    }
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
    
    lint {
        abortOnError = true
        checkReleaseBuilds = true
        baseline = file("lint-baseline.xml")
    }
}

// Room schema export for migrations (exportSchema = true)
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Compose BOM
    implementation(platform(libs.compose.bom))
    androidTestImplementation(platform(libs.compose.bom))
    
    // Compose
    implementation(libs.bundles.compose)
    
    // Activity & Core
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    
    // Lifecycle
    implementation(libs.bundles.lifecycle)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    
    // Room for offline caching
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    // Network
    implementation(libs.bundles.network)
    
    // Proton Kit for Proton Drive integration


    // Image loading with disk caching
    implementation(libs.bundles.coil)
    
    // Animations
    implementation(libs.bundles.animations)
    
    // Palette for wallpaper color extraction
    implementation("androidx.palette:palette-ktx:1.0.0")

    // Wear OS for biometric theme integration
    implementation(libs.play.services.wearable)
    
    // ═════════════════════════════════════════════════════════════
    // PHASE 1: STABILITY & PERFORMANCE
    // ═════════════════════════════════════════════════════════════
    
    // Firebase Crashlytics
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
    
    // LeakCanary for memory leak detection
    debugImplementation(libs.leakcanary.android)
    
    // ═════════════════════════════════════════════════════════════
    // PHASE 2: DEPENDENCY INJECTION (HILT)
    // ═════════════════════════════════════════════════════════════
    
    // Hilt
    implementation(libs.bundles.hilt)
    ksp(libs.hilt.android.compiler)
    
    // ═════════════════════════════════════════════════════════════
    // COROUTINES & SERIALIZATION
    // ═════════════════════════════════════════════════════════════
    
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.serialization.json)
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    
    // ═════════════════════════════════════════════════════════════
    // WORK MANAGER
    // ═════════════════════════════════════════════════════════════
    
    implementation(libs.androidx.work.runtime)
    
    // ═════════════════════════════════════════════════════════════
    // TESTING
    // ═════════════════════════════════════════════════════════════
    
    // JUnit 4 & 5
    testImplementation(libs.bundles.test.junit)
    
    // Mocking
    testImplementation(libs.bundles.test.mock)
    
    // Coroutines testing
    testImplementation(libs.kotlinx.coroutines.test)
    
    // Robolectric
    testImplementation(libs.robolectric)
    
    // AndroidX Test
    testImplementation(libs.bundles.test.android)
    
    // Compose Testing
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
    androidTestImplementation(libs.navigation.testing)
    
    // Espresso
    androidTestImplementation(libs.bundles.test.espresso)
    
    // Orchestrator for isolated test execution
    androidTestUtil(libs.orchestrator)
    
    // ═════════════════════════════════════════════════════════════
    // DEBUG TOOLS
    // ═════════════════════════════════════════════════════════════
    
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}

apply(from = "jacoco.gradle.kts")
