package com.zegreatrob.coupling.cli

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.options.versionOption

class CouplingCli : SuspendingCliktCommand() {

    init {
        versionOption(com.zegreatrob.coupling.cli.Versions.couplingVersion)
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
