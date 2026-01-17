package com.zegreatrob.coupling.cli

import com.github.ajalt.clikt.command.SuspendingCliktCommand

class Welcome : SuspendingCliktCommand() {

    override suspend fun run() {
        if (this.currentContext.invokedSubcommands.isEmpty()) {
            echo("Welcome to Coupling CLI.")
        }
    }
}
