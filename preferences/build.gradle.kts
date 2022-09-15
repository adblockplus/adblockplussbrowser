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
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("androidx.navigation.safeargs.kotlin")
}

applyCommonConfig()

android {
    buildFeatures {
        dataBinding = true
    }
}

createFlavorsConfig()
addFeature("allowlisting", "abp")
addFeature("allowlisting", "adblock")

configurations {
    all {
        exclude(module = "commons-logging")
    }
}

dependencies {
    implementation(project(":analytics"))
    implementation(project(":base"))
    implementation(project(":i18n"))
    implementation(project(":settings"))

    implementation(libs.material)
    implementation(libs.timber)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.commons.validator)
    implementation(libs.hilt)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    kapt(libs.hilt.compiler)
    implementation(libs.dialogs.core)
    implementation(libs.dialogs.input)
    implementation(libs.gms.play.services.oss.licenses)
    implementation(libs.speed.dial)
    implementation(libs.okhttp3)
    implementation(libs.gildor.coroutines.okhttp)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.spotlight)

    testImplementation(libs.robolectric)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test )
    testAnnotationProcessor(libs.hilt.compiler)
    testImplementation(libs.okhttp3.mockwebserver)
    testImplementation(libs.androidx.arch.core.testing)
}

