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

object Deps {
    const val ROBOLECTRIC = "org.robolectric:robolectric:4.7.3"

    const val DETEKT_PLUGIN_ID = "io.gitlab.arturbosch.detekt"
    const val DETEKT_PLUGIN_VERSION = "1.18.1"

    val GRADLE_PLUGIN = "com.android.tools.build:gradle" version "7.0.4"

    const val GRADLE_VERSIONS_PLUGIN_ID = "com.github.ben-manes.versions"
    const val GRADLE_VERSIONS_PLUGIN_VERSION = "0.39.0"
    val GRADLE_VERSIONS_PLUGIN = "com.github.ben-manes:gradle-versions-plugin" version GRADLE_VERSIONS_PLUGIN_VERSION

    val JUNIT = "junit:junit" version "4.13.2"

    val JACOBO_PLUGIN = "gradle.plugin.com.kageiit:jacobo-plugin" version "2.0.5"

    val JACOBO = "com.kageiit.jacobo"

    val JACOCO_CORE = "org.jacoco:org.jacoco.core" version "0.8.7"

    const val JACOCO = "jacoco"

    val DOTS_INDICATOR = "com.tbuonomo:dotsindicator" version "4.2"

    val MATERIAL = "com.google.android.material:material" version "1.3.0"

    val OKIO = "com.squareup.okio:okio" version "3.0.0-alpha.6"

    val TIMBER = "com.jakewharton.timber:timber" version "4.7.1"

    val SPEED_DIAL = "com.leinardi.android:speed-dial" version "3.2.0"

    object Gms: DependencyGroup("com.google.android") {
        val OSS_LICENSES_PLUGIN = dependency {
            groupName("gms:oss-licenses-plugin")
            version("0.10.4")
        }

        val OSS_LICENSES = dependency {
            groupName("gms:play-services-oss-licenses")
            version("17.0.0")
        }
    }

    object Firebase: DependencyGroup("com.google") {
        val BOM = dependency {
            groupName("firebase:firebase-bom")
            version("28.3.0")
        }

        val ANALYTICS = dependency {
            groupName("firebase:firebase-analytics-ktx")
        }

        val CRASHLYTICS_GRADLE = dependency {
            groupName("firebase:firebase-crashlytics-gradle")
            version("2.7.1")
        }

        val CRASHLYTICS = dependency {
            groupName("firebase:firebase-crashlytics-ktx")
            version("18.2.1")
        }

        val GOOGLE_SERVICES = dependency {
            groupName("gms:google-services")
            version("4.3.8")
        }
    }

    object AndroidX : DependencyGroup("androidx") {
        val ACTIVITY = dependency {
            groupName("activity:activity-ktx")
            version("1.2.3")
        }

        val APPCOMPAT = dependency {
            groupName("appcompat:appcompat")
            version("1.3.0")
        }

        val CORE = dependency {
            groupName("core:core-ktx")
            version("1.5.0")
        }

        val CONSTRAINT_LAYOUT = dependency {
            groupName("constraintlayout:constraintlayout")
            version("2.0.4")
        }

        val FRAGMENT = dependency {
            groupName("fragment:fragment-ktx")
            version("1.3.4")
        }

        val PREFERENCE = dependency {
            groupName("preference:preference-ktx")
            version("1.1.1")
        }

        val TEST_CORE = dependency {
            groupName("test:core")
            version("1.4.0")
        }

        val VIEWPAGER2 = dependency {
            groupName("viewpager2:viewpager2")
            version("1.0.0")
        }

        object DataStore: DependencyGroup("androidx.datastore", "1.0.0-beta01") {
            val DATASTORE = dependency { name("datastore") }
            val PREFERENCES = dependency { name("datastore-preferences") }
        }

        object Hilt : DependencyGroup("androidx.hilt", "1.0.0") {
            val COMPILER = dependency { name("hilt-compiler") }
            val COMMON = dependency { name("hilt-common") }
            val WORK = dependency { name("hilt-work") }
        }

        object Lifecycle: DependencyGroup("androidx.lifecycle", "2.3.1") {
            val LIVEDATA = dependency { name("lifecycle-livedata-ktx") }
            val VIEWMODEL = dependency { name("lifecycle-viewmodel-ktx") }
        }

        object Navigation: DependencyGroup("androidx.navigation", "2.3.5") {
            val FRAGMENT = dependency { name("navigation-fragment-ktx") }
            val SAFE_ARGS_GRADLE_PLUGIN = dependency { name("navigation-safe-args-gradle-plugin") }
            val UI = dependency { name("navigation-ui-ktx") }
        }

