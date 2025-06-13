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
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            buildConfigField("String","SERVER_IP","\"185.130.224.155\"")
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
        // Включаем десахаризацию для поддержки API Java 8+
        isCoreLibraryDesugaringEnabled = true
        // Убедитесь, что sourceCompatibility и targetCompatibility установлены на Java 8 или выше.
        // У вас уже VERSION_17, что подходит. Если бы было ниже 1.8 (Java 8), нужно было бы поднять.
        sourceCompatibility = JavaVersion.VERSION_1_8 // Можно оставить VERSION_17, но для десахаризации достаточно 1.8
        targetCompatibility = JavaVersion.VERSION_1_8 // Можно оставить VERSION_17
    }
    kotlinOptions {
        // Убедитесь, что jvmTarget установлен на "1.8" или выше.
        // У вас уже "17", что подходит. Если бы было ниже "1.8", нужно было бы поднять.
        jvmTarget = "1.8" // Можно оставить "17"
    }
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)

    // ... other dependencies
    implementation(libs.glide)
    kapt(libs.compiler) // Предполагаю, что это libs.glide.compiler
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
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Добавляем зависимость для десахаризации библиотек JDK
    // Рекомендуется использовать последнюю версию. Проверьте актуальную версию на Maven Central.
    // На момент написания, 2.0.4 была актуальной.
    coreLibraryDesugaring(libs.desugar.jdk.libs) // Предполагая, что вы добавили это в ваш файл libs.versions.toml
    // Если вы не используете каталог версий, то так:
    // coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}

kapt {
    correctErrorTypes = true
}