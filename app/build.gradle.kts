/*
 * This file is part of Adblock Plus <https://adblockplus.org/>,
 * Copyright (C) 2006-present eyeo GmbH
 *
 * Adblock Plus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * Adblock Plus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adblock Plus.  If not, see <http://www.gnu.org/licenses/>.
 */

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
        versionCode = versionCode()
        versionName = Config.VERSION_NAME
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
            copy {
                from("google-services/abp/")
                include("*.json")
                into(".")
            }
        }

        create("crystal") {
            dimension = productDimension
            applicationId = "co.crystalapp.crystal"
            copy {
                from("google-services/crystal/")
                include("*.json")
                into(".")
            }
        }

        create("adblock") {
            dimension = productDimension
            applicationId = "com.betafish.adblocksbrowser"
            copy {
                from("google-services/adblock/")
                include("*.json")
                into(".")
            }
        }
    }

    buildFeatures {
        dataBinding = true
    }

    signingConfigs {
        named("debug") {
            storeFile(rootProject.file("debug.keystore"))
        }
    }
}

dependencies {
    implementation(project(":analytics"))
    implementation(project(":base"))
    implementation(project(":core"))
    implementation(project(":i18n"))
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
    implementation(Deps.Android.INSTALL_REFERRER)
}

// Install commit pre-hook
tasks.register<Copy>("installPrePushHook") {
    from(rootProject.file("scripts/pre-push"))
    into(rootProject.file(".git/hooks"))
}

tasks.named("preBuild") {
    dependsOn("installPrePushHook")
}
