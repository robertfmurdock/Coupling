package com.zegreatrob.coupling.plugins

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.message.ObjectMessage
import org.apache.tools.ant.util.TeeOutputStream
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import java.io.OutputStream
import java.util.*

val nodeLogger: Logger = LogManager.getLogger("node-exec")

val objectMapper = ObjectMapper()

afterEvaluate {
    val testRunIdentifier: String = getTestRunIdentifier()

    System.setProperty("testRunIdentifier", testRunIdentifier)

    tasks.withType(KotlinJsTest::class).configureEach {
        val jsonLoggingListener = JsonLoggingTestListener(path, testRunIdentifier)
        addTestListener(jsonLoggingListener)
        addTestOutputListener(jsonLoggingListener)
    }

    tasks.withType(NodeJsExec::class).configureEach {
        val processLine = { line: String ->
            val json = findJsonNode(line)
            val level = json["level"]?.textValue()?.let { Level.getLevel(it) } ?: Level.TRACE
            json.remove("level")
            nodeLogger.log(level) { ObjectMessage(json) }
        }

        val alt = TransformingOutputStream(processLine)

        val existingOutput: OutputStream? = standardOutput

        standardOutput = if (existingOutput != null) {
            TeeOutputStream(existingOutput, alt)
        } else {
            alt
        }
    }
}

fun Project.getTestRunIdentifier(): String {
    var testRunIdentifier: Any? by rootProject.extra
    return if (testRunIdentifier != null)
        "$testRunIdentifier"
    else {
        UUID.randomUUID().toString().also {
            testRunIdentifier = it
        }
    }
}

fun asJsonOrNull(line: String): ObjectNode? {
    val json = try {
        objectMapper.readTree(line)
    } catch (nope: JsonParseException) {
        null
    }
    if (json?.isObject == true) {
        return json as ObjectNode?
    }
    return null
}

fun findJsonNode(line: String): ObjectNode {
    val json = asJsonOrNull(line)

    if (json != null) {
        return json
    }

    val openJsonIndex = line.indexOf("{")
    val anotherAttempt = if (openJsonIndex != -1) line.substring(openJsonIndex) else null
    val foundJson = anotherAttempt?.let { asJsonOrNull(it) }

    return if (foundJson != null) {
        val remainingMessage = line.substring(0, openJsonIndex)
        if (remainingMessage.isBlank()) {
            foundJson
        } else {
            foundJson.set("prefix", TextNode(remainingMessage.trim()))
        }
    } else {
        objectMapper.createObjectNode().set("message", TextNode(line))
    }
}