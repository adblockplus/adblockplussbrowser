plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdkVersion(Config.COMPILE_SDK)
    buildToolsVersion(Config.BUILD_TOOLS_VERSION)

    defaultConfig {
        applicationId = "org.adblockplus.adblockplussbrowser"
        minSdkVersion(Config.MIN_SDK)
        targetSdkVersion(Config.TARGET_SDK)
        versionCode = Config.VERSION_CODE
        versionName = Config.VERSION_NAME

        testInstrumentationRunner = Config.ANDROID_TEST_INSTRUMENTATION_RUNNER
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        getByName("debug") {
            isMinifyEnabled = false
        }
    }

    flavorDimensions("region", "product")
    productFlavors {
        create("world") {
            dimension = "region"
        }

        create("china") {
            dimension = "region"
            applicationIdSuffix = ".cn"
        }

        create("abp") {
            dimension = "product"
        }

        create("crystal") {
            dimension = "product"
            applicationId = "co.crystalapp.crystal"
        }
    }

    viewBinding {
        android.buildFeatures.viewBinding = true
    }
}

dependencies {
    implementation(project(":core"))

    implementation(Deps.KOTLIN.KOTLIN_STDLIB)
    implementation(Deps.KOTLINX.COROUTINES)
    implementation(Deps.KOTLINX.COROUTINES_ANDROID)
    implementation(Deps.ANDROIDX.APPCOMPAT)
    implementation(Deps.ANDROIDX.CORE)
    implementation(Deps.ANDROIDX.CONSTRAINT_LAYOUT)
    implementation(Deps.MATERIAL)
    implementation(Deps.TIMBER)

    testImplementation(Deps.JUNIT)

    androidTestImplementation(Deps.ANDROIDX.TEST.JUNIT)
    androidTestImplementation(Deps.ANDROIDX.TEST.ESPRESSO.CORE)
}