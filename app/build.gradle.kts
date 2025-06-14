plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.dagger.hilt.android")
}


android {
    namespace = "com.example.kasperchat_test"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.kasperchat_test"
        minSdk = 29
        //noinspection OldTargetApi
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            buildConfigField("String","SERVER_IP","\"192.168.1.42\"")
            buildConfigField("String","SERVER_PORT","\"5012\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            buildConfigField("String","SERVER_IP","\"192.168.1.42\"")
            buildConfigField("String","SERVER_PORT","\"5012\"")
        }
    }

    buildFeatures{
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
}
dependencies {
    // ... other dependencies
    // annotationProcessor "com.github.bumptech.glide:compiler:4.12.0" // For Java projects
    // For Kotlin projects, use kapt instead of annotationProcessor
}
dependencies {
    implementation(libs.glide)
    kapt(libs.compiler)
    implementation(libs.coil)
    implementation (libs.roundedimageview)
    implementation(libs.google.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.signalr.v504)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.logging.interceptor)
    implementation (libs.circleimageview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation("com.google.android.material:material:1.11.0") // или актуальная
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}
kapt {
    correctErrorTypes = true
}