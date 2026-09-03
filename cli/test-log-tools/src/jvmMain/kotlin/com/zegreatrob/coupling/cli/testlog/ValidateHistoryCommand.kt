package com.zegreatrob.coupling.cli.testlog

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.zegreatrob.coupling.testlog.analysis.TestLogCommand
import com.zegreatrob.coupling.testlog.analysis.TestLogRequest
import com.zegreatrob.coupling.testlog.analysis.TestLogTools

class ValidateHistoryCommand : SuspendingCliktCommand("validate-history") {
    private val strict by option("--strict").flag(default = false)
    private val reportFile by option("--report-file")
    private val quietSuccess by option("--quiet-success").flag(default = false)
    private val failureSummary by option("--failure-summary").flag(default = false)
    private val path by argument(name = "path").default("build/test-output/test.jsonl")

    override suspend fun run() {
        val result = TestLogTools.run(
            TestLogRequest(
                TestLogCommand.VALIDATE_HISTORY,
                buildList {
                    if (strict) add("--strict")
                    add(path)
                },
            ),
        )
        OutputRenderer("validate-history", OutputOptions(reportFile, quietSuccess, failureSummary)).render(result)
    }
}
