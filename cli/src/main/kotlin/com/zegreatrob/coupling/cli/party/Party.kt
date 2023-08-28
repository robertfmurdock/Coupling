package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

private class Party : CliktCommand() {
    override fun run() {
    }
}

fun party(): CliktCommand = Party()
    .subcommands(List())
    .subcommands(Contribution())
