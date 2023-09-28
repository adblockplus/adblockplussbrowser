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
    createFlavorsConfig()

    defaultConfig {
        versionCode = versionCode()
        versionName = "0.0.0"
        val languagesSet =
            setOf("en", "ar", "de", "el", "es", "fr", "hu", "it", "ja", "ko", "nl", "pl", "pt", "ru", "tr", "zh-rCN")
        resourceConfigurations.addAll(languagesSet)
    }

    buildFeatures {
        dataBinding = true
    }

    signingConfigs {
        named("debug") {
            storeFile = rootProject.file("debug.keystore")
        }
    }
    compileOptions {
        @Suppress("UnstableApiUsage")
        // this is needed for `OffsetDateTime` java class that is used in json serialization
        isCoreLibraryDesugaringEnabled = true
    }

}

// recommended https://dagger.dev/hilt/gradle-setup.html#add-the-hilt-android-gradle-plugin
kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(project(":analytics"))
    implementation(project(":base"))
    implementation(project(":core"))
    implementation(project(":i18n"))
    implementation(project(":onboarding"))
    implementation(project(":preferences"))
    implementation(project(":settings"))
    implementation(project(":telemetry"))

    implementation(libs.material)
    implementation(libs.timber)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.work.runtime)
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.hilt.work)
    kapt(libs.androidx.hilt.compiler)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.dialogs.core)
    implementation(libs.protobuf.javalite)
    implementation(libs.okhttp3)
    implementation(libs.okhttp3.logging.interceptor)
    implementation(libs.gms.play.services.oss.licenses)
    implementation(libs.installreferrer)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(project(":test-utils"))
}

// Install commit pre-hook
tasks.register<Copy>("installPrePushHook") {
    from(rootProject.file("scripts/pre-push"))
    into(rootProject.file(".git/hooks"))
}

tasks.named("preBuild") {
    dependsOn("installPrePushHook")
}
