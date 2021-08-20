plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

applyCommonConfig()

dependencies {
    implementation(Deps.Hilt.ANDROID)
    implementation(Deps.Firebase.CRASHLYTICS)
    kapt(Deps.Hilt.ANDROID_COMPILER)
    implementation(platform(Deps.Firebase.BOM))
    implementation(Deps.Firebase.ANALYTICS)
}