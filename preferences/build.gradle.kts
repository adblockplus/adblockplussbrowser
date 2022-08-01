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
    id(Deps.JACOCO)
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
    implementation(Deps.OkHttp.OKHTTP)
    implementation(Deps.OkHttp.COROUTINES)
    implementation(Deps.KotlinX.COROUTINES)

    testImplementation(Deps.JUNIT)
    testImplementation(Deps.ROBOLECTRIC)
    testImplementation(Deps.Mockito.Core)
    testImplementation(Deps.Mockito.Kotlin)
    testImplementation(Deps.KotlinXTest.COROUTINES_TEST )
    testAnnotationProcessor(Deps.Hilt.ANDROID_COMPILER)
    testImplementation(Deps.OkHttp.MOCK_WEB_SERVER)
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
        xml.required.set(true)
    }
}