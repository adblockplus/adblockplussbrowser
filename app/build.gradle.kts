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
        versionName = "0.0.0"
    }

    createFlavorsConfig()

    buildFeatures {
        dataBinding = true
    }

    signingConfigs {
        named("debug") {
            storeFile = rootProject.file("debug.keystore")
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

    implementation(libs.material)
    implementation(libs.timber)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.constraintlayout)
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
    implementation(libs.gms.play.services.oss.licenses)
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
