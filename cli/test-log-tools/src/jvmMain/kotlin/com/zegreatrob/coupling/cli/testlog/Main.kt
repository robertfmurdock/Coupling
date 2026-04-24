package com.zegreatrob.coupling.cli.testlog

import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.subcommands

suspend fun main(args: Array<String>) {
    TestLogToolsCli()
        .subcommands(ValidateCommand(), AnalyzeCommand())
        .main(args)
}
