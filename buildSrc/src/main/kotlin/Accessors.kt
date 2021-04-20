import com.android.build.gradle.TestedExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

private const val ANDROID = "android"
private const val KOTLIN_OPTIONS = "kotlinOptions"
private const val IMPLEMENTATION = "implementation"
private const val TEST_IMPLEMENTATION = "testImplementation"
private const val ANDROID_TEST_IMPLEMENTATION = "androidTestImplementation"

internal fun Project.android(configure: TestedExtension.() -> Unit) {
    this.configureExtension(ANDROID, configure)
}

internal fun TestedExtension.kotlinOptions(configure: KotlinJvmOptions.() -> Unit) {
    this.configureExtension(KOTLIN_OPTIONS, configure)
}

internal fun DependencyHandler.implementation(dependencyNotation: Any): Dependency? =
    add(IMPLEMENTATION, dependencyNotation)

internal fun DependencyHandler.testImplementation(dependencyNotation: Any): Dependency? =
    add(TEST_IMPLEMENTATION, dependencyNotation)

internal fun DependencyHandler.androidTestImplementation(dependencyNotation: Any): Dependency? =
    add(ANDROID_TEST_IMPLEMENTATION, dependencyNotation)

private fun <T> Any.configureExtension(name: String, action: Action<T>) {
    (this as ExtensionAware).extensions.configure(name, action)
}