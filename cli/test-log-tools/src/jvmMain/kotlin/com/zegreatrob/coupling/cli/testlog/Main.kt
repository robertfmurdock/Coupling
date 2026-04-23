package com.zegreatrob.coupling.cli.testlog

import com.zegreatrob.coupling.testlog.analysis.TestLogCommand
import com.zegreatrob.coupling.testlog.analysis.TestLogRequest
import com.zegreatrob.coupling.testlog.analysis.TestLogTools

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

    val result = TestLogTools.run(
        request = TestLogRequest(
            command = command,
            args = args.drop(1),
        ),
    )
    result.outputJson?.let(::println)
    result.errorOutput?.let(System.err::println)
    if (result.exitCode != 0) {
        kotlin.system.exitProcess(result.exitCode)
    }
}

private fun printUsage() {
    System.err.println("usage: test-log-tools <validate|analyze> [options] <path>")
}
