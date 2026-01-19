package com.zegreatrob.coupling.cli

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.options.versionOption

class CouplingCli : SuspendingCliktCommand() {

    init {
        versionOption(Versions.couplingVersion)
        context {
            readEnvvar = { key -> getEnv(key) }
        }
    }

    override suspend fun run() {
        val accessToken = getAccessToken()
        if (accessToken == null) {
            echo("You are not currently logged in. Some functions will not work.")
            echo("Run `coupling login` to log in.")
        }
    }
}
