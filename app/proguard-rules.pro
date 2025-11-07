# =================================================================================
# 1. General Android & Kotlin Rules
# =================================================================================

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep custom Parcelable classes
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Keep custom Application, Activity, Service, etc., and their default constructors
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity
#-keep public class * extends androidx.appcompat.app.AppCompatActivity
-keep public class * extends androidx.activity.ComponentActivity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
-keep public class * extends androidx.fragment.app.Fragment
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# Kotlin Coroutines
# This is important for suspend functions and coroutine machinery
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# =================================================================================
# 2. Dependency-Specific Rules
# =================================================================================

# --- Hilt ---
# Hilt's Gradle plugin adds most necessary rules, but these are good fallbacks.
-keep class dagger.hilt.internal.aggregatedroot.codegen.** { *; }
-keep class com.anshul.smartmediaai.Hilt_** { *; } # Replace with your app's package name
-keep class hilt_aggregated_deps.** { *; }
-dontwarn dagger.hilt.**
-keep class * extends androidx.lifecycle.ViewModel
-keep class * extends androidx.work.Worker

# --- Room ---
# Keep your Entity classes, as Room uses them with reflection.
-keep class com.anshul.smartmediaai.data.entities.** { *; } # Adjust package if needed
-keep class com.anshul.smartmediaai.data.local.db.entity.** { *; } # As used in a previous example
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static final androidx.room.RoomDatabase$Callback sCallback;
}

# --- Retrofit & Gson ---
# Keep data model classes used for serialization/deserialization.
# Assuming they are in a `model` or `dto` package. Adjust as needed.
-keep class com.anshul.smartmediaai.data.model.** { *; }
-keep class com.anshul.smartmediaai.data.remote.** { *; }
-keep class com.google.gson.** { *; }
-keepattributes Signature, InnerClasses, *Annotation*

# For Retrofit 3.0+ using Java 8+ features like default methods in interfaces
-dontwarn java.lang.invoke.*

# --- Firebase Crashlytics & Analytics ---
# Firebase and Google Services plugins usually handle this, but explicit rules don't hurt.
-keep class com.google.firebase.crashlytics.** { *; }
-keep class com.google.android.gms.measurement.** { *; }
-keepattributes *Annotation*

# Firebase Analytics / Google Play Measurement
-keep class com.google.android.gms.internal.measurement.** { *; }

# Prevent removing generic type info
-keepattributes Signature


# --- Google Sign-In & Credentials Manager (Google ID) ---
# Keep model classes used by the Google Identity services.
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.libraries.identity.** { *; }
-keep class androidx.credentials.** { *; }

# --- MPAndroidChart ---
# Keep classes from the charting library.
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# --- Orbit MVI ---
# Keep state and side effect classes.
-keep class com.anshul.smartmediaai.ui.compose.expensetracker.state.** { *; } # Keep your State/SideEffect classes

# --- Jsoup ---
# Keep classes from the Jsoup library for HTML parsing.
-dontwarn org.jsoup.**
-keep class org.jsoup.** { *; }

# =================================================================================
# 3. WorkManager ---
# Your `GmailSyncWorker` and `CleanUpWorker` must be kept.
# =================================================================================
-keep public class com.anshul.smartmediaai.core.wm.GmailSyncWorker
-keep public class com.anshul.smartmediaai.core.wm.CleanUpWorker

# =================================================================================
# 4. Jetpack Compose ---
# The Compose compiler plugin and R8 work well together, but if you have issues with
# composables being removed, you might need specific rules. Usually not needed.
# =================================================================================
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-keepclassmembers class **.R$* {
    <fields>;
}

-keepattributes Signature, InnerClasses, EnclosingMethod

# Keep Guava reflection classes
-keep class com.google.common.reflect.** { *; }
-keepclassmembers class com.google.common.reflect.** { *; }

# Keep Gson/Retrofit models if applicable
-keep class retrofit2.** { *; }

-keep class com.google.api.** { *; }
-keep class com.google.cloud.aiplatform.** { *; }

-keep class com.google.common.reflect.TypeToken { *; }
-keep class * extends com.google.common.reflect.TypeToken

