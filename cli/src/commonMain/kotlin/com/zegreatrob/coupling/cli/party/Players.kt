package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.zegreatrob.coupling.cli.SdkProvider
import com.zegreatrob.coupling.cli.gql.PlayersQuery
import com.zegreatrob.coupling.cli.withSdk
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.schema.type.PartyInput

internal fun formatPlayers(partyId: PartyId, players: List<String>): String {
    val body = players.joinToString("\n") { "  - $it" }
    return if (body.isBlank()) {
        "Players for Party ID: ${partyId.value}"
    } else {
        "Players for Party ID: ${partyId.value}\n$body"
    }
}

class Players(private val sdkProvider: SdkProvider) : SuspendingCliktCommand() {
    private val context by requireObject<PartyContext>()
    private val partyId: PartyId by requireObject<PartyId>("partyId")

    override suspend fun run() {
        withSdk(env = context.env, echo = ::echo, sdkProvider = sdkProvider) { sdk ->
            val players = sdk.fire(GqlQuery(PlayersQuery(PartyInput(partyId))))
                ?.party
                ?.playerList
                ?.map { "${it.playerDetails.name} ${it.playerDetails.email}" }
                ?: emptyList()
            echo(formatPlayers(partyId, players))
        }
    }
}
