package com.zegreatrob.coupling.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

abstract class WeeklyCleanupPlanTask : DefaultTask() {
    @get:OutputFile
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
