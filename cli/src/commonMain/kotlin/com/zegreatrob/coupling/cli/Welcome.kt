package com.zegreatrob.coupling.cli

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.context

class Welcome : SuspendingCliktCommand() {

    init {
        context {
            readEnvvar = { key -> getEnv(key) }
        }
    }

    override suspend fun run() {
        if (this.currentContext.invokedSubcommands.isEmpty()) {
            echo("Welcome to Coupling CLI.")
        }
    }
}
