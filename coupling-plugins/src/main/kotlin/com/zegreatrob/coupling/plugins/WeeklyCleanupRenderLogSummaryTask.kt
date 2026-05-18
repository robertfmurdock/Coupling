package com.zegreatrob.coupling.plugins

import groovy.json.JsonSlurper
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class WeeklyCleanupRenderLogSummaryTask : WeeklyCleanupLogTask() {
    @get:Internal
    abstract val jsonlFilePath: Property<String>

    @get:Internal
    abstract val outputFilePath: Property<String>

    @get:Input
    abstract val runId: Property<String>

    @Suppress("UNCHECKED_CAST")
    @TaskAction
    fun render() {
        val jsonlFile = File(jsonlFilePath.get())
        if (!jsonlFile.exists()) {
            logger.lifecycle("No agent stream JSONL at ${jsonlFile.absolutePath}; skipping summary render.")
            return
        }
        val slurper = JsonSlurper()
        val lines = mutableListOf<String>()
        val maxLines = 100

        val resolvedRunId = runId.get()
        if (resolvedRunId.isNotBlank()) {
            lines += runIdHeader(resolvedRunId).trimEnd().split("\n")
            lines += ""
        }

        var turnIndex = 0
        var truncated = false

        jsonlFile.useLines { lineSeq: Sequence<String> ->
            for (line in lineSeq) {
                if (line.isBlank()) continue
                val event = try {
                    slurper.parseText(line) as? Map<*, *>
                } catch (e: Exception) {
                    null
                } ?: continue
                when (event["type"] as? String) {
                    "assistant" -> {
                        if (lines.size >= maxLines) {
                            truncated = true
                            break
                        }
                        turnIndex++
                        val message = event["message"] as? Map<*, *> ?: continue
                        val content = message["content"] as? List<*> ?: continue
                        val firstText = content.filterIsInstance<Map<*, *>>()
                            .firstOrNull { it["type"] == "text" }
                            ?.let { it["text"] as? String }
                            ?.trim()
                        if (!firstText.isNullOrBlank()) {
                            lines += "**Turn $turnIndex:**"
                            lines += ""
                            lines += firstText
                            lines += ""
                        }
                    }

                    "result" -> {
                        val subtype = event["subtype"] as? String ?: "unknown"
                        lines += "**Result:** $subtype"
                    }
                }
            }
        }

        if (truncated) {
            lines += ""
            lines += "_(truncated — see full log in `.github/weekly-cleanup/logs/`)_"
        }

        val out = File(outputFilePath.get())
        out.parentFile.mkdirs()
        out.writeText(lines.joinToString("\n") + "\n")
        logger.lifecycle("Wrote agent log summary: ${out.absolutePath}")
    }
}
