plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.service)
    alias(libs.plugins.hilt)
    kotlin("kapt") // Hilt needs kapt
}

android {
    namespace = "com.anshul.smartmediaai"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.anshul.smartmediaai"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    splits {
        abi {
            isEnable = false
            isUniversalApk = true
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY") ?: ""}\"")
            buildConfigField("String", "GOOGLE_API_KEY", "\"${project.findProperty("GOOGLE_API_KEY") ?: ""}\"")
            buildConfigField("String","WEB_CLIENT_ID","\"${project.findProperty("WEB_CLIENT_ID") ?: ""}\"")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY") ?: ""}\"")
            buildConfigField("String", "GOOGLE_API_KEY", "\"${project.findProperty("GOOGLE_API_KEY") ?: ""}\"")
            buildConfigField("String","WEB_CLIENT_ID","\"${project.findProperty("WEB_CLIENT_ID") ?: ""}\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
   /* configurations.all {
        resolutionStrategy {
            force ("com.squareup:kotlinpoet:1.15.3")
            force("org.jetbrains.kotlin:kotlin-stdlib:1.9.20")
            force ("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.20")
            force ("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.20")
            force ("org.jetbrains.kotlin:kotlin-reflect:1.9.20")
        }
    }
*/
    configurations.all {
        resolutionStrategy {
            force ("com.squareup:javapoet:1.13.0") // pick latest stable
        }
    }
}
hilt {
    enableAggregatingTask = false
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // Firebase & Vertex AI
    implementation(platform(libs.firebase.bom))
    implementation (libs.firebase.analytics)
    implementation (libs.firebase.ai)
    //implementation (libs.firebase.vertexai)
    // Jetpack Media3 (ex-video playback, styling, transforms)
    implementation (libs.androidx.media3.exoplayer)
    implementation (libs.androidx.media3.ui)
    implementation (libs.androidx.media3.common)
    implementation (libs.androidx.media3.transformer)
    // Compose UI
    implementation (libs.material3)
    implementation (libs.androidx.activity.compose.v182)
    // hilt
    implementation(libs.hilt.android){
        exclude("org.jetbrains.kotlin", "kotlin-reflect")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk7")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
    }
    implementation (libs.play.services.location)
    implementation(libs.play.services.auth.base)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    //orbit
    // Core of Orbit, providing state management and unidirectional data flow (multiplatform)
    implementation(libs.orbit.core)
// Integrates Orbit with Android and Common ViewModel for lifecycle-aware state handling (Android, iOS, desktop)
    implementation(libs.orbit.viewmodel)
// Enables Orbit support for Jetpack Compose and Compose Multiplatform (Android, iOS, desktop)
    implementation(libs.orbit.compose)
// Simplifies testing with utilities for verifying state and event flows (multiplatform)
    testImplementation(libs.orbit.test)
    //youtube player
    implementation (libs.core)

    implementation (libs.gson)
    //room db
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    //mplibchart
    implementation(libs.mpandroidchart)

    implementation(libs.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    //test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}