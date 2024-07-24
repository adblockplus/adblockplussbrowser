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

import com.google.protobuf.gradle.GenerateProtoTask
import com.google.protobuf.gradle.id

plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
    id("com.google.protobuf")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.download)
    kotlin("kapt")
    alias(libs.plugins.ksp)
    alias(libs.plugins.dagger.hilt.android)
}

// workaround for "[ksp] NonExistentClass' could not be resolved." error
androidComponents {
    onVariants(selector().all()) { variant ->
        afterEvaluate {
            val protoTask =
                project.tasks.getByName("generate" + variant.name.replaceFirstChar { it.uppercaseChar() } + "Proto") as GenerateProtoTask

            project.tasks.getByName("ksp" + variant.name.replaceFirstChar { it.uppercaseChar() } + "Kotlin") {
                dependsOn(protoTask)
                (this as org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool<*>).setSource(
                    protoTask.outputBaseDir
                )
            }
        }
    }
}

applyCommonConfig()

createFlavorsConfig()

dependencies {
    implementation(project(":analytics"))
    implementation(project(":base"))
    implementation(project(":settings"))
    implementation(project(":telemetry"))

    implementation(libs.androidx.datastore)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.work.runtime)
    implementation(libs.gildor.coroutines.okhttp)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okio)
    implementation(libs.okhttp3)
    implementation(libs.okhttp3.logging.interceptor)
    implementation(libs.protobuf.javalite)
    implementation(libs.timber)
    implementation(libs.xz)

    implementation(libs.hilt)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.okhttp3.mockwebserver)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.hilt.testing)
    testImplementation(project(":test-utils"))
    testAnnotationProcessor(libs.hilt.compiler)
    kspTest(libs.hilt.compiler)
    kspTest(libs.androidx.hilt.compiler)
    kspAndroidTest(libs.hilt.compiler)
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

hilt {
    // disable if it causes error during build
    enableAggregatingTask = false
}

tasks.register("downloadExceptionRules", de.undercouch.gradle.tasks.download.Download::class) {
    val flavor = project.findProperty("flavor")?.toString()?.lowercase() ?: "abp"
    val baseDir = if (flavor == "abp") "src/main/assets" else "src/$flavor/assets"

    val source = when (flavor) {
        "abp" -> "https://0.samsung-internet.filter-list-downloads.eyeo.com/" +
                "aa-variants/samsung_internet_browser-adblock_plus.txt"

        "adblock" -> "https://0.samsung-internet.filter-list-downloads.getadblock.com/" +
                "aa-variants/samsung_internet_browser-adblock.txt"

        "crystal" -> "https://0.samsung-internet.filter-list-downloads.eyeo.com/" +
                "aa-variants/samsung_internet_browser-crystal.txt"

        else -> throw GradleException("Given flavor <$flavor> not supported")
    }
    src(source)
    dest("$baseDir/exceptionrules.txt")
}

tasks.register("downloadEasyList", de.undercouch.gradle.tasks.download.Download::class) {
    val flavor = project.findProperty("flavor")?.toString()?.lowercase() ?: "abp"
    val baseDir = if (flavor == "abp") "src/main/assets" else "src/$flavor/assets"
    src("https://0.samsung-internet.filter-list-downloads.getadblock.com/easylist.txt")
    dest("$baseDir/easylist.txt")
}

tasks.register("packSubscriptionsFiles") {
    val flavor = project.findProperty("flavor")?.toString()?.lowercase() ?: "abp"
    val baseDir = if (flavor == "abp") "src/main/assets" else "src/$flavor/assets"
    val xz = "xz"

    doLast {
        // Delete execptionrules compressed file if exists to avoid "File exists" error
        delete("$baseDir/exceptionrules.txt.xz")
        exec {
            commandLine(
                xz,
                "$baseDir/exceptionrules.txt"
            )
        }
    }
}

tasks.register("checkSubscriptionsFiles") {
    val flavor = project.findProperty("flavor")?.toString()?.lowercase() ?: "abp"
    val baseDir = if (flavor == "abp") "core/src/main/assets" else "core/src/$flavor/assets"

    doLast {
        val easyListLength = File("$baseDir/easylist.txt").length()
        val exceptionRulesLength = File("$baseDir/exceptionrules.txt.xz").length()

        println("$flavor EASYLIST SIZE: ${easyListLength / 1024} KB")
        println("$flavor EXCEPTIONRULES PACKED SIZE: ${exceptionRulesLength / 1024} KB")

        when {
            easyListLength == 0L -> throw GradleException("Something went wrong. Easylist file is empty!")
            exceptionRulesLength == 0L -> throw GradleException("Something went wrong. Exception rules file is empty!")
        }
    }
}

/* To run this task a parameter `flavor` (abp, adblock or crystal) must be provided.
    gradle :core:downloadSubscriptions -Pflavor=adblock (abp if no flavor is provided)
 */
tasks.register("downloadSubscriptions") {
    dependsOn(
        "downloadExceptionRules",
        "downloadEasyList",
        "packSubscriptionsFiles",
        "checkSubscriptionsFiles"
    )
    tasks.getByName("downloadEasyList").mustRunAfter("downloadExceptionRules")
    tasks.getByName("packSubscriptionsFiles").mustRunAfter("downloadEasyList")
    tasks.getByName("checkSubscriptionsFiles").mustRunAfter("packSubscriptionsFiles")
}
