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

    implementation(Deps.MATERIAL)
    implementation(Deps.TIMBER)
    implementation(Deps.AndroidX.APPCOMPAT)
    implementation(Deps.AndroidX.CONSTRAINT_LAYOUT)
    implementation(Deps.AndroidX.PREFERENCE)
    implementation(Deps.AndroidX.Lifecycle.LIVEDATA)
    implementation(Deps.AndroidX.Lifecycle.VIEWMODEL)
    implementation(Deps.Apache.COMMONS_VALIDATOR)
    implementation(Deps.Hilt.ANDROID)
    implementation(Deps.AndroidX.Navigation.FRAGMENT)
    implementation(Deps.AndroidX.Navigation.UI)
    kapt(Deps.Hilt.ANDROID_COMPILER)
    implementation(Deps.MaterialDialogs.CORE)
    implementation(Deps.MaterialDialogs.INPUT)
    implementation(Deps.Gms.OSS_LICENSES)
    implementation(Deps.SPEED_DIAL)
}

