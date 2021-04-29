import com.google.protobuf.gradle.*

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("com.google.protobuf")
    id("dagger.hilt.android.plugin")
}

applyCommonConfig()

dependencies {
    implementation(project(":base"))

    implementation(Deps.AndroidX.APPCOMPAT)
    implementation(Deps.AndroidX.DATASTORE)
    implementation(Deps.Protobuf.JAVALITE)
    implementation(Deps.Hilt.ANDROID)
    kapt(Deps.Hilt.ANDROID_COMPILER)
    implementation(Deps.Moshi.KOTLIN)
    kapt(Deps.Moshi.KOTLIN_CODEGEN)
}

protobuf {
    protoc {
        artifact = Deps.Protobuf.PROTOC
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                id("java") {
                    option("lite")
                }
            }
        }
    }
}