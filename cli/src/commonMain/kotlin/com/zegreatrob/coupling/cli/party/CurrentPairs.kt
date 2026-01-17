package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.requireObject
import com.zegreatrob.coupling.cli.loadSdk
import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.callSign
import com.zegreatrob.coupling.model.pairassignmentdocument.players
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import com.zegreatrob.coupling.sdk.schema.CurrentPairAssignmentsQuery
import com.zegreatrob.testmints.action.ActionCannon

class CurrentPairs(val cannon: ActionCannon<CouplingSdkDispatcher>?) : SuspendingCliktCommand("current-pairs") {

    private val context by requireObject<PartyContext>()
    private val partyId: PartyId by requireObject<PartyId>("partyId")

    override suspend fun run() {
        val actionCannon = cannon ?: loadSdk(context.env, ::echo)
        val pairingSet = actionCannon?.fire(GqlQuery(CurrentPairAssignmentsQuery(partyId)))
            ?.party?.currentPairingSet?.pairingSetDetails?.toDomain()

        if (pairingSet == null) {
            throw CliktError("Party not found.", printError = true)
        }
        echo("Current Pairs for Party ID: ${partyId.value}")
        echo("")
        pairingSet.pairs.map { pair ->
            echo("- ${pair.callSign()}")
            echo("  ${pair.players.map { it.name }.toList().joinToString(" & ")}")
        }
    }
}
