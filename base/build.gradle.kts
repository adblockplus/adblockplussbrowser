plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
}

applyCommonConfig()

dependencies {
    implementation(Deps.Hilt.ANDROID)
    kapt(Deps.Hilt.ANDROID_COMPILER)
    implementation(Deps.Moshi.KOTLIN)
    kapt(Deps.Moshi.KOTLIN_CODEGEN)
}