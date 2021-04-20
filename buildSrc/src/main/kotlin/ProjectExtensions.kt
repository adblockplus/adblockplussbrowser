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

        androidTestImplementation(Deps.ANDROIDX.TEST.JUNIT)
        androidTestImplementation(Deps.ANDROIDX.TEST.ESPRESSO.CORE)
    }
}