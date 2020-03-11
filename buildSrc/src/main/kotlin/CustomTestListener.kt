package com.zegreatrob.coupling.build

import com.soywiz.klock.DateTime
import mu.KotlinLogging
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestResult

class CustomTestListener : org.gradle.api.tasks.testing.TestListener {

    private val logger by lazy { KotlinLogging.logger("testListener") }
    private var lastStart: DateTime? = null

    override fun beforeTest(testDescriptor: TestDescriptor) = logger.info {
            mapOf("type" to "TestStart", "test" to testDescriptor.displayName)
        }
        .also { lastStart = DateTime.now() }

    override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
        val duration = lastStart?.let { DateTime.now() - it }
        logger.info {
                mapOf(
                    "type" to "TestEnd",
                    "test" to testDescriptor.displayName,
                    "status" to result.resultType,
                    "duration" to "$duration",
                    "failures" to result.exceptions
                        .joinToString("\n", "\n") { "message: ${it.message} \nstack: ${it.stackTrace}" }
                )
            }
            .also { lastStart = null }
    }

    override fun beforeSuite(suite: TestDescriptor?) {
    }

    override fun afterSuite(suite: TestDescriptor?, result: TestResult?) {
    }

}