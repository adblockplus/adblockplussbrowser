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

buildscript {
    dependencies {
        // this lib does not have an id, therefore cannot be used `plugin` syntax
        classpath(libs.json.kotlin.gradle)
    }
}

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    // Referencing `libs` raises "LibrariesForLibs'
    // can't be called in this context by implicit receiver."
    // TODO needs Gradle version update
    @Suppress("DSL_SCOPE_VIOLATION", "UnstableApiUsage")
    alias(libs.plugins.kotlinx.plugin.serialization)
    id("com.google.protobuf")
    id("dagger.hilt.android.plugin")
}

applyCommonConfig()

createFlavorsConfig()

kapt {
    correctErrorTypes = true
}

apply<JSONSchemaCodegenPlugin>()

project.tasks.findByName("prepareKotlinBuildScriptModel")?.dependsOn(project.tasks.getByName("generate"))

android {
    // consumed by `json-kotlin-schema-gradle` plugin
    sourceSets.getByName("main") {
        java.srcDirs("build/generated-sources/kotlin")
    }
    compileOptions {
        @Suppress("UnstableApiUsage")
        // this is needed for `OffsetDateTime` java class that is used in json serialization
        isCoreLibraryDesugaringEnabled = true
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
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okio)
    implementation(libs.okhttp3)
    implementation(libs.protobuf.javalite)
    implementation(libs.timber)

    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.hilt.work)
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)
    // required for `@HiltWorker` annotation
    kapt(libs.androidx.hilt.compiler)

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

    coreLibraryDesugaring(libs.desugar.jdk.libs)
}
