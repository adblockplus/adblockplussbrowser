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
    id("de.undercouch.download")
    id(Deps.JACOCO)
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

    implementation(Deps.Hilt.ANDROID)
    kapt(Deps.Hilt.ANDROID_COMPILER)
    implementation(Deps.AndroidX.Hilt.COMMON)
    implementation(Deps.AndroidX.Hilt.WORK)
    kapt(Deps.AndroidX.Hilt.COMPILER)

    testImplementation(Deps.JUNIT)
    testImplementation(Deps.OkHttp.MOCK_WEB_SERVER)
    testImplementation(Deps.Mockito.Core)
    testImplementation(Deps.Mockito.Kotlin)
    testImplementation(Deps.ROBOLECTRIC)
    testImplementation(Deps.AndroidX.TEST_CORE)
    testImplementation(Deps.AndroidX.Work.TESTING)
    testImplementation(Deps.KotlinXTest.COROUTINES_TEST )
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

tasks.register("downloadExceptionRules", de.undercouch.gradle.tasks.download.Download::class) {
    val flavor = project.findProperty("flavor")?.toString()?.toLowerCase() ?: "abp"
    val baseDir = if (flavor == "abp") "src/main/assets" else "src/$flavor/assets"

    val source = when(flavor) {
        "abp" -> "https://0.samsung-internet.filter-list-downloads.eyeo.com/aa-variants/samsung_internet_browser-adblock_plus.txt"
        "adblock" -> "https://0.samsung-internet.filter-list-downloads.getadblock.com/aa-variants/samsung_internet_browser-adblock.txt"
        "crystal" -> "https://0.samsung-internet.filter-list-downloads.eyeo.com/aa-variants/samsung_internet_browser-crystal.txt"
        else -> throw GradleException("Given flavor <$flavor> not supported")
    }

    download.run {
        src(source)
        dest("$baseDir/exceptionrules.txt")
    }
}

tasks.register("downloadEasyList", de.undercouch.gradle.tasks.download.Download::class) {
    val flavor = project.findProperty("flavor")?.toString()?.toLowerCase() ?: "abp"
    val baseDir = if (flavor == "abp") "src/main/assets" else "src/$flavor/assets"

    download.run {
        src("https://0.samsung-internet.filter-list-downloads.getadblock.com/easylist.txt")
        dest("$baseDir/easylist.txt")
    }
}

tasks.register("createAssetsDir") {
    val flavor = project.findProperty("flavor")?.toString()?.toLowerCase() ?: "abp"
    val baseDir = if (flavor == "abp") "src/main/assets" else "src/$flavor/assets"
    // Create assets folder if doesn't exist
    project.mkdir(baseDir)
    // Add files if don't exist or replace content to be empty
    File("core/$baseDir", "easylist.txt").writeText("")
    File("core/$baseDir", "exceptionrules.txt").writeText("")
}

tasks.register("checkSubscriptionsFiles") {
    val flavor = project.findProperty("flavor")?.toString()?.toLowerCase() ?: "abp"
    val baseDir = if (flavor == "abp") "core/src/main/assets" else "core/src/$flavor/assets"
    val easyListLength = File("$baseDir/easylist.txt").length()
    val exceptionRulesLength = File("$baseDir/exceptionrules.txt").length()

    println("$flavor EASYLIST SIZE: ${easyListLength / 1024} KB")
    println("$flavor EXCEPTIONRULES SIZE: ${exceptionRulesLength / 1024} KB")

    if (easyListLength == 0L || exceptionRulesLength == 0L) {
        throw GradleException("Something went wrong. At least one of the subscriptions files is empty!")
    }
}

/* To run this task a parameter `flavor` (abp, adblock or crystal) must be provided.
    gradle :core:downloadSubscriptions -Pflavor=adblock (abp if no flavor is provided)
 */
tasks.register("downloadSubscriptions") {
    dependsOn("createAssetsDir", "downloadEasyList", "downloadExceptionRules")
    tasks.getByName("downloadExceptionRules").mustRunAfter("createAssetsDir")
    tasks.getByName("downloadEasyList").mustRunAfter("downloadExceptionRules")
    doLast {
        tasks.getByName("checkSubscriptionsFiles").run{}
    }
}

tasks.withType(Test::class.java) {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
    finalizedBy(tasks.getByName("jacocoTestReport"))
}


tasks.register("jacocoTestReport", JacocoReport::class.java) {
    val coverageSourceDirs = listOf("src/main/kotlin")
    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*"
    )

    val javaClasses = fileTree("$buildDir/intermediates/javac/worldAbpDebug/classes").setExcludes(fileFilter)
    val kotlinClasses = fileTree("$buildDir/tmp/kotlin-classes/worldAbpDebug").setExcludes(fileFilter)
    classDirectories.setFrom(files(javaClasses, kotlinClasses))
    additionalSourceDirs.setFrom(files(coverageSourceDirs))
    sourceDirectories.setFrom(files(coverageSourceDirs))
    executionData.setFrom(
        fileTree("$buildDir").setIncludes(listOf("jacoco/testWorldAbpDebugUnitTest.exec"))
    )

    reports {
        html.required.set(true)
    }
}