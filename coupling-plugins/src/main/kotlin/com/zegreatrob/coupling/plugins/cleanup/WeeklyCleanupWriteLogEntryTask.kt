package com.zegreatrob.coupling.plugins.cleanup

import com.zegreatrob.coupling.plugins.util.parsePlanEnv
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class WeeklyCleanupWriteLogEntryTask : WeeklyCleanupLogTask() {
    @get:InputFile
    abstract val logFilePath: Property<String>

    @get:InputFile
    abstract val planFilePath: Property<String>

    @get:Input
    abstract val runId: Property<String>

    @get:OutputDirectory
    abstract val outputDirPath: Property<String>

    @TaskAction
    fun writeEntry() {
        val logFile = File(logFilePath.get())
        if (!logFile.exists()) {
            logger.lifecycle("No rendered log at ${logFilePath.get()}; skipping log entry write.")
            return
        }
        val plan = File(planFilePath.get()).parsePlanEnv()
        val runDate = plan["RUN_DATE"] ?: throw GradleException("Missing RUN_DATE in plan file")
        val focus = plan["FOCUS"] ?: throw GradleException("Missing FOCUS in plan file")
        val resolvedRunId = runId.get()
        val safeFocus = focus.replace("/", "-")
        val filename = "$runDate-$safeFocus.md"
        val header = runIdHeader(resolvedRunId)
        val outDir = File(outputDirPath.get())
        outDir.mkdirs()
        val outFile = outDir.resolve(filename)
        outFile.writeText(header + logFile.readText())
        logger.lifecycle("Wrote weekly cleanup log entry: ${outFile.absolutePath}")
    }
}
