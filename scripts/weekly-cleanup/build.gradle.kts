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

    @TaskAction
    fun writePlan() {
        fun shellQuote(value: String): String = "'${value.replace("'", "'\"'\"'")}'"
        val resolvedFocusOverride = focusOverride.get().trim()
        val runDate = runDateOverride.get().trim().takeUnless { it.isBlank() }
            ?: LocalDate.now().toString()
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
        val allowedPattern = when (focus) {
            "client/components" -> "^(client/components/|client/src/|client/build\\.gradle\\.kts)"
            "sdk" -> "^(sdk/|sdk/build\\.gradle\\.kts)"
            "server/actionz" -> "^(server/actionz/|server/build\\.gradle\\.kts)"
            "libraries/model" -> "^(libraries/model/|libraries/model/build\\.gradle\\.kts)"
            "libraries/repository/core" -> "^(libraries/repository/core/|libraries/repository/core/build\\.gradle\\.kts)"
            "e2e" -> "^(e2e/|e2e/build\\.gradle\\.kts)"
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
                appendLine("ALLOWED_PATTERN=${shellQuote(allowedPattern)}")
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

    @TaskAction
    fun render() {
        val plan = File(planFilePath.get())
            .readLines()
            .filter { it.contains("=") }
            .associate { line ->
                val i = line.indexOf('=')
                line.substring(0, i) to line.substring(i + 1)
            }
        val focus = plan["FOCUS"] ?: throw GradleException("Missing FOCUS in plan file")
        val runDate = plan["RUN_DATE"] ?: throw GradleException("Missing RUN_DATE in plan file")
        val rendered = File(templateFilePath.get())
            .readText()
            .replace("__FOCUS_AREA__", focus)
            .replace("__RUN_DATE__", runDate)
        val out = File(outputFilePath.get())
        out.parentFile.mkdirs()
        out.writeText(rendered)
        logger.lifecycle("Wrote weekly cleanup prompt: ${out.absolutePath}")
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
        val allowedPattern = plan["ALLOWED_PATTERN"] ?: throw GradleException("Missing ALLOWED_PATTERN in plan file")
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
        val outOfScope = changedFiles.filterNot { Regex(allowedPattern).containsMatchIn(it) }

        val gateOk = hasChanges && outOfScope.isEmpty() && changedFiles.size <= maxFiles && changedLines <= maxLines
        val out = File(outputFilePath.get())
        out.parentFile.mkdirs()
        out.writeText(
            buildString {
                appendLine("HAS_CHANGES=$hasChanges")
                appendLine("FILE_COUNT=${changedFiles.size}")
                appendLine("CHANGED_LINES=$changedLines")
                appendLine("OUT_OF_SCOPE_COUNT=${outOfScope.size}")
                appendLine("OUT_OF_SCOPE_FILES=${outOfScope.joinToString(",")}")
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

tasks {
    val weeklyCleanupPlan = register<WeeklyCleanupPlanTask>("weeklyCleanupPlan") {
        group = "automation"
        description = "Derives weekly cleanup focus and writes plan metadata for local/CI reuse."
        outputFilePath.set(weeklyCleanupPlanFilePath)
        focusOverride.set(providers.gradleProperty("weeklyCleanupFocusOverride").orElse(""))
        runDateOverride.set(providers.gradleProperty("weeklyCleanupRunDateOverride").orElse(""))
    }

    register<WeeklyCleanupRenderPromptTask>("weeklyCleanupRenderPrompt") {
        group = "automation"
        description = "Renders weekly cleanup prompt from template + computed plan."
        dependsOn(weeklyCleanupPlan)
        templateFilePath.set(rootProject.file(".github/weekly-cleanup/agent-prompt.md").absolutePath)
        planFilePath.set(weeklyCleanupPlanFilePath)
        outputFilePath.set(weeklyCleanupPromptFilePath)
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
                .orElse(5),
        )
        maxChangedLines.set(
            providers.gradleProperty("weeklyCleanupMaxChangedLines")
                .map { it.toInt() }
                .orElse(200),
        )
    }
}
