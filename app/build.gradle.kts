plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

applyCommonConfig()

android {
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

        create("china") {
            dimension = regionDimension
            applicationIdSuffix = ".cn"
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

    viewBinding {
        android.buildFeatures.viewBinding = true
    }
}

dependencies {
    implementation(project(":core"))

    implementation(Deps.MATERIAL)
    implementation(Deps.TIMBER)
    implementation(Deps.ANDROIDX.APPCOMPAT)
    implementation(Deps.ANDROIDX.CORE)
    implementation(Deps.ANDROIDX.CONSTRAINT_LAYOUT)
    implementation(Deps.KOTLIN.KOTLIN_STDLIB)
    implementation(Deps.KOTLINX.COROUTINES)
    implementation(Deps.KOTLINX.COROUTINES_ANDROID)
}