package com.zegreatrob.coupling.plugins

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.TextNode
import com.soywiz.klock.TimeSpan
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.message.ObjectMessage
import org.gradle.api.tasks.testing.*


class JsonLoggingTestListener(private val taskName: String) : TestListener, TestOutputListener {

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
        "testName" to testDescriptor.name
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
        if(outputEvent != null) {
            val tree = mapper.readTree(outputEvent.message)
            val level = Level.getLevel(tree["level"].textValue())
            logger.log(level) {
                ObjectMessage(mapper.createObjectNode().apply {
                    set<JsonNode>("type", TextNode("forward"))
                    set<JsonNode>("taskName", TextNode(taskName))
                    set<JsonNode>("testParent", TextNode(testDescriptor?.parent?.name ?: ""))
                    set<JsonNode>("testName", TextNode(testDescriptor?.name ?: ""))
                    set<JsonNode>("originalLogger", tree["name"])
                    set<JsonNode>("originalMessage", tree["message"])
                }) }
        }
    }

}

