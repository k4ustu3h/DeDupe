import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "k4ustu3h.dedupe"
    compileSdk = 36

    defaultConfig {
        applicationId = "k4ustu3h.dedupe"
        minSdk = 33
        targetSdk = compileSdk
        versionCode = 3
        versionName = "1.1.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_23
        targetCompatibility = JavaVersion.VERSION_23
    }
    buildFeatures {
        viewBinding = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget("23")
    }
}

dependencies {
    implementation(libs.androidveil)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.balloon)
    implementation(libs.groupie)
    implementation(libs.groupie.kotlin.android.extensions)
    implementation(libs.groupie.viewbinding)
    implementation(libs.material)
}