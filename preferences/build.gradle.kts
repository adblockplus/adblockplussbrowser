@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("androidx.navigation.safeargs.kotlin")
}

applyCommonConfig()

android {
    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    implementation(project(":base"))
    implementation(project(":settings"))
    implementation(project(":core"))

    implementation(Deps.MATERIAL)
    implementation(Deps.AndroidX.APPCOMPAT)
    implementation(Deps.AndroidX.CONSTRAINT_LAYOUT)
    implementation(Deps.AndroidX.PREFERENCE)
    implementation(Deps.AndroidX.Lifecycle.LIVEDATA)
    implementation(Deps.AndroidX.Lifecycle.VIEWMODEL)
    implementation(Deps.AndroidX.Navigation.FRAGMENT)
    implementation(Deps.Hilt.ANDROID)
    kapt(Deps.Hilt.ANDROID_COMPILER)
    implementation(Deps.MaterialDialogs.CORE)
    implementation(Deps.MaterialDialogs.INPUT)
}