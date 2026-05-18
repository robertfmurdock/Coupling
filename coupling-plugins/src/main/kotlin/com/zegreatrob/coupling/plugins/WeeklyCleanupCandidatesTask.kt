package com.zegreatrob.coupling.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

abstract class WeeklyCleanupCandidatesTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Internal
    abstract val planFilePath: Property<String>

    @get:Internal
    abstract val outputFilePath: Property<String>

    @get:Internal
    abstract val historyFilePath: Property<String>

    @TaskAction
    fun generateCandidates() {
        val plan = File(planFilePath.get()).parsePlanEnv()
        val focus = plan["FOCUS"] ?: throw GradleException("Missing FOCUS in plan file")
        val strategy = plan["STRATEGY"] ?: "dead-code"
        val rootDir = project.rootDir
        val focusDir = rootDir.resolve(focus)
        if (!focusDir.exists()) throw GradleException("Focus directory does not exist: ${focusDir.absolutePath}")
        val finalVerdicts = setOf(
            "verified-in-use", "deleted", "skipped",
            "test-grooming-deleted", "test-grooming-revert", "test-grooming-gap",
            "test-grooming-move-candidate", "test-grooming-orphan", "verified-anchor-or-variation",
        )

        fun settledBaseNames(): Set<String> {
            val resolvedHistoryPath = historyFilePath.get()
            if (resolvedHistoryPath.isBlank()) return emptySet()
            val historyFile = File(resolvedHistoryPath)
            if (!historyFile.exists()) return emptySet()
            val cutoff = LocalDate.now().minusMonths(3)
            val sectionDateRegex = Regex("""^## (\d{4}-\d{2}-\d{2})""")
            return historyFile.readLines()
                .fold(listOf<Pair<LocalDate?, List<String>>>()) { sections, line ->
                    val dateMatch = sectionDateRegex.find(line)
                    if (dateMatch != null) {
                        sections + (LocalDate.parse(dateMatch.groupValues[1]) to emptyList())
                    } else {
                        val (date, lines) = sections.lastOrNull() ?: (null to emptyList<String>())
                        sections.dropLast(1) + (date to (lines + line))
                    }
                }
                .filter { (date, _) -> date != null && !date.isBefore(cutoff) }
                .flatMap { (_, lines) -> lines }
                .filter { it.trimStart().startsWith("- ") }
                .filter { finalVerdicts.any { verdict -> it.contains(": $verdict") } }
                .map { it.trimStart().removePrefix("- ").substringBefore(":").trim() }
                .filter { it.isNotBlank() }
                .toSet()
        }

        fun File.isDiscoveredByTestRunner() = nameWithoutExtension.endsWith("Test")

        fun hasNoImportReferences(file: File): Boolean {
            val stdout = ByteArrayOutputStream()
            execOperations.exec {
                commandLine(
                    "bash",
                    "-c",
                    "grep -rl --include='*.kt' 'import.*\\b${file.nameWithoutExtension}\\b' '${rootDir.absolutePath}' 2>/dev/null || true",
                )
                standardOutput = stdout
                isIgnoreExitValue = true
            }
            return stdout.toString().trim().lines().none { it.isNotBlank() && it != file.absolutePath }
        }

        val candidateFilter: (File) -> Boolean
        val candidateHeaders: List<String>
        val emptyMessage: String

        if (strategy == "test-grooming") {
            candidateFilter = { it.isDiscoveredByTestRunner() }
            candidateHeaders = listOf(
                "# Weekly Cleanup Test-Grooming Candidates",
                "# Focus: $focus",
                "# Ranked by file size ascending",
                "# All test files in focus area — evaluate for architectural placement",
            )
            emptyMessage = "(no test files found in focus area)"
        } else {
            candidateFilter = { !it.isDiscoveredByTestRunner() && hasNoImportReferences(it) }
            candidateHeaders = listOf(
                "# Weekly Cleanup Dead-Code Candidates",
                "# Focus: $focus",
                "# Ranked by file size ascending (lowest blast radius first)",
                "# Files with zero import references outside their own file",
            )
            emptyMessage = "(no zero-import candidates found in focus area)"
        }

        val settledBaseNames = settledBaseNames()
        val sorted = focusDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filterNot { it.name in settledBaseNames }
            .filter(candidateFilter)
            .map { it to it.length() }
            .sortedBy { it.second }
            .toList()
        val out = File(outputFilePath.get())
        out.parentFile.mkdirs()
        out.writeText(
            buildString {
                candidateHeaders.forEach(::appendLine)
                appendLine()
                if (sorted.isEmpty()) {
                    appendLine(emptyMessage)
                } else {
                    sorted.forEach { (file, size) ->
                        appendLine("- ${file.relativeTo(rootDir).path} ($size bytes)")
                    }
                }
            },
        )
        logger.lifecycle("Wrote weekly cleanup candidates: ${out.absolutePath} (${sorted.size} candidates)")
    }
}
