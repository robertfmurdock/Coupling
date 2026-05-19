package com.zegreatrob.coupling.plugins.cleanup

import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class WeeklyCleanupRenderLogTask : DefaultTask() {
    @get:Internal
    abstract val jsonlFilePath: Property<String>

    @get:Internal
    abstract val outputFilePath: Property<String>

    @Suppress("UNCHECKED_CAST")
    @TaskAction
    fun render() {
        val jsonlFile = File(jsonlFilePath.get())
        if (!jsonlFile.exists()) {
            logger.lifecycle("No agent stream JSONL at ${jsonlFile.absolutePath}; skipping render.")
            return
        }
        val slurper = JsonSlurper()
        val sb = StringBuilder()
        var turnIndex = 0

        jsonlFile.forEachLine { line ->
            if (line.isBlank()) return@forEachLine
            val event = try {
                slurper.parseText(line) as? Map<*, *>
            } catch (e: Exception) {
                logger.warn("Skipping unparseable line: ${line.take(80)}")
                null
            } ?: return@forEachLine

            when (event["type"] as? String) {
                "assistant" -> {
                    turnIndex++
                    sb.appendLine("## Turn $turnIndex")
                    sb.appendLine()
                    val message = event["message"] as? Map<*, *> ?: return@forEachLine
                    val content = message["content"] as? List<*> ?: return@forEachLine
                    val blocks = content.filterIsInstance<Map<*, *>>()
                    val textBlocks = blocks.filter { it["type"] == "text" }
                    val toolUses = blocks.filter { it["type"] == "tool_use" }
                    for (block in textBlocks) {
                        val text = (block["text"] as? String)?.trim() ?: continue
                        if (text.isNotBlank()) {
                            sb.appendLine(text)
                            sb.appendLine()
                        }
                    }
                    for (tool in toolUses) {
                        val name = tool["name"] as? String ?: "?"
                        val input = tool["input"] as? Map<*, *> ?: emptyMap<String, Any?>()
                        sb.appendLine("- $name: ${summarizeTool(name, input)}")
                    }
                    if (toolUses.isNotEmpty()) sb.appendLine()
                }

                "result" -> {
                    sb.appendLine("## Result")
                    sb.appendLine()
                    val subtype = event["subtype"] as? String ?: "unknown"
                    val error = event["error"] as? String
                    val errorPart = if (!error.isNullOrBlank()) " — $error" else ""
                    sb.appendLine("Outcome: **$subtype**$errorPart")
                }
            }
        }

        val out = File(outputFilePath.get())
        out.parentFile.mkdirs()
        out.writeText(sb.toString())
        logger.lifecycle("Wrote agent log: ${out.absolutePath} ($turnIndex turns)")
    }

    private fun summarizeTool(name: String, input: Map<*, *>): String = when (name) {
        "Bash" -> truncate(input["command"] as? String ?: "", 120)

        "Read" -> input["file_path"] as? String ?: ""

        "Edit" -> input["file_path"] as? String ?: ""

        "Write" -> input["file_path"] as? String ?: ""

        "Glob" -> buildString {
            append(input["pattern"] as? String ?: "")
            val path = input["path"] as? String
            if (!path.isNullOrBlank()) append(" in $path")
        }

        "Grep" -> buildString {
            append(input["pattern"] as? String ?: "")
            val path = input["path"] as? String
            if (!path.isNullOrBlank()) append(" in $path")
            val glob = input["glob"] as? String
            if (!glob.isNullOrBlank()) append(" ($glob)")
        }

        "TodoWrite" -> "(updated todo list)"

        "Agent" -> truncate(input["description"] as? String ?: "", 80)

        else -> input.entries.take(2).joinToString(", ") { (k, v) ->
            "$k=${truncate(v?.toString() ?: "", 60)}"
        }
    }

    private fun truncate(s: String, maxLen: Int): String = if (s.length <= maxLen) s else s.take(maxLen - 1) + "…"
}
