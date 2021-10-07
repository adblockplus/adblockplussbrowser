import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

fun Project.applyCommonConfig() {
    android {
        compileSdkVersion(Config.COMPILE_SDK_VERSION)
        buildToolsVersion(Config.BUILD_TOOLS_VERSION)

        defaultConfig {
            minSdkVersion(Config.MIN_SDK_VERSION)
            targetSdkVersion(Config.TARGET_SDK_VERSION)
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
    }
    dependencies {
        implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

        testImplementation(Deps.JUNIT)

        androidTestImplementation(Deps.AndroidX.Test.JUNIT)
        androidTestImplementation(Deps.AndroidX.Test.Espresso.CORE)
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
fun versionCode(): Int {
    // Let's fix how may versions we can publish per day: by using 3 bits, we can do 8 releases per day
    val dayVersion = System.getenv("ABP4SI_DAY_VERSION").let {
        try {
            minOf(7, maxOf(0, it.toInt()))
        } catch (_: Exception) {
            0
        }
    }
    val offset = 20 // Next version when automatic calculation started
    val startDate = 1_633_384_800_000L // 2021-10-05 as milliseconds since Unix Epoch
    val oneDayInMillis = 86_400_000L
    val days = ((System.currentTimeMillis() - startDate) / oneDayInMillis).toInt() // Days since 2021-10-05
    return offset + (days shl 3) + dayVersion
}
