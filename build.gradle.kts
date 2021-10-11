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
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

detekt {
    source = files(projectDir)
    parallel = true
    baseline = file("${projectDir}/detekt-baseline.xml")

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
