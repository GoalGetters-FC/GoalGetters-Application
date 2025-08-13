plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("de.mannodermaus.android-junit5")
    kotlin("kapt")
}

android {
    namespace = "com.ggetters.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ggetters.app"
        minSdk = 29
        targetSdk = 36
        versionCode = 5
        versionName = "dev-2025w32a"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isDebuggable = false
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    android {
        testOptions {
            unitTests.isIncludeAndroidResources = true
            unitTests.all {
                it.useJUnitPlatform()
            }
        }
    }

    dependencies {
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.material)
        implementation(libs.androidx.activity)
        implementation(libs.androidx.constraintlayout)
        implementation(libs.firebase.firestore.ktx)
        implementation(libs.androidx.fragment.ktx)
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)

        // Unit Testing
        testImplementation("org.jetbrains.kotlin:kotlin-test:2.0.0")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.0")
        testImplementation("io.mockk:mockk:1.13.13")
        testImplementation("org.assertj:assertj-core:3.26.3")
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
        testImplementation("org.robolectric:robolectric:4.12.2")

        // Integration Testing
        androidTestImplementation("androidx.test.ext:junit:1.2.1")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
        androidTestImplementation("androidx.test.espresso:espresso-contrib:3.6.1")
        androidTestImplementation("io.mockk:mockk-android:1.13.13")

        // Architecture Components
        androidTestImplementation("androidx.arch.core:core-testing:2.2.0")

        //  Test coverage
        testImplementation("org.jacoco:org.jacoco.core:0.8.10")
        // Jetpack Compose

        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.ui)
        implementation(libs.androidx.material3)
        implementation(libs.androidx.ui.tooling.preview)
        implementation(libs.androidx.navigation.compose)
        debugImplementation(libs.androidx.ui.tooling)
        debugImplementation(libs.androidx.ui.test.manifest)
        implementation(libs.androidx.core.splashscreen)

        // Firebase

        implementation(platform(libs.firebase.bom))
        implementation(libs.firebase.analytics)
        implementation(libs.firebase.crashlytics.ndk)
        implementation(libs.firebase.auth)
        implementation(libs.firebase.firestore.ktx)

        // Room

        implementation(libs.androidx.room.runtime)
        implementation(libs.androidx.room.ktx)
        kapt(libs.androidx.room.compiler)

        // Hilt

        implementation(libs.hilt.android)
        kapt(libs.hilt.android.compiler)
        implementation(libs.hilt.navigation.compose)

        // Lottie

        implementation(libs.lottie)

        implementation("com.google.code.gson:gson:2.10.1")
    }
}