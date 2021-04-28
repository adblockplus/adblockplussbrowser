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
    implementation(Deps.AndroidX.APPCOMPAT)
    implementation(Deps.AndroidX.CORE)
    implementation(Deps.AndroidX.CONSTRAINT_LAYOUT)
    implementation(Deps.Kotlin.KOTLIN_STDLIB)
    implementation(Deps.KotlinX.COROUTINES)
    implementation(Deps.KotlinX.COROUTINES_ANDROID)
}