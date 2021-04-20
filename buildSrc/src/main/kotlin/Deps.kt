object Deps {
    val GRADLE_PLUGIN = "com.android.tools.build:gradle" version "4.1.3"

    const val GRADLE_VERSIONS_PLUGIN_ID = "com.github.ben-manes.versions"
    const val GRADLE_VERSIONS_PLUGIN_VERSION = "0.38.0"
    val GRADLE_VERSIONS_PLUGIN = "com.github.ben-manes:gradle-versions-plugin" version GRADLE_VERSIONS_PLUGIN_VERSION

    val JUNIT = "junit:junit" version "4.13.2"

    val MATERIAL = "com.google.android.material:material" version "1.3.0"

    val OKIO = "com.squareup.okio:okio" version "3.0.0-alpha.2"

    val TIMBER = "com.jakewharton.timber:timber" version "4.7.1"

    object ANDROIDX : DependencyGroup("androidx") {
        val APPCOMPAT = dependency {
            groupName("appcompat:appcompat")
            version("1.2.0")
        }

        val CORE = dependency {
            groupName("core:core-ktx")
            version("1.3.2")
        }

        val CONSTRAINT_LAYOUT = dependency {
            groupName("constraintlayout:constraintlayout")
            version("2.0.4")
        }

        object TEST : DependencyGroup("androidx.test") {
            val JUNIT = dependency {
                groupName("test.ext:junit")
                version("1.1.2")
            }

            object ESPRESSO : DependencyGroup("androidx.test.espresso", "3.3.0") {
                val CORE = dependency { name("espresso-core") }
            }
        }

        object WORK : DependencyGroup("androidx.work", "2.5.0") {
            val RUNTIME = dependency { name("work-runtime-ktx") }
        }
    }

    object KOTLIN : DependencyGroup("org.jetbrains.kotlin", "1.4.32") {
        val KOTLIN_PLUGIN = dependency { name("kotlin-gradle-plugin") }
        val KOTLIN_STDLIB = dependency { name("kotlin-stdlib") }
    }

    object KOTLINX : DependencyGroup("org.jetbrains.kotlinx", "1.4.3") {
        val COROUTINES = dependency { name("kotlinx-coroutines-core") }
        val COROUTINES_ANDROID = dependency { name("kotlinx-coroutines-android") }
    }

    object OKHTTP : DependencyGroup("com.squareup.okhttp3", "4.9.1") {
        val OKHTTP = dependency { name("okhttp") }
        val LOGGER = dependency { name("logging-interceptor") }

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