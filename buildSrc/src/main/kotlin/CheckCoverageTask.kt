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

import groovy.util.Node
import groovy.util.NodeList
import groovy.xml.XmlParser
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import java.io.File
import kotlin.math.max

/**
 * Convenience extension method to quickly extract xml attributes from a [Node]
 *
 * @param attr the attribute name
 * @return A string, may be empty if the node has not such attribute
 */
private fun Any.getAttribute(attr: String): String = (this as Node).attribute(attr).toString()

// Xml features names
private object xmlParserFeatures {
    const val DISALLOW_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl"
    const val LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd"
}

// JaCoCo reports strings
private object jacocoReport {

    object tags {
        const val COUNTER = "counter"
    }

    object attrNames {
        const val TYPE = "type"
        const val COVERED = "covered"
        const val MISSED = "missed"
    }

    object attrValues {
        const val INSTRUCTION = "INSTRUCTION"
    }
}

/**
 * A task to check code coverage percentage. It reads JaCoCo report file (XML), extracts the *covered* and the *missed*
 * instructions, calculate *covered* over total and compare the latter to the given threshold: if the covered percentage
 * is lower thant the threshold, the task fails.
 *
 * Usage example:
 * ```
 * tasks.register("checkCoverage", CheckCoverageTask::class.java) {
 *     coverageFile = file("$buildDir/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
 *     threshold = 0.95F
 *     dependsOn("jacocoTestReport")
 * }
 * ```
 */
open class CheckCoverageTask: DefaultTask() {

    /**
     * The JaCoCo coverage report to parse
     */
    @InputFile
    public lateinit var coverageFile: File

    /**
     * The desired instructions coverage percentage expressed as a float between 0 and 1. Default 1
     */
    @Input
    public var threshold: Float = 1F

    init {
        doLast {
            if (!coverageFile.isFile) {
                error("$coverageFile is not a file")
            }
            if (threshold < 0F || threshold > 1F) {
                error("The threshold should be between 0 and 1")
            }

            val parser = XmlParser()
            parser.setFeature(xmlParserFeatures.DISALLOW_DOCTYPE_DECL, false)
            parser.setFeature(xmlParserFeatures.LOAD_EXTERNAL_DTD, false);
            val coverageXml = parser.parse(coverageFile)

            val counter = (coverageXml.get(jacocoReport.tags.COUNTER) as NodeList).find {
                (it as Node).attribute(jacocoReport.attrNames.TYPE).equals(jacocoReport.attrValues.INSTRUCTION)
            }
            if (counter == null) {
                error(  "Can't find a <${jacocoReport.tags.COUNTER}> tag with " +
                        "${jacocoReport.attrNames.TYPE} equal to \"${jacocoReport.attrValues.INSTRUCTION}\"")
            }

            val missed = counter.getAttribute(jacocoReport.attrNames.MISSED).toFloat()
            val covered = counter.getAttribute(jacocoReport.attrNames.COVERED).toFloat()
            val percentage = covered / max(1F, missed + covered)

            if (percentage < threshold) {
                error("Unsufficient test coverage: expected $threshold, calculated $percentage")
            }
        }
    }
}
