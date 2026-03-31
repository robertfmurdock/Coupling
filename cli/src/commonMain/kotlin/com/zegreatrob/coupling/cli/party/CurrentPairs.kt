package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.requireObject
import com.zegreatrob.coupling.cli.SdkProvider
import com.zegreatrob.coupling.cli.withSdk
import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.callSign
import com.zegreatrob.coupling.model.pairassignmentdocument.players
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import com.zegreatrob.coupling.sdk.schema.CurrentPairAssignmentsQuery

internal fun formatCurrentPairs(partyId: PartyId, pairs: List<Pair<String, String>>): String {
    val pairRows = pairs.joinToString("\n\n") { (callSign, players) ->
        "- $callSign\n  $players"
    }
    return if (pairRows.isBlank()) {
        "Current Pairs for Party ID: ${partyId.value}"
    } else {
        "Current Pairs for Party ID: ${partyId.value}\n\n$pairRows"
    }
}

class CurrentPairs(private val sdkProvider: SdkProvider) : SuspendingCliktCommand("current-pairs") {

    private val context by requireObject<PartyContext>()
    private val partyId: PartyId by requireObject<PartyId>("partyId")

    override suspend fun run() {
        withSdk(env = context.env, echo = ::echo, sdkProvider = sdkProvider) { sdk ->
            val pairingSet = sdk.fire(GqlQuery(CurrentPairAssignmentsQuery(partyId)))
                ?.party
                ?.currentPairingSet
                ?.pairingSetDetails
                ?.toDomain()
            if (pairingSet == null) {
                throw CliktError("Party not found.", printError = true)
            }
            val pairs = pairingSet.pairs.map { pair ->
                pair.callSign().toString() to pair.players.map { it.name }.toList().joinToString(" & ")
            }.toList()
            echo(formatCurrentPairs(partyId, pairs))
        }
    }
}
