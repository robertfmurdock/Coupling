package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.zegreatrob.coupling.cli.gql.PlayersQuery
import com.zegreatrob.coupling.cli.loadSdk
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.schema.type.PartyInput
import com.zegreatrob.testmints.action.ActionCannon

class Players(val cannon: ActionCannon<CouplingSdkDispatcher>?) : SuspendingCliktCommand() {
    private val context by requireObject<PartyContext>()
    private val partyId: PartyId by requireObject<PartyId>("partyId")
    override suspend fun run() {
        val actionCannon = cannon ?: loadSdk(context.env, ::echo)
        val result = actionCannon?.fire(GqlQuery(PlayersQuery(PartyInput(partyId))))

        echo("Players for Party ID: ${partyId.value}")
        result?.party?.playerList?.forEach { player ->
            echo("  - ${player.playerDetails.name} ${player.playerDetails.email}")
        }
    }
}
