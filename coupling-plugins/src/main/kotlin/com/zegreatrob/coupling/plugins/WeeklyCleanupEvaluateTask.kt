package com.zegreatrob.coupling.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

abstract class WeeklyCleanupEvaluateTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:OutputFile
    abstract val outputFilePath: Property<String>

    @get:Input
    abstract val maxChangedFiles: Property<Int>

    @get:Input
    abstract val maxChangedLines: Property<Int>

    @TaskAction
    fun evaluate() {
        val maxFiles = maxChangedFiles.get()
        val maxLines = maxChangedLines.get()

        fun git(vararg args: String): String {
            val stdout = ByteArrayOutputStream()
            execOperations.exec {
                commandLine("git", *args)
                standardOutput = stdout
            }
            return stdout.toString().trim()
        }

        val changedFilesRaw = git("diff", "--name-only")
        val changedFiles = changedFilesRaw.lines().filter { it.isNotBlank() }
        val hasChanges = changedFiles.isNotEmpty()
        val shortstat = git("diff", "--shortstat")
        val insertions = Regex("""(\d+)\s+insertion""").find(shortstat)?.groupValues?.get(1)?.toInt() ?: 0
        val deletions = Regex("""(\d+)\s+deletion""").find(shortstat)?.groupValues?.get(1)?.toInt() ?: 0
        val changedLines = insertions + deletions
        val gateOk = hasChanges && changedFiles.size <= maxFiles && changedLines <= maxLines
        val out = File(outputFilePath.get())
        out.parentFile.mkdirs()
        out.writeText(
            buildString {
                appendLine("HAS_CHANGES=$hasChanges")
                appendLine("FILE_COUNT=${changedFiles.size}")
                appendLine("CHANGED_LINES=$changedLines")
                appendLine("GATE_OK=$gateOk")
            },
        )
        logger.lifecycle("Wrote weekly cleanup evaluation: ${out.absolutePath}")
    }
}
