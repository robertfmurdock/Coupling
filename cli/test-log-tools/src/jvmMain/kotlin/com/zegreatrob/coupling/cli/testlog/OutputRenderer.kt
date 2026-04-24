package com.zegreatrob.coupling.cli.testlog

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.ajalt.clikt.core.ProgramResult
import com.zegreatrob.coupling.testlog.analysis.TestLogRunResult
import java.io.File

class OutputRenderer(
    private val commandName: String,
    private val outputOptions: OutputOptions,
) {
    private val mapper = ObjectMapper()

    fun render(result: TestLogRunResult) {
        outputOptions.reportFilePath?.let { reportPath ->
            val reportFile = File(reportPath)
            reportFile.parentFile?.mkdirs()
            reportFile.writeText(result.outputJson ?: "")
        }

        if (result.exitCode == 0) {
            if (!outputOptions.quietSuccess) {
                result.outputJson?.let(::println)
            }
            result.errorOutput?.takeIf { it.isNotBlank() }?.let(System.err::println)
            return
        }

        if (outputOptions.failureSummary) {
            val summary = buildFailureSummary(result.outputJson)
            if (summary.isNotBlank()) {
                System.err.println(summary)
            }
        } else {
            result.outputJson?.let(::println)
        }
        result.errorOutput?.takeIf { it.isNotBlank() }?.let(System.err::println)
        throw ProgramResult(result.exitCode)
    }

    private fun buildFailureSummary(outputJson: String?): String {
        if (outputJson.isNullOrBlank()) {
            return "$commandName failed." +
                (outputOptions.reportFilePath?.let { "\nreport: $it" } ?: "")
        }

        val root = runCatching { mapper.readTree(outputJson) }.getOrNull()
        if (root == null) {
            return "$commandName failed." +
                (outputOptions.reportFilePath?.let { "\nreport: $it" } ?: "")
        }

        return buildString {
            appendLine("$commandName failed.")
            outputOptions.reportFilePath?.let { appendLine("report: $it") }
            root.get("mode")?.asText()?.let { appendLine("mode: $it") }
            if (root.has("failing_violations") || root.has("total_violations")) {
                appendLine(
                    "violations: failing=${root.get("failing_violations")?.asInt() ?: 0}, " +
                        "total=${root.get("total_violations")?.asInt() ?: 0}",
                )
            }

            val offenders = root.get("offenders")
            if (offenders != null && offenders.isArray && offenders.size() > 0) {
                appendLine("top offenders:")
                for (i in 0 until minOf(5, offenders.size())) {
                    val offender = offenders.get(i)
                    val line = offender.get("line")?.asInt()
                    val reason = offender.get("reason")?.asText()
                    if (line != null && reason != null) {
                        appendLine("  - line $line: $reason")
                    } else {
                        appendLine("  - ${offender.asText()}")
                    }
                }
            }
        }.trim()
    }
}
