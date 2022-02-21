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

import com.google.protobuf.gradle.*

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("com.google.protobuf")
    id("dagger.hilt.android.plugin")
}

applyCommonConfig()

createFlavorsConfig()

dependencies {
    implementation(project(":analytics"))
    implementation(project(":base"))
    implementation(project(":settings"))

    implementation(Deps.TIMBER)
    implementation(Deps.OKIO)
    implementation(Deps.AndroidX.Work.RUNTIME)
    implementation(Deps.Kotlin.KOTLIN_STDLIB)
    implementation(Deps.KotlinX.COROUTINES)
    implementation(Deps.KotlinX.COROUTINES_ANDROID)
    implementation(Deps.OkHttp.OKHTTP)
    implementation(Deps.OkHttp.LOGGER)
    implementation(Deps.OkHttp.COROUTINES)
    implementation(Deps.AndroidX.Work.RUNTIME)
    implementation(Deps.AndroidX.DataStore.DATASTORE)
    implementation(Deps.Protobuf.JAVALITE)
    implementation(Deps.XZ.XZ)

    implementation(Deps.Hilt.ANDROID)
    kapt(Deps.Hilt.ANDROID_COMPILER)
    implementation(Deps.AndroidX.Hilt.COMMON)
    implementation(Deps.AndroidX.Hilt.WORK)
    kapt(Deps.AndroidX.Hilt.COMPILER)

    testImplementation(Deps.JUNIT)
    testImplementation(Deps.OkHttp.MOCK_WEB_SERVER)
    testImplementation(Deps.Mockito.Core)
    testImplementation(Deps.Mockito.Kotlin)
    testImplementation(Deps.Hilt.ANDROID_TESTING)
    testImplementation(Deps.Robolectric.ROBOLECTRIC)
    testAnnotationProcessor(Deps.Hilt.ANDROID_COMPILER)
    kaptTest(Deps.Hilt.ANDROID_COMPILER)
    kaptTest(Deps.AndroidX.Hilt.COMPILER)
    kaptAndroidTest(Deps.Hilt.ANDROID_COMPILER)
}

protobuf {
    protoc {
        artifact = Deps.Protobuf.PROTOC
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                id("java") {
                    option("lite")
                }
            }
        }
    }
}
