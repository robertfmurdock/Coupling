import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

abstract class WeeklyCleanupPlanTask : DefaultTask() {
    @get:Internal
    abstract val outputFilePath: Property<String>

    @get:Input
    abstract val focusOverride: Property<String>

    @get:Input
    abstract val runDateOverride: Property<String>

    @get:Input
    abstract val maxChangedFiles: Property<Int>

    @get:Input
    abstract val maxChangedLines: Property<Int>

    @get:Input
    abstract val strategyOverride: Property<String>

    @TaskAction
    fun writePlan() {
        fun shellQuote(value: String): String = "'${value.replace("'", "'\"'\"'")}'"
        val resolvedFocusOverride = focusOverride.get().trim()
        val runDate = runDateOverride.get().trim().takeUnless { it.isBlank() }
            ?: LocalDate.now().toString()
        val allowedStrategies = listOf("dead-code", "boundary-check", "test-grooming", "suggest-new-strategy")
        val resolvedStrategyOverride = strategyOverride.get().trim()
        val strategy = if (resolvedStrategyOverride.isNotBlank()) {
            require(allowedStrategies.contains(resolvedStrategyOverride)) {
                "weeklyCleanupStrategyOverride must be one of: ${allowedStrategies.joinToString(", ")}"
            }
            resolvedStrategyOverride
        } else {
            val week = LocalDate.now().get(WeekFields.of(Locale.ROOT).weekOfWeekBasedYear())
            val strategyRotation = listOf("dead-code", "dead-code", "dead-code", "boundary-check", "test-grooming", "suggest-new-strategy")
            strategyRotation[week % strategyRotation.size]
        }
        val allowedFocuses = listOf(
            "client/components",
            "sdk",
            "server/actionz",
            "libraries/model",
            "libraries/repository/core",
            "e2e",
        )
        val focus = if (resolvedFocusOverride.isNotBlank()) {
            require(allowedFocuses.contains(resolvedFocusOverride)) {
                "weeklyCleanupFocusOverride must be one of: ${allowedFocuses.joinToString(", ")}"
            }
            resolvedFocusOverride
        } else {
            val week = LocalDate.now().get(WeekFields.of(Locale.ROOT).weekOfWeekBasedYear())
            allowedFocuses[week % allowedFocuses.size]
        }
        val moduleTask = when (focus) {
            "client/components" -> "./gradlew :client:components:check"
            "sdk" -> "./gradlew :sdk:check"
            "server/actionz" -> "./gradlew :server:actionz:check"
            "libraries/model" -> "./gradlew :libraries:model:check"
            "libraries/repository/core" -> "./gradlew :libraries:repository:core:check"
            "e2e" -> "./gradlew :e2e:check"
            else -> throw GradleException("Unsupported focus: $focus")
        }
        val out = File(outputFilePath.get())
        out.parentFile.mkdirs()
        out.writeText(
            buildString {
                appendLine("FOCUS=${shellQuote(focus)}")
                appendLine("RUN_DATE=${shellQuote(runDate)}")
                appendLine("BRANCH=${shellQuote("bot/cleanup/$focus/$runDate")}")
                appendLine("TITLE=${shellQuote("chore(cleanup): $focus architecture-aligned cleanup")}")
                appendLine("MODULE_TASK=${shellQuote(moduleTask)}")
                appendLine("MAX_FILES=${maxChangedFiles.get()}")
                appendLine("MAX_LINES=${maxChangedLines.get()}")
                appendLine("STRATEGY=${shellQuote(strategy)}")
            },
        )
        logger.lifecycle("Wrote weekly cleanup plan: ${out.absolutePath}")
    }
}

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
        val plan = File(planFilePath.get())
            .readLines()
            .filter { it.contains("=") }
            .associate { line ->
                val i = line.indexOf('=')
                line.substring(0, i) to line.substring(i + 1).trim('\'')
            }
        val focus = plan["FOCUS"] ?: throw GradleException("Missing FOCUS in plan file")
        val runDate = plan["RUN_DATE"] ?: throw GradleException("Missing RUN_DATE in plan file")
        val moduleTask = plan["MODULE_TASK"] ?: throw GradleException("Missing MODULE_TASK in plan file")
        val maxFiles = plan["MAX_FILES"] ?: throw GradleException("Missing MAX_FILES in plan file")
        val maxLines = plan["MAX_LINES"] ?: throw GradleException("Missing MAX_LINES in plan file")
        val strategy = plan["STRATEGY"] ?: "dead-code"
        val historyFile = File(historyFilePath.get())
        val queuedEntries = if (historyFile.exists()) {
            historyFile.readLines().filter { it.trimStart().startsWith("- ") && it.contains(": queued") }.takeLast(10)
        } else emptyList()
        val queuedSection = if (queuedEntries.isNotEmpty()) buildString {
            appendLine("**Start here:** These candidates were queued from prior runs — investigate these before generating new ones:")
            appendLine()
            queuedEntries.forEach { appendLine(it) }
            appendLine()
        } else ""
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
        val plan = File(planFilePath.get())
            .readLines()
            .filter { it.contains("=") }
            .associate { line ->
                val i = line.indexOf('=')
                line.substring(0, i) to line.substring(i + 1).trim('\'')
            }
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
            val cutoff = java.time.LocalDate.now().minusMonths(3)
            val sectionDateRegex = Regex("""^## (\d{4}-\d{2}-\d{2})""")
            return historyFile.readLines()
                .fold(listOf<Pair<java.time.LocalDate?, List<String>>>()) { sections, line ->
                    val dateMatch = sectionDateRegex.find(line)
                    if (dateMatch != null) {
                        sections + (java.time.LocalDate.parse(dateMatch.groupValues[1]) to emptyList())
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
                    "bash", "-c",
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
        out.writeText(buildString {
            candidateHeaders.forEach(::appendLine)
            appendLine()
            if (sorted.isEmpty()) {
                appendLine(emptyMessage)
            } else {
                sorted.forEach { (file, size) ->
                    appendLine("- ${file.relativeTo(rootDir).path} (${size} bytes)")
                }
            }
        })
        logger.lifecycle("Wrote weekly cleanup candidates: ${out.absolutePath} (${sorted.size} candidates)")
    }
}

abstract class WeeklyCleanupEvaluateTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Internal
    abstract val planFilePath: Property<String>

    @get:Internal
    abstract val outputFilePath: Property<String>

    @get:Input
    abstract val maxChangedFiles: Property<Int>

    @get:Input
    abstract val maxChangedLines: Property<Int>

    @TaskAction
    fun evaluate() {
        val plan = File(planFilePath.get())
            .readLines()
            .filter { it.contains("=") }
            .associate { line ->
                val i = line.indexOf('=')
                line.substring(0, i) to line.substring(i + 1)
            }
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

val weeklyCleanupDir = rootProject.layout.buildDirectory.dir("weekly-cleanup")
val weeklyCleanupPlanFilePath = weeklyCleanupDir.map { it.file("plan.env").asFile.absolutePath }
val weeklyCleanupPromptFilePath = weeklyCleanupDir.map { it.file("prompt.md").asFile.absolutePath }
val weeklyCleanupEvalFilePath = weeklyCleanupDir.map { it.file("evaluation.env").asFile.absolutePath }
val weeklyCleanupCandidatesFilePath = weeklyCleanupDir.map { it.file("candidates.md").asFile.absolutePath }

tasks {
    val weeklyCleanupPlan = register<WeeklyCleanupPlanTask>("weeklyCleanupPlan") {
        group = "automation"
        description = "Derives weekly cleanup focus and writes plan metadata for local/CI reuse."
        outputFilePath.set(weeklyCleanupPlanFilePath)
        focusOverride.set(providers.gradleProperty("weeklyCleanupFocusOverride").orElse(""))
        runDateOverride.set(providers.gradleProperty("weeklyCleanupRunDateOverride").orElse(""))
        maxChangedFiles.set(
            providers.gradleProperty("weeklyCleanupMaxChangedFiles")
                .map { it.toInt() }
                .orElse(20),
        )
        maxChangedLines.set(
            providers.gradleProperty("weeklyCleanupMaxChangedLines")
                .map { it.toInt() }
                .orElse(400),
        )
        strategyOverride.set(providers.gradleProperty("weeklyCleanupStrategyOverride").orElse(""))
    }

    register<WeeklyCleanupRenderPromptTask>("weeklyCleanupRenderPrompt") {
        group = "automation"
        description = "Renders weekly cleanup prompt from harness template + strategy + computed plan."
        dependsOn(weeklyCleanupPlan)
        templateFilePath.set(rootProject.file(".github/weekly-cleanup/agent-prompt-harness.md").absolutePath)
        planFilePath.set(weeklyCleanupPlanFilePath)
        outputFilePath.set(weeklyCleanupPromptFilePath)
        historyFilePath.set(rootProject.file(".github/weekly-cleanup/cleanup-history.md").absolutePath)
        candidatesFilePath.set(weeklyCleanupCandidatesFilePath)
        strategyDirPath.set(rootProject.file("agents.d/context").absolutePath)
    }

    register<WeeklyCleanupCandidatesTask>("weeklyCleanupCandidates") {
        group = "automation"
        description = "Produces a ranked candidate list for the weekly cleanup focus area (strategy-aware)."
        dependsOn(weeklyCleanupPlan)
        planFilePath.set(weeklyCleanupPlanFilePath)
        outputFilePath.set(weeklyCleanupCandidatesFilePath)
        historyFilePath.set(rootProject.file(".github/weekly-cleanup/cleanup-history.md").absolutePath)
    }

    register<WeeklyCleanupEvaluateTask>("weeklyCleanupEvaluate") {
        group = "automation"
        description = "Evaluates weekly cleanup diff against file/line/focus safety gates."
        dependsOn(weeklyCleanupPlan)
        planFilePath.set(weeklyCleanupPlanFilePath)
        outputFilePath.set(weeklyCleanupEvalFilePath)
        maxChangedFiles.set(
            providers.gradleProperty("weeklyCleanupMaxChangedFiles")
                .map { it.toInt() }
                .orElse(20),
        )
        maxChangedLines.set(
            providers.gradleProperty("weeklyCleanupMaxChangedLines")
                .map { it.toInt() }
                .orElse(400),
        )
    }
}
