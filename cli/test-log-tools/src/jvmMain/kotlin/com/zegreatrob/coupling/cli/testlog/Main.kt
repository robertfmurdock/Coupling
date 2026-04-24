package com.zegreatrob.coupling.cli.testlog

import com.fasterxml.jackson.databind.ObjectMapper
import com.zegreatrob.coupling.testlog.analysis.TestLogCommand
import com.zegreatrob.coupling.testlog.analysis.TestLogRequest
import com.zegreatrob.coupling.testlog.analysis.TestLogTools
import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        printUsage()
        kotlin.system.exitProcess(2)
    }

    val command = when (args.first()) {
        "validate" -> TestLogCommand.VALIDATE

        "analyze" -> TestLogCommand.ANALYZE

        else -> {
            printUsage()
            kotlin.system.exitProcess(2)
        }
    }

    val outputOptions = parseOutputOptions(args.drop(1))
    val result = TestLogTools.run(
        request = TestLogRequest(
            command = command,
            args = outputOptions.forwardedArgs,
        ),
    )

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
        val summary = buildFailureSummary(
            command = command,
            outputJson = result.outputJson,
            reportFilePath = outputOptions.reportFilePath,
        )
        if (summary.isNotBlank()) {
            System.err.println(summary)
        }
    } else {
        result.outputJson?.let(::println)
    }
    result.errorOutput?.takeIf { it.isNotBlank() }?.let(System.err::println)
    kotlin.system.exitProcess(result.exitCode)
}

private data class OutputOptions(
    val reportFilePath: String? = null,
    val quietSuccess: Boolean = false,
    val failureSummary: Boolean = false,
    val forwardedArgs: List<String>,
)

private fun parseOutputOptions(rawArgs: List<String>): OutputOptions {
    var reportFilePath: String? = null
    var quietSuccess = false
    var failureSummary = false
    val forwarded = mutableListOf<String>()

    var index = 0
    while (index < rawArgs.size) {
        when {
            rawArgs[index] == "--report-file" -> {
                val value = rawArgs.getOrNull(index + 1)
                if (value == null) {
                    System.err.println("ERROR: --report-file requires a path value")
                    printUsage()
                    kotlin.system.exitProcess(2)
                }
                reportFilePath = value
                index += 2
            }

            rawArgs[index].startsWith("--report-file=") -> {
                reportFilePath = rawArgs[index].substringAfter('=')
                index += 1
            }

            rawArgs[index] == "--quiet-success" -> {
                quietSuccess = true
                index += 1
            }

            rawArgs[index] == "--failure-summary" -> {
                failureSummary = true
                index += 1
            }

            else -> {
                forwarded += rawArgs[index]
                index += 1
            }
        }
    }

    return OutputOptions(
        reportFilePath = reportFilePath,
        quietSuccess = quietSuccess,
        failureSummary = failureSummary,
        forwardedArgs = forwarded,
    )
}

private fun buildFailureSummary(
    command: TestLogCommand,
    outputJson: String?,
    reportFilePath: String?,
): String {
    if (outputJson.isNullOrBlank()) {
        return "${command.name.lowercase()} failed." +
            (reportFilePath?.let { "\nreport: $it" } ?: "")
    }

    val root = runCatching { ObjectMapper().readTree(outputJson) }.getOrNull()
    if (root == null) {
        return "${command.name.lowercase()} failed." +
            (reportFilePath?.let { "\nreport: $it" } ?: "")
    }

    return buildString {
        appendLine("${command.name.lowercase()} failed.")
        reportFilePath?.let { appendLine("report: $it") }
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

private fun printUsage() {
    System.err.println(
        "usage: test-log-tools <validate|analyze> [--report-file <path>] [--quiet-success] [--failure-summary] [options] <path>",
    )
}
