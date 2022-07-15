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
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

open class FileWrappedNodeJsExec
@Inject
constructor(
    @Internal
    override val compilation: KotlinJsCompilation,
) : NodeJsExec(compilation) {
    @OutputFile
    lateinit var outputFile: File

    override fun exec() {
        val processLine = { line: String ->
            val json = findJsonNode(line)
            val level = json["level"]?.textValue()?.let { Level.getLevel(it) } ?: Level.TRACE
            json.remove("level")
            val nodeLogger: Logger = LogManager.getLogger("node-exec")

            nodeLogger.log(level) { ObjectMessage(json) }
        }

        val alt = TransformingOutputStream(processLine)

        val allOutput = ByteArrayOutputStream()
        standardOutput = TeeOutputStream(allOutput, alt)
        super.exec()
        outputFile.writeText(allOutput.toString())
    }
}

val objectMapper = ObjectMapper()

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
