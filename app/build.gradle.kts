plugins {
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.android.application)
}

android {
    namespace = "io.github.toyota32k.server.sample"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.github.toyota32k.server.sample"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
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
    implementation(libs.coreKtx)
    implementation(libs.appCompat)
    implementation(libs.material)
    implementation(libs.constraintLayout)
    implementation(project(":libServer"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidTestExtJunit)
    androidTestImplementation(libs.espressoCore)
}