package com.zegreatrob.coupling.cli.testlog

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.zegreatrob.coupling.testlog.analysis.TestLogCommand
import com.zegreatrob.coupling.testlog.analysis.TestLogRequest
import com.zegreatrob.coupling.testlog.analysis.TestLogTools

class AnalyzeCommand : SuspendingCliktCommand("analyze") {
    private val strict by option("--strict").flag(default = false)
    private val maxOffenders by option("--max-offenders")
    private val reportFile by option("--report-file")
    private val quietSuccess by option("--quiet-success").flag(default = false)
    private val failureSummary by option("--failure-summary").flag(default = false)
    private val path by argument(name = "path").default("build/test-output/test.jsonl")

    override suspend fun run() {
        val toolArgs = buildList {
            if (strict) add("--strict")
            maxOffenders?.let { add("--max-offenders=$it") }
            add(path)
        }
        val result = TestLogTools.run(
            TestLogRequest(
                command = TestLogCommand.ANALYZE,
                args = toolArgs,
            ),
        )
        OutputRenderer(
            commandName = "analyze",
            outputOptions = OutputOptions(
                reportFilePath = reportFile,
                quietSuccess = quietSuccess,
                failureSummary = failureSummary,
            ),
        ).render(result)
    }
}
