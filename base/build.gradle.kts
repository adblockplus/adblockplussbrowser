@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
}

applyCommonConfig()

android {
    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    implementation(Deps.MATERIAL)
    implementation(Deps.AndroidX.ACTIVITY)
    implementation(Deps.AndroidX.APPCOMPAT)
    implementation(Deps.AndroidX.FRAGMENT)
    implementation(Deps.AndroidX.Navigation.FRAGMENT)
}