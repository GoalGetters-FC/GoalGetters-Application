import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
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

        // --- Environment Variables

        /**
         * Function to retrieve a secret value from a property file.
         *
         * **Note:** This should be used only for the `local.properties` repository
         * and these values should never be shared or committed to a version control
         * system in any form.
         *
         * @param property The name of the property to retrieve.
         * @param filename The name of the file to read from (should not be changed)
         *
         * @return The value of the requested property or an empty string if the
         *         file or property could not be found.
         *
         * @author MP
         */
        fun getLocalSecret(
            property: String, filename: String = "local.properties"
        ): String {
            val properties = Properties()
            val propertiesFile = rootProject.file(filename)
            if (propertiesFile.exists()) {
                properties.load(FileInputStream(propertiesFile))
            } else {
                println("Local property file not found.")
            }

            return properties.getProperty(property) ?: ""
        }
        
        buildConfigField(
            type = "String",
            name = "GOOGLE_SERVER_CLIENT_ID",
            value = "\"${
                getLocalSecret("GOOGLE_SERVER_CLIENT_ID")
            }\""
        )
    }

    afterEvaluate {
        val isEnvironmentProduction = gradle.startParameter.taskNames.any {
            it.contains("release", ignoreCase = true)
        }

        if (isEnvironmentProduction) {
            val clientId = (project.findProperty("GOOGLE_SERVER_CLIENT_ID") as? String).orEmpty()
            if (clientId.isBlank()) throw GradleException(
                "Essential property is missing from build: (use -P GOOGLE_SERVER_CLIENT_ID=...)"
            )
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isDebuggable = false
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.googleid)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

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
    implementation("androidx.hilt:hilt-work:1.2.0")

    // Lottie

    implementation(libs.lottie)
    implementation("com.google.code.gson:gson:2.10.1")
}