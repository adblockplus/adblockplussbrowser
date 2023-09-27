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
    defaultConfig {
        // Access the custom property from the :app module
        val appInfo = project(":app").property("appInfo") as Map<*, *>?

        // Check if appInfo is not null
        if (appInfo != null) {
            buildConfigField("String", "APPLICATION_ID", "\"${appInfo["applicationId"]}\"")
            buildConfigField("String", "APPLICATION_VERSION", "\"${appInfo["versionName"]}\"")
        } else {
            val errorMsg = "Error: Unable to retrieve app info from :app module. " +
                    "\tPlease, add the following snippet into app/build.gradle.kts: " +
                    "\textra[\"appInfo\"] = mapOf(\n" +
                    "\t\t\"applicationId\" to applicationId,\n" +
                    "\t\t\"versionName\" to versionName\n" +
                    "\t)"

            System.err.println(errorMsg)
        }
    }
}

createFlavorsConfig()

dependencies {
    implementation(project(":i18n"))

    implementation(libs.material)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.hilt.work)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockito.kotlin)
}
