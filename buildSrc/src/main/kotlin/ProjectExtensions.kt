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

import com.android.build.gradle.AppExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import java.util.Locale

// Flavor descriptor
internal data class Flavor(
    val name: String,
    val dimension: String,
    val applicationId: String? = null,
    val versionName: String? = null
)

// All the flavors (and dimensions) defined in this project
internal object Flavors {
    const val PRODUCT_DIMENSION = "product"
    const val REGION_DIMENSION = "region"

    val ABP = Flavor("abp", PRODUCT_DIMENSION, "org.adblockplus.adblockplussbrowser", Config.Versions.ABP)
    val ADBLOCK = Flavor("adblock", PRODUCT_DIMENSION, "com.betafish.adblocksbrowser", Config.Versions.ADBLOCK)
    val CRYSTAL = Flavor("crystal", PRODUCT_DIMENSION, "co.crystalapp.crystal", Config.Versions.CRYSTAL)
    val WORLD = Flavor("world", REGION_DIMENSION)

    val asList = listOf(WORLD, ABP, ADBLOCK, CRYSTAL)
}

fun Project.applyCommonConfig() {
    // Enable or disable shrinking resource dynamically depending if this is a app or a library
    // (libraries do not support resources shrinking)
    val shrinkResources = this.plugins.hasPlugin("com.android.application")

    android {
        compileSdkVersion(Config.COMPILE_SDK_VERSION)
        buildToolsVersion(Config.BUILD_TOOLS_VERSION)

        defaultConfig {
            minSdk = Config.MIN_SDK_VERSION
            targetSdk = Config.TARGET_SDK_VERSION
            testInstrumentationRunner = Config.ANDROID_TEST_INSTRUMENTATION_RUNNER
            vectorDrawables.useSupportLibrary = true
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }

        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
        }

        sourceSets.all {
            java.srcDir("src/$name/kotlin")
        }

        buildTypes {
            getByName("release") {
                isMinifyEnabled = true
                isShrinkResources = shrinkResources
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }

            getByName("debug") {
                isMinifyEnabled = false
            }
        }
    }

    dependencies {
        implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

        testImplementation(Deps.JUNIT)

        androidTestImplementation(Deps.AndroidX.Test.JUNIT)
        androidTestImplementation(Deps.AndroidX.Test.Espresso.CORE)
    }
}

/**
 * Create all the flavors for the project that invokes this method
 */
fun Project.createFlavorsConfig() {
    android {
        // Check is the project is an App (not a Library)
        val isApp = this is AppExtension
        flavorDimensions(Flavors.REGION_DIMENSION, Flavors.PRODUCT_DIMENSION)
        productFlavors {
            // Iterate over all the flavors
            Flavors.asList.forEach { flavor ->
                create(flavor.name) {
                    dimension = flavor.dimension
                    // Only apps can define the applicationId and the versionName
                    if (isApp) {
                        // Region flavors do not define applicationId or versionName (they are null)
                        flavor.applicationId?.let { applicationId = it }
                        flavor.versionName?.let { versionName = it }
                    }
                }

                // Just add the FLAVOR_{name} constants to the BuildConfig to keep the names aligned
                defaultConfig.buildConfigField(
                    "String",
                    "FLAVOR_${flavor.name.toUpperCase(Locale.ROOT)}",
                    "\"${flavor.name}\""
                )
                defaultConfig.buildConfigField(
                    "String",
                    "APPLICATION_VERSION",
                    "\"${flavor.versionName}\""
                )
            }
        }
    }
}

/**
 * Calculate the versionCode based on time with the ability to tweak it via the APB4SI_DAY_VERSION
 * environment variable. The latter can vary between 0 and 7 (included) making it possible to
 * release up to 8 different versions per day.
 *
 * We start by calculating how many days have passed since 2021-10-05, then we multiply this number
 * by 8 (we shift left 3 bits), we add the offset (latest version published before this method of
 * calculating the versionCode was in place) and the ABP4SI_DAY_VERSION value. This is the new
 * versionCode.
 */
@Suppress("MagicNumber")
fun versionCode(): Int {
    // Let's fix how may versions we can publish per day: by using 3 bits, we can do 8 releases per day
    val dayVersion = System.getenv("ABP4SI_DAY_VERSION").let {
        try {
            minOf(7, maxOf(0, it.toInt()))
        } catch (_: Exception) {
            0
        }
    }
    // Days since 2021-10-05
    val days = ((System.currentTimeMillis() - Constants.DAY_0) / Constants.ONE_DAY_IN_MS).toInt()
    return Constants.VERSION_OFFSET + (days shl 3) + dayVersion
}

internal object Constants {
    // Next version when automatic calculation started
    const val VERSION_OFFSET = 20

    // 2021-10-05 as milliseconds since Unix Epoch, the day from which we started counting versions
    const val DAY_0 = 1_633_384_800_000L

    const val ONE_DAY_IN_MS = 86_400_000L
}
