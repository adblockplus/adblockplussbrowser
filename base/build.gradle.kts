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
    id("kotlin-parcelize")
}

applyCommonConfig()

android {
    buildFeatures {
        dataBinding = true
    }
}

createFlavorsConfig()

dependencies {
    implementation(project(":i18n"))

    implementation(Deps.MATERIAL)
    implementation(Deps.AndroidX.ACTIVITY)
    implementation(Deps.AndroidX.APPCOMPAT)
    implementation(Deps.AndroidX.FRAGMENT)
    implementation(Deps.AndroidX.Navigation.FRAGMENT)
    implementation(Deps.AndroidX.DataStore.DATASTORE)
    implementation(Deps.AndroidX.DataStore.PREFERENCES)
    implementation(Deps.AndroidX.Hilt.WORK)
    implementation(Deps.TIMBER)

    testImplementation(Deps.JUNIT)
    testImplementation(Deps.ROBOLECTRIC)
    testImplementation(Deps.Mockito.Kotlin)
}
