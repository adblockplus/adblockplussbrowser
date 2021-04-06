plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdkVersion(Config.COMPILE_SDK)
    buildToolsVersion(Config.BUILD_TOOLS_VERSION)

    defaultConfig {
        minSdkVersion(Config.MIN_SDK)
        targetSdkVersion(Config.TARGET_SDK)
        versionCode = Config.VERSION_CODE
        versionName = Config.VERSION_NAME

        testInstrumentationRunner = Config.ANDROID_TEST_INSTRUMENTATION_RUNNER
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(Deps.KOTLIN.KOTLIN_STDLIB)
    implementation(Deps.KOTLINX.COROUTINES)
    implementation(Deps.KOTLINX.COROUTINES_ANDROID)
    implementation(Deps.TIMBER)
    implementation(Deps.OKIO)
    implementation(Deps.OKHTTP.OKHTTP)
    implementation(Deps.OKHTTP.LOGGER)
    implementation(Deps.ANDROIDX.WORK.RUNTIME)

    testImplementation(Deps.JUNIT)

    androidTestImplementation(Deps.ANDROIDX.TEST.JUNIT)
    androidTestImplementation(Deps.ANDROIDX.TEST.ESPRESSO.CORE)
}