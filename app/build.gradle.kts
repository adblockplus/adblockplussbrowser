@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.application")
    id("com.google.android.gms.oss-licenses-plugin")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

applyCommonConfig()

android {
    defaultConfig {
        versionCode = Config.VERSION_CODE
        versionName = Config.VERSION_NAME
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

    val regionDimension = "region"
    val productDimension = "product"
    flavorDimensions(regionDimension, productDimension)
    productFlavors {
        create("world") {
            dimension = regionDimension
        }

        create("abp") {
            dimension = productDimension
            applicationId = "org.adblockplus.adblockplussbrowser"
        }

        create("crystal") {
            dimension = productDimension
            applicationId = "co.crystalapp.crystal"
        }
    }

    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    implementation(project(":analytics"))
    implementation(project(":base"))
    implementation(project(":core"))
    implementation(project(":onboarding"))
    implementation(project(":preferences"))
    implementation(project(":settings"))

    implementation(Deps.MATERIAL)
    implementation(Deps.TIMBER)
    implementation(Deps.AndroidX.ACTIVITY)
    implementation(Deps.AndroidX.APPCOMPAT)
    implementation(Deps.AndroidX.CORE)
    implementation(Deps.AndroidX.CONSTRAINT_LAYOUT)
    implementation(Deps.AndroidX.DataStore.DATASTORE)
    implementation(Deps.AndroidX.DataStore.PREFERENCES)
    implementation(Deps.AndroidX.Lifecycle.LIVEDATA)
    implementation(Deps.AndroidX.Lifecycle.VIEWMODEL)
    implementation(Deps.AndroidX.Navigation.FRAGMENT)
    implementation(Deps.AndroidX.Navigation.UI)
    implementation(Deps.AndroidX.Work.RUNTIME)
    implementation(Deps.Hilt.ANDROID)
    kapt(Deps.Hilt.ANDROID_COMPILER)
    implementation(Deps.AndroidX.Hilt.COMMON)
    implementation(Deps.AndroidX.Hilt.WORK)
    kapt(Deps.AndroidX.Hilt.COMPILER)
    implementation(Deps.Kotlin.KOTLIN_STDLIB)
    implementation(Deps.KotlinX.COROUTINES)
    implementation(Deps.KotlinX.COROUTINES_ANDROID)
    implementation(Deps.MaterialDialogs.CORE)
    implementation(Deps.Protobuf.JAVALITE)
    implementation(Deps.OkHttp.OKHTTP)
    implementation(Deps.OkHttp.LOGGER)
    implementation(Deps.Gms.OSS_LICENSES)
}
