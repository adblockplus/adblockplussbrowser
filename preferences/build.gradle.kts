import com.google.protobuf.gradle.*

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

applyCommonConfig()

dependencies {
    implementation(project(":settings"))

    implementation(Deps.AndroidX.APPCOMPAT)
    implementation(Deps.AndroidX.Navigation.FRAGMENT)
}