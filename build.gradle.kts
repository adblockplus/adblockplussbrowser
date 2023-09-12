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

// Referencing `libs` raises "LibrariesForLibs'
// can't be called in this context by implicit receiver."
// TODO needs Gradle version update
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    jacoco
    alias(libs.plugins.detekt)
    alias(libs.plugins.plugin.versions)
    alias(libs.plugins.jacoco.test.aggregation.coverage)
}

val coverageProjectsPath = setOf(":base", ":core", ":preferences", ":app", ":telemetry")

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

// see https://github.com/gmazzo/gradle-android-test-aggregation-plugin/
testAggregation {
    val coverageProjects = subprojects.filter { it.path in coverageProjectsPath}
    modules {
        include(coverageProjects)
        exclude(rootProject)
    }
}

afterEvaluate {
    // TODO this code doesn't work because `JacocoCoverageVerification` has to know
    // executionData, sourceDirectories and classDirectories before the task is executed
    // and we have to somehow get it from
    tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
        // this is sort of an example, test aggregation does not add test report tasks to
        // individual subprojects
        subprojects.forEach{ project ->
            project.tasks.named<JacocoReport>("jacocoTestReport").map {
                executionData(it.executionData)
                sourceDirectories.setFrom(it.sourceDirectories)
                classDirectories.setFrom(it.classDirectories)
            }
        }

        violationRules {
            rule {
                limit {
                    minimum = 0.40F.toBigDecimal()
                }
            }
        }
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

