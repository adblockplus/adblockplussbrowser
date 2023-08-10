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
import com.google.protobuf.gradle.builtins
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import net.pwall.json.kotlin.codegen.gradle.JSONSchemaCodegenPlugin

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("com.google.protobuf")
    id("dagger.hilt.android.plugin")
}

applyCommonConfig()

buildscript {
    dependencies {
        classpath(libs.json.kotlin.gradle)
    }
}
apply<JSONSchemaCodegenPlugin>()

createFlavorsConfig()

// if not added causing conflict during gradle build
// similar issue https://github.com/plaid/plaid-link-android/issues/169
// TODO find out what plugin has dependency to `proto-javalite`
android {
    packagingOptions {
        resources.excludes.add("google/protobuf/*.proto")
    }
}

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
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

dependencies {
    implementation(project(":base"))
    implementation(project(":settings"))

    implementation(libs.androidx.datastore)
    implementation(libs.androidx.work.runtime)
    implementation(libs.gildor.coroutines.okhttp)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okio)
    implementation(libs.okhttp3)
//    implementation(libs.okhttp3.logging.interceptor)
    implementation(libs.protobuf.kotlin.lite)
    implementation(libs.timber)

    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.hilt.work)
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.okhttp3.mockwebserver)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.hilt.testing)
    testAnnotationProcessor(libs.hilt.compiler)
    kaptTest(libs.hilt.compiler)
    kaptTest(libs.androidx.hilt.compiler)
    kaptAndroidTest(libs.hilt.compiler)
}
