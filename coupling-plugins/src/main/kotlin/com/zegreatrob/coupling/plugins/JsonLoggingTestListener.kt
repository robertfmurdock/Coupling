package com.zegreatrob.coupling.plugins

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.TextNode
import korlibs.time.TimeSpan
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.message.ObjectMessage
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestOutputEvent
import org.gradle.api.tasks.testing.TestOutputListener
import org.gradle.api.tasks.testing.TestResult

class JsonLoggingTestListener(private val taskName: String, val testRunIdentifier: String) :
    TestListener,
    TestOutputListener {

    companion object {
        val logger: Logger = LogManager.getLogger("test")
        val mapper = ObjectMapper()
    }

    override fun beforeTest(testDescriptor: TestDescriptor) = logger.info {
        testInfo(testDescriptor)
            .plus("type" to "TestStart")
            .asMessage()
    }

    private fun testInfo(testDescriptor: TestDescriptor) = mapOf(
        "taskName" to taskName,
        "testParent" to testDescriptor.parent?.name,
        "testName" to testDescriptor.name,
    )

    private fun Map<String, String?>.asMessage() = ObjectMessage(this)

    override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
        val durationLong = TimeSpan((result.endTime - result.startTime).toDouble())
        logger.info {
            testInfo(testDescriptor)
                .plus(afterTestInfo(result, durationLong))
                .asMessage()
        }
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

    override fun onOutput(testDescriptor: TestDescriptor?, outputEvent: TestOutputEvent?) {
        if (outputEvent != null) {
            try {
                val tree = mapper.readTree(outputEvent.message)
                val level = Level.getLevel(tree["level"].level())
                logger.log(level) {
                    ObjectMessage(
                        mapper.createObjectNode().apply {
                            set<JsonNode>("type", TextNode("forward"))
                            set<JsonNode>("taskName", TextNode(taskName))
                            set<JsonNode>("testParent", TextNode(testDescriptor?.parent?.name ?: ""))
                            set<JsonNode>("testName", TextNode(testDescriptor?.name ?: ""))
                            set<JsonNode>("originalLogger", tree["name"])
                            set<JsonNode>("originalMessage", tree["message"])
                        }
                    )
                }
            } catch (problem: JsonParseException) {
                logger.info {
                    ObjectMessage(
                        mapper.createObjectNode().apply {
                            set<JsonNode>("type", TextNode("forward"))
                            set<JsonNode>("taskName", TextNode(taskName))
                            set<JsonNode>("testParent", TextNode(testDescriptor?.parent?.name ?: ""))
                            set<JsonNode>("testName", TextNode(testDescriptor?.name ?: ""))
                            set<JsonNode>("originalMessage", TextNode(outputEvent.message))
                        }
                    )
                }
            }
        }
    }

    private fun JsonNode?.level(): String? {
        return if (this?.isTextual == true)
            textValue()
        else
            "Info"
    }
}
