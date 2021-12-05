package com.zegreatrob.coupling.plugins

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.message.ObjectMessage
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult


class JsonLoggingTestListener(private val taskName: String) : TestListener {

    companion object {
        val logger: Logger = LogManager.getLogger("test")
    }

    private var lastStart: DateTime? = null

    override fun beforeTest(testDescriptor: TestDescriptor) = logger.info {
        testInfo(testDescriptor)
            .plus("type" to "TestStart")
            .asMessage()
    }
        .also { lastStart = DateTime.now() }

    private fun testInfo(testDescriptor: TestDescriptor) = mapOf(
        "taskName" to taskName,
        "testParent" to testDescriptor.parent?.name,
        "testName" to testDescriptor.name
    )

    private fun Map<String, String?>.asMessage() = ObjectMessage(this)

    override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
        val duration = lastStart?.let { DateTime.now() - it }
        logger.info {
            testInfo(testDescriptor)
                .plus(afterTestInfo(result, duration))
                .asMessage()
        }
            .also { lastStart = null }
    }

    private fun afterTestInfo(result: TestResult, duration: TimeSpan?) = mapOf(
        "type" to "TestEnd",
        "status" to "${result.resultType}",
        "duration" to "$duration",
        "failures" to result.exceptions
            .joinToString("\n", "\n") { "message: ${it.message} \nstack: ${it.stackTrace}" }
    )

    override fun beforeSuite(suite: TestDescriptor?) {
    }

    override fun afterSuite(suite: TestDescriptor?, result: TestResult?) {
    }

}