        object Test : DependencyGroup("androidx.test") {
            val JUNIT = dependency {
                groupName("ext:junit")
                version("1.1.2")
            }

            object Espresso : DependencyGroup("androidx.test.espresso", "3.3.0") {
                val CORE = dependency { name("espresso-core") }
            }
        }

        object Work : DependencyGroup("androidx.work", "2.5.0") {
            val RUNTIME = dependency { name("work-runtime-ktx") }
            val TESTING = dependency { name("work-testing") }
        }
    }

    object Apache: DependencyGroup() {
        val COMMONS_VALIDATOR = "commons-validator:commons-validator" version "1.7"
    }

    object Hilt : DependencyGroup("com.google.dagger", "2.39.1") {
        val ANDROID = dependency { name("hilt-android") }
        val ANDROID_COMPILER = dependency { name("hilt-android-compiler") }
        val ANDROID_GRADLE_PLUGIN = dependency { name("hilt-android-gradle-plugin") }
        val ANDROID_TESTING = dependency { name("hilt-android-testing") }
    }

    object Kotlin : DependencyGroup("org.jetbrains.kotlin", "1.5.31") {
        val KOTLIN_PLUGIN = dependency { name("kotlin-gradle-plugin") }
        val KOTLIN_STDLIB = dependency { name("kotlin-stdlib") }
    }

    object KotlinX : DependencyGroup("org.jetbrains.kotlinx", "1.5.0") {
        val COROUTINES = dependency { name("kotlinx-coroutines-core") }
        val COROUTINES_ANDROID = dependency { name("kotlinx-coroutines-android") }
    }

    object KotlinXTest: DependencyGroup("org.jetbrains.kotlinx", "1.6.0") {
        val COROUTINES_TEST = dependency { name("kotlinx-coroutines-test") }
    }

    object MaterialDialogs : DependencyGroup("com.afollestad.material-dialogs", "3.3.0") {
        val CORE = dependency { name("core") }
        val INPUT = dependency { name("input") }
    }

    object OkHttp : DependencyGroup("com.squareup.okhttp3", "4.9.1") {
        val OKHTTP = dependency { name("okhttp") }
        val LOGGER = dependency { name("logging-interceptor") }
        val MOCK_WEB_SERVER = dependency { name("mockwebserver") }
        val COROUTINES = dependency {
            groupPrefix("ru.gildor.coroutines")
            name("kotlin-coroutines-okhttp")
            version("1.0")
        }
    }

    object Protobuf : DependencyGroup("com.google.protobuf", "3.17.2") {
        val GRADLE_PLUGIN = dependency {
            name("protobuf-gradle-plugin")
            version("0.8.16")
        }
        val JAVALITE = dependency { name("protobuf-javalite") }
        val PROTOC = dependency { name("protoc") }
    }

    object Android: DependencyGroup("com.android", "1.1") {
        val INSTALL_REFERRER = dependency { groupName("installreferrer:installreferrer") }
    }

    object Mockito : DependencyGroup("org.mockito", "4.0.0") {
        val Core = dependency { name("mockito-core") }
        val Kotlin = dependency { groupName("kotlin:mockito-kotlin") }
    }

    object XZ : DependencyGroup("org.tukaani", "1.9") {
        val XZ = dependency { name("xz") }
    }
}

abstract class DependencyGroup(
    private val groupPrefix: String = "",
    private val defaultVersion: String = ""
) {

    fun dependency(initializer: DependencyDsl.() -> Unit) : String {
        return DependencyDsl(groupPrefix, defaultVersion).apply(initializer).build()
    }
}

private infix fun String.version(version: String) = "$this:$version"

class DependencyDsl(private var groupPrefix: String, private var defaultVersion: String) {
    private var version: String? = null
    private var groupName: String? = null
    private var name: String? = null

    fun groupPrefix(groupPrefix: String) {
        this.groupPrefix = groupPrefix
    }

    fun version(version: String) {
        this.version = version
    }

    fun groupName(groupName: String) {
        this.groupName = groupName
    }

    fun name(name: String) {
        this.name = name
    }

    fun build(): String {
        val depGroupName =
            if (!groupName.isNullOrEmpty()) "$groupPrefix.$groupName" else "$groupPrefix:$name"
        val depVersion = if (!version.isNullOrEmpty()) version else defaultVersion
        return "$depGroupName:$depVersion"
    }
}
