tasks.withType(Test::class.java) {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
    finalizedBy(tasks.getByName("jacocoTestReport"))
}

tasks.register("jacocoTestReport", JacocoReport::class.java) {
    val coverageSourceDirs = listOf("src/main/kotlin")
    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*"
    )

    val javaClasses = fileTree("$buildDir/intermediates/javac/worldAbpDebug/classes").setExcludes(fileFilter)
    val kotlinClasses = fileTree("$buildDir/tmp/kotlin-classes/worldAbpDebug").setExcludes(fileFilter)
    classDirectories.setFrom(files(javaClasses, kotlinClasses))
    additionalSourceDirs.setFrom(files(coverageSourceDirs))
    sourceDirectories.setFrom(files(coverageSourceDirs))
    executionData.setFrom(
        fileTree("$buildDir").setIncludes(listOf("jacoco/testWorldAbpDebugUnitTest.exec"))
    )

    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}