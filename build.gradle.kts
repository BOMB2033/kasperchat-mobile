plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android)  apply false

    id("androidx.navigation.safeargs.kotlin") version "2.9.0" apply false
    id("com.google.dagger.hilt.android") version "2.56.2" apply false
}