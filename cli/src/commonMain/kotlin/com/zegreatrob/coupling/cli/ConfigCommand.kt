package com.zegreatrob.coupling.cli

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.findOrSetObject
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.zegreatrob.coupling.model.party.PartyId
import kotlinx.serialization.json.Json

class ConfigCommand : SuspendingCliktCommand("config") {
    init {
        context {
            val configFileSource = ConfigFileSource(readEnvvar)
            val config = configFileSource.config
            if (config != null) {
                findOrSetObject<CouplingCliConfig>("config") { config }
                valueSource = configFileSource
            }
        }
    }

    private val partyId: PartyId? by option().convert { PartyId(it) }
    val config by findOrSetObject<CouplingCliConfig>("config") { CouplingCliConfig() }

    override suspend fun run() {
        val configFilePath = currentContext.readEnvvar("PWD") + "/.coupling"
        config.copy(partyId = partyId)
            .let { Json.encodeToString(it) }
            .let { writeDataToFile(configFilePath, it) }
        echo("Updated file: $configFilePath")
    }
}
