plugins {
    id("com.android.library")
    kotlin("android")
}

applyCommonConfig()

dependencies {
    implementation(Deps.TIMBER)
    implementation(Deps.OKIO)
    implementation(Deps.ANDROIDX.WORK.RUNTIME)
    implementation(Deps.KOTLIN.KOTLIN_STDLIB)
    implementation(Deps.KOTLINX.COROUTINES)
    implementation(Deps.KOTLINX.COROUTINES_ANDROID)
    implementation(Deps.OKHTTP.OKHTTP)
    implementation(Deps.OKHTTP.LOGGER)
}