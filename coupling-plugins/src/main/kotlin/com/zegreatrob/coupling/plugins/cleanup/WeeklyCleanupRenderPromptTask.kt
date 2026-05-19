package com.zegreatrob.coupling.plugins.cleanup

import com.zegreatrob.coupling.plugins.util.parsePlanEnv
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class WeeklyCleanupRenderPromptTask : DefaultTask() {
    @get:Internal
    abstract val templateFilePath: Property<String>

    @get:Internal
    abstract val planFilePath: Property<String>

    @get:Internal
    abstract val outputFilePath: Property<String>

    @get:Internal
    abstract val historyFilePath: Property<String>

    @get:Internal
    abstract val candidatesFilePath: Property<String>

    @get:Internal
    abstract val strategyDirPath: Property<String>

    @TaskAction
    fun render() {
        val plan = File(planFilePath.get()).parsePlanEnv()
        val focus = plan["FOCUS"] ?: throw GradleException("Missing FOCUS in plan file")
        val runDate = plan["RUN_DATE"] ?: throw GradleException("Missing RUN_DATE in plan file")
        val moduleTask = plan["MODULE_TASK"] ?: throw GradleException("Missing MODULE_TASK in plan file")
        val maxFiles = plan["MAX_FILES"] ?: throw GradleException("Missing MAX_FILES in plan file")
        val maxLines = plan["MAX_LINES"] ?: throw GradleException("Missing MAX_LINES in plan file")
        val strategy = plan["STRATEGY"] ?: "dead-code"
        val historyFile = File(historyFilePath.get())
        val queuedEntries = if (historyFile.exists()) {
            historyFile.readLines().filter { it.trimStart().startsWith("- ") && it.contains(": queued") }.takeLast(10)
        } else {
            emptyList()
        }
        val queuedSection = if (queuedEntries.isNotEmpty()) {
            buildString {
                appendLine("**Start here:** These candidates were queued from prior runs — investigate these before generating new ones:")
                appendLine()
                queuedEntries.forEach { appendLine(it) }
                appendLine()
            }
        } else {
            ""
        }
        val strategyFile = File(strategyDirPath.get()).resolve("agent-strategy-$strategy.md")
        if (!strategyFile.exists()) throw GradleException("Strategy file not found: ${strategyFile.absolutePath}")
        val strategyContent = strategyFile.readText().trimEnd()
        val rendered = File(templateFilePath.get())
            .readText()
            .replace("__STRATEGY_CONTENT__", strategyContent)
            .replace("__FOCUS_AREA__", focus)
            .replace("__RUN_DATE__", runDate)
            .replace("__MODULE_TASK__", moduleTask)
            .replace("__MAX_FILES__", maxFiles)
            .replace("__MAX_LINES__", maxLines)
            .replace("__QUEUED_CANDIDATES__", queuedSection)
            .replace("__CANDIDATES_FILE__", candidatesFilePath.get())
        val out = File(outputFilePath.get())
        out.parentFile.mkdirs()
        out.writeText(rendered)
        logger.lifecycle("Wrote weekly cleanup prompt: ${out.absolutePath}")
    }
}
