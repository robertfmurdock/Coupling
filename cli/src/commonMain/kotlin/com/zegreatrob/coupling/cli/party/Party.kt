package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import kotlin.time.Clock

private class Party : CliktCommand() {
    override fun run() {
    }
}

fun party(): CliktCommand = Party()
    .subcommands(List())
    .subcommands(
        Contribution()
            .subcommands(SaveContribution(clock = Clock.System))
            .subcommands(BatchContribution(clock = Clock.System)),
    )
