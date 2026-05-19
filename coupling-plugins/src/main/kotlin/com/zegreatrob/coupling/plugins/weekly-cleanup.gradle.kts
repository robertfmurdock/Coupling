package com.zegreatrob.coupling.plugins

import com.zegreatrob.coupling.plugins.cleanup.WeeklyCleanupCandidatesTask
import com.zegreatrob.coupling.plugins.cleanup.WeeklyCleanupEvaluateTask
import com.zegreatrob.coupling.plugins.cleanup.WeeklyCleanupPlanTask
import com.zegreatrob.coupling.plugins.cleanup.WeeklyCleanupRenderLogSummaryTask
import com.zegreatrob.coupling.plugins.cleanup.WeeklyCleanupRenderLogTask
import com.zegreatrob.coupling.plugins.cleanup.WeeklyCleanupRenderPromptTask
import com.zegreatrob.coupling.plugins.cleanup.WeeklyCleanupWriteLogEntryTask

val maxChangedFilesProvider = providers.gradleProperty("weeklyCleanupMaxChangedFiles")
    .map { it.toInt() }
    .orElse(20)
val maxChangedLinesProvider = providers.gradleProperty("weeklyCleanupMaxChangedLines")
    .map { it.toInt() }
    .orElse(400)

val weeklyCleanupDir = rootProject.layout.buildDirectory.dir("weekly-cleanup")
val weeklyCleanupPlanFilePath = weeklyCleanupDir.map { it.file("plan.env").asFile.absolutePath }
val weeklyCleanupPromptFilePath = weeklyCleanupDir.map { it.file("prompt.md").asFile.absolutePath }
val weeklyCleanupEvalFilePath = weeklyCleanupDir.map { it.file("evaluation.env").asFile.absolutePath }
val weeklyCleanupCandidatesFilePath = weeklyCleanupDir.map { it.file("candidates.md").asFile.absolutePath }
val weeklyCleanupJsonlFilePath = weeklyCleanupDir.map { it.file("agent-stream.jsonl").asFile.absolutePath }
val weeklyCleanupLogFilePath = weeklyCleanupDir.map { it.file("agent-log.md").asFile.absolutePath }
val weeklyCleanupLogSummaryFilePath = weeklyCleanupDir.map { it.file("agent-log-summary.md").asFile.absolutePath }

tasks {
    val weeklyCleanupPlan = register<WeeklyCleanupPlanTask>("weeklyCleanupPlan") {
        group = "automation"
        description = "Derives weekly cleanup focus and writes plan metadata for local/CI reuse."
        outputFilePath.set(weeklyCleanupPlanFilePath)
        focusOverride.set(providers.gradleProperty("weeklyCleanupFocusOverride").orElse(""))
        runDateOverride.set(providers.gradleProperty("weeklyCleanupRunDateOverride").orElse(""))
        maxChangedFiles.set(maxChangedFilesProvider)
        maxChangedLines.set(maxChangedLinesProvider)
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
        outputFilePath.set(weeklyCleanupEvalFilePath)
        maxChangedFiles.set(maxChangedFilesProvider)
        maxChangedLines.set(maxChangedLinesProvider)
    }

    val weeklyCleanupRenderLog = register<WeeklyCleanupRenderLogTask>("weeklyCleanupRenderLog") {
        group = "automation"
        description = "Renders agent reasoning log from JSONL stream to human-readable markdown."
        jsonlFilePath.set(weeklyCleanupJsonlFilePath)
        outputFilePath.set(weeklyCleanupLogFilePath)
    }

    register<WeeklyCleanupRenderLogSummaryTask>("weeklyCleanupRenderLogSummary") {
        group = "automation"
        description = "Renders condensed agent log summary (first text block per turn) for PR body injection."
        jsonlFilePath.set(weeklyCleanupJsonlFilePath)
        outputFilePath.set(weeklyCleanupLogSummaryFilePath)
        runId.set(providers.gradleProperty("weeklyCleanupRunId").orElse(""))
    }

    register<WeeklyCleanupWriteLogEntryTask>("weeklyCleanupWriteLogEntry") {
        group = "automation"
        description = "Writes rendered agent log to .github/weekly-cleanup/logs/ for in-repo persistence."
        dependsOn(weeklyCleanupRenderLog)
        dependsOn(weeklyCleanupPlan)
        logFilePath.set(weeklyCleanupLogFilePath)
        planFilePath.set(weeklyCleanupPlanFilePath)
        runId.set(providers.gradleProperty("weeklyCleanupRunId").orElse(""))
        outputDirPath.set(rootProject.file(".github/weekly-cleanup/logs").absolutePath)
    }
}
