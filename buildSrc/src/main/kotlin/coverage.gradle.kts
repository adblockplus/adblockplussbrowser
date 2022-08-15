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

plugins {
    id("jacoco")
}

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

tasks.register("checkCoverage", CheckCoverageTask::class.java) {
    coverageFile = file("$buildDir/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
    threshold = 0.4F
    dependsOn("jacocoTestReport")
}
