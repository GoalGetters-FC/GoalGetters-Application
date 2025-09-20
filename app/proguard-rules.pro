# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# =============================================================================
# GOALGETTERS SECURITY CONFIGURATION
# =============================================================================

# Keep line numbers for debugging crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep stack trace information
-keepattributes *Annotation*

# =============================================================================
# FIREBASE CONFIGURATION
# =============================================================================

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Keep Firebase Authentication
-keep class com.google.firebase.auth.** { *; }

# Keep Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firestore.v1.** { *; }

# Keep Firebase Crashlytics
-keep class com.google.firebase.crashlytics.** { *; }
-keep class com.crashlytics.** { *; }

# =============================================================================
# HILT DEPENDENCY INJECTION
# =============================================================================

# Keep Hilt classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep class **_HiltComponents$SingletonC { *; }
-keep class **_*_Impl { *; }

# Keep Hilt modules
-keep @dagger.Module class *
-keep @javax.inject.Inject class *

# =============================================================================
# ROOM DATABASE
# =============================================================================

# Keep Room entities and DAOs
-keep class com.ggetters.app.data.model.** { *; }
-keep class com.ggetters.app.data.local.dao.** { *; }
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep @androidx.room.Database class *

# Keep Room converters
-keep class com.ggetters.app.data.local.converters.** { *; }

# =============================================================================
# AUTHENTICATION & SECURITY
# =============================================================================

# Keep authentication services
-keep class com.ggetters.app.core.services.AuthenticationService { *; }
-keep class com.ggetters.app.core.services.GoogleAuthenticationClient { *; }
-keep class com.ggetters.app.core.services.GlobalAuthenticationListener { *; }
-keep class com.ggetters.app.core.services.ConfigurationsService { *; }

# Keep credential classes
-keep class com.ggetters.app.core.services.Credential { *; }

# =============================================================================
# DATA MODELS & ENTITIES
# =============================================================================

# Keep all data models with their fields
-keep class com.ggetters.app.data.model.User { *; }
-keep class com.ggetters.app.data.model.Team { *; }
-keep class com.ggetters.app.data.model.Event { *; }
-keep class com.ggetters.app.data.model.Attendance { *; }
-keep class com.ggetters.app.data.model.Lineup { *; }
-keep class com.ggetters.app.data.model.Configuration { *; }

# Keep enum classes
-keep enum com.ggetters.app.data.model.** { *; }

# =============================================================================
# REPOSITORIES & SYNC
# =============================================================================

# Keep repository interfaces and implementations
-keep class com.ggetters.app.data.repository.** { *; }
-keep class com.ggetters.app.data.online.** { *; }

# Keep sync workers
-keep class com.ggetters.app.core.sync.** { *; }

# =============================================================================
# VIEW MODELS & UI
# =============================================================================

# Keep ViewModels
-keep class com.ggetters.app.ui.**.viewmodels.** { *; }

# Keep UI state classes
-keep class com.ggetters.app.ui.**.models.** { *; }

# =============================================================================
# KOTLIN & COROUTINES
# =============================================================================

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# =============================================================================
# GSON (if used)
# =============================================================================

# Keep Gson classes
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# =============================================================================
# GENERAL SECURITY
# =============================================================================

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Remove custom logger in release builds
-assumenosideeffects class com.ggetters.app.core.utils.Clogger {
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** d(...);
    public static *** e(...);
}