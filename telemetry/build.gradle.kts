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
import net.pwall.json.kotlin.codegen.gradle.JSONSchemaCodegen
import net.pwall.json.kotlin.codegen.gradle.JSONSchemaCodegenTask
import org.jetbrains.kotlin.konan.properties.Properties

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

hilt {
    // disable if it causes error during build
    enableAggregatingTask = true
}
kapt {
    correctErrorTypes = true
}

// Stuff for JSONSchemaCodegen plugin
// This code has been taken from `JSONSchemaCodegenPlugin::apply` method
// because for some reason it does not work when called
// from inside `JSONSchemaCodegenPlugin::apply` method
project.extensions.create<JSONSchemaCodegen>("jsonSchemaCodegen", project)
val generateTask =
    project.tasks.register<JSONSchemaCodegenTask>("generateJsonSchemaDataClasses") {
        description = "Generates code for specified schemata"
        group = "build"
    }
// Make sure that `JSONSchemaCodegenPlugin` task (`generate`) is executed before `assemble` task
tasks.named("preBuild").configure {
    dependsOn(generateTask)
}

val jsonSchemaOutputDir = "build/generated/source/json-schema/"

configure<JSONSchemaCodegen> {
    outputDir.set(file(jsonSchemaOutputDir))
}

/*
* We read environment variables and local config file, then merge them together
* and pass to build config.
* All environment variables should start with `EYEO_` prefix are added to build config.
* For example:
* `EYEO_TELEMETRY_ENDPOINT_URL` will be added as `BuildConfig.EYEO_TELEMETRY_ENDPOINT_URL`
* Also, any variable from local config file will be added to build config and override
* the same variable from environment variables.
*
* Local config file is `config.local.properties` and should be placed
* in the root of the ":telemetry" module
*/
System.getenv().filter { (key, _) ->
    key.startsWith("EYEO_")
}.toMutableMap().also { envVars ->
    File(projectDir, "config.local.properties").takeIf { file -> file.exists() }
        ?.let { file ->
            Properties().apply {
                load(file.inputStream())
                stringPropertyNames().forEach { key ->
                    envVars[key] = getProperty(key)
                }
            }
        }
}.forEach { (key, value) ->
    android.defaultConfig.buildConfigField(
        "String",
        key.toString(),
        "\"$value\""
    )
}


android {
    // consumed by `json-kotlin-schema-gradle` plugin
    sourceSets.getByName("main") {
        java.srcDirs(jsonSchemaOutputDir)
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
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okio)
    implementation(libs.okhttp3)
    implementation(libs.okhttp3.logging.interceptor)
    implementation(libs.protobuf.javalite)
    implementation(libs.timber)

    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.hilt.work)
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)
    // required for `@HiltWorker` annotation
    kapt(libs.androidx.hilt.compiler)

    testImplementation(project(":test-utils"))
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.okhttp3.mockwebserver)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.hilt.testing)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.androidx.lifecycle.runtime.testing)
    testAnnotationProcessor(libs.hilt.compiler)
    kaptTest(libs.hilt.compiler)
    kaptTest(libs.androidx.hilt.compiler)
    kaptAndroidTest(libs.hilt.compiler)
}
