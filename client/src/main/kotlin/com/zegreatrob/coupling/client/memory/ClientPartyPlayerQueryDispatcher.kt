package com.zegreatrob.coupling.client.memory

import com.zegreatrob.coupling.action.player.callsign.FindCallSignAction
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.callsign.CallSign
import com.zegreatrob.coupling.sdk.PartyPlayerQuery
import com.zegreatrob.coupling.sdk.SdkProviderSyntax
import com.zegreatrob.coupling.sdk.gql.graphQuery

interface ClientPartyPlayerQueryDispatcher :
    SdkProviderSyntax,
    FindCallSignAction.Dispatcher,
    PartyPlayerQuery.Dispatcher {
    override suspend fun perform(query: PartyPlayerQuery) = query.get()

    private suspend fun PartyPlayerQuery.get() = partyId.getData()
        ?.let { (party, players) ->
            if (party == null) {
                null
            } else {
                Triple(
                    party,
                    players,
                    players.findOrDefaultNew(playerId),
                )
            }
        }

    private suspend fun PartyId.getData() = sdk.perform(
        graphQuery {
            party(this@getData) {
                party()
                playerList()
            }
        },
    )?.party
        ?.let { it.details?.data to (it.playerList?.elements ?: emptyList()) }

    private fun List<Player>.findOrDefaultNew(playerId: String?) = firstOrNull { it.id == playerId }
        ?: defaultWithCallSign()

    private fun List<Player>.defaultWithCallSign() = execute(FindCallSignAction(this, ""))
        .let(::defaultPlayer)

    private fun defaultPlayer(callSign: CallSign) = Player(
        callSignAdjective = callSign.adjective,
        callSignNoun = callSign.noun,
        avatarType = null,
    )
}
