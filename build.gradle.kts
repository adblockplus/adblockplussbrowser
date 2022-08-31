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
        classpath(Deps.GRADLE_PLUGIN)
        classpath(Deps.GRADLE_VERSIONS_PLUGIN)
        classpath(Deps.AndroidX.Navigation.SAFE_ARGS_GRADLE_PLUGIN)
        classpath(Deps.Hilt.ANDROID_GRADLE_PLUGIN)
        classpath(Deps.Kotlin.KOTLIN_PLUGIN)
        classpath(Deps.Protobuf.GRADLE_PLUGIN)
        classpath(Deps.Gms.OSS_LICENSES_PLUGIN)
        classpath(Deps.Firebase.GOOGLE_SERVICES)
        classpath(Deps.Firebase.CRASHLYTICS_GRADLE)
    }
}

plugins {
    id(Deps.GRADLE_VERSIONS_PLUGIN_ID).version(Deps.GRADLE_VERSIONS_PLUGIN_VERSION)
    id(Deps.DETEKT_PLUGIN_ID).version(Deps.DETEKT_PLUGIN_VERSION)
    id(Deps.KOVER_PLUGIN_ID).version(Deps.KOVER_PLUGIN_VERSION)
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
    apply(plugin = Deps.KOVER_PLUGIN_ID)
}

kover {
    koverMerged {
        enable() // create Kover merged reports

        filters { // common filters for all default Kover merged tasks
           classes {
                excludes += listOf("**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
                    "**/*Test*.*", "android/**/*.*", "**/_*.class")
            }
            projects {
                /* Specifies the projects excluded from the merged tasks. If tests are added to any
                of the projects listed below, it should be removed from this list */
                excludes += listOf(":app", ":analytics", ":i18n", ":onboarding", ":settings")
            }
        }

        htmlReport {
            // change report directory
            reportDir.set(layout.buildDirectory.dir("reports/coverage/html"))
        }

        xmlReport {
            // change report file name
            reportFile.set(layout.buildDirectory.file("reports/coverage/xml/coverage_report.xml"))
        }

        verify {
            rule {
                name = "Minimal line coverage rate in percents"
                bound {
                    minValue = 10
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

