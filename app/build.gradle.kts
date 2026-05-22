plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.deskpet"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.deskpet"
        minSdk = 26
        targetSdk = 34
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("org.nanohttpd:nanohttpd:2.3.1")
}