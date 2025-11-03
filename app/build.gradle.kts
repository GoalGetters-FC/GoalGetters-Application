import java.io.FileInputStream
import java.util.Properties

var resolvedGoogleServerClientId: String = ""

val googleServerClientId: String by lazy {
    val gradleProperty = (project.findProperty("GOOGLE_SERVER_CLIENT_ID") as? String)
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
    if (gradleProperty != null) return@lazy gradleProperty

    val environmentValue = System.getenv("GOOGLE_SERVER_CLIENT_ID")
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
    if (environmentValue != null) return@lazy environmentValue

    val properties = Properties()
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists()) {
        FileInputStream(propertiesFile).use { properties.load(it) }
        properties.getProperty("GOOGLE_SERVER_CLIENT_ID")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { return@lazy it }
    }

    ""
}

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
        versionCode = 15
        versionName = "2025w42d (dev)"

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
                FileInputStream(propertiesFile).use { properties.load(it) }
            } else {
                println("Local property file not found.")
            }

            return properties.getProperty(property) ?: ""
        }

        resolvedGoogleServerClientId = googleServerClientId

        if (resolvedGoogleServerClientId.isBlank()) {
            println("[GoalGetters] Warning: GOOGLE_SERVER_CLIENT_ID is not configured. Google Sign-In will be disabled.")
        }

        buildConfigField(
            type = "String",
            name = "GOOGLE_SERVER_CLIENT_ID",
            value = "\"$resolvedGoogleServerClientId\""
        )

        resValue(
            type = "string",
            name = "google_server_client_id",
            value = resolvedGoogleServerClientId
        )

        manifestPlaceholders["googleServerClientId"] = resolvedGoogleServerClientId
    }

    afterEvaluate {
        val isEnvironmentProduction = gradle.startParameter.taskNames.any {
            it.contains("release", ignoreCase = true)
        }

        if (isEnvironmentProduction) {
            if (resolvedGoogleServerClientId.isBlank()) throw GradleException(
                "Essential property is missing from build: provide GOOGLE_SERVER_CLIENT_ID via -P flag, environment variable, or local.properties"
            )
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
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
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics.ndk)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.perf)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    implementation(libs.lottie)
    implementation(libs.gson)
    implementation(libs.hilt.android)
    implementation(libs.androidx.browser)

    kapt(libs.androidx.room.compiler)
    kapt(libs.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
