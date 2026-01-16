import com.android.build.api.dsl.ApplicationExtension

plugins {
    alias(libs.plugins.android.application)
}

configure<ApplicationExtension> {
    namespace = "io.github.toyota32k.server.sample"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.toyota32k.server.sample"
        minSdk = 26
        targetSdk = 36
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