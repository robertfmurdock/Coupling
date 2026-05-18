package com.zegreatrob.coupling.plugins

import org.gradle.api.DefaultTask

abstract class WeeklyCleanupLogTask : DefaultTask() {
    protected fun runIdHeader(runId: String): String = buildString {
        appendLine("**GH Actions run:** https://github.com/robertfmurdock/Coupling/actions/runs/$runId")
        appendLine()
        appendLine("**Download raw JSONL:** `gh run download $runId -n agent-stream-log`")
        appendLine()
        appendLine("---")
        appendLine()
    }
}
