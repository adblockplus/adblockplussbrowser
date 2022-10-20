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

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath(libs.android.plugin.gradle)
        classpath(libs.plugin.versions)
        classpath(libs.androidx.navigation.safeargs.plugin)
        classpath(libs.hilt.gradle.plugin)
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.protobuf.gradle.plugin)
        classpath(libs.gms.oss.licenses.plugin)
        classpath(libs.gms.google.services)
        classpath(libs.firebase.crashlytics.gradle)
        classpath(libs.jacoco.core)
    }
}

plugins {
    `jacoco`
    alias(libs.plugins.detekt)
    alias(libs.plugins.plugin.versions)
}

val coverageProjectsPath = setOf(":base", ":core", ":preferences", ":app")
val testTaskName = "testWorldAbpDebugUnitTest"

allprojects {
    repositories {
        google()
        mavenCentral()
    }

    if (path in coverageProjectsPath) {
        apply(plugin = "jacoco")

        tasks.withType(Test::class.java) {
            // We want coverage only for the worldAbp flavor
            if (name != testTaskName)
                return@withType

            configure<JacocoTaskExtension> {
                isIncludeNoLocationClasses = true
                excludes = listOf("jdk.internal.*")
            }
        }
    }
}

afterEvaluate {
    val coverageProjects = subprojects.filter { it.path in coverageProjectsPath}
    val executionDataPaths = coverageProjects.map {
        it.buildDir.toPath().resolve("jacoco/$testTaskName.exec").toString()
    }
    val sourcePaths = coverageProjects.map {
        it.projectDir.toPath().resolve("src/main/kotlin").toFile()
    }
    val classPaths = coverageProjects.map {
        it.buildDir.toPath().resolve("tmp/kotlin-classes/worldAbpDebug").toFile()
    }
    val deps = coverageProjectsPath.map { "$it:$testTaskName" }

    tasks.register<JacocoReport>("jacocoTestReport") {
        executionData(executionDataPaths)
        sourceDirectories.setFrom(sourcePaths)
        classDirectories.setFrom(classPaths)
        dependsOn(deps)

        reports {
            xml.required.set(true)
            html.required.set(true)
        }

        dependsOn(deps)
    }

    tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
        executionData(executionDataPaths)
        sourceDirectories.setFrom(sourcePaths)
        classDirectories.setFrom(classPaths)

        violationRules {
            rule {
                limit {
                    minimum = 0.39f.toBigDecimal()
                }
            }
        }

        dependsOn(deps)
    }
}

detekt {
    source = files(fileTree(".") {
        // On CI, we set GRADLE_USER_HOME to .gradle, this makes detekt fail
        exclude(".gradle")
    })
    parallel = true
    buildUponDefaultConfig = true
    config = files("${projectDir}/config/detekt/detekt.yml")
    baseline = file("${projectDir}/config/detekt/detekt-baseline.xml")

    reports {
        html.enabled = true
        xml.enabled = true
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates").configure {
    // Example 1: reject all non stable versions
    rejectVersionIf {
        isNonStable(candidate.version)
    }

    // Example 2: disallow release candidates as upgradable versions from stable versions
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }

    // Example 3: using the full syntax
    resolutionStrategy {
        componentSelection {
            all {
                if (isNonStable(candidate.version) && !isNonStable(currentVersion)) {
                    reject("Release candidate")
                }
            }
        }
    }
}

