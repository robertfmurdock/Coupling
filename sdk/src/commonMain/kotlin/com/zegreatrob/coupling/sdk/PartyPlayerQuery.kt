package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.player.callsign.FindCallSignAction
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.callsign.CallSign
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.party.PartyIdGetSyntax
import com.zegreatrob.coupling.repository.player.PartyPlayersSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

typealias PartyPlayerData = Triple<Party, List<Player>, Player>

data class PartyPlayerQuery(val partyId: PartyId, val playerId: String?) :
    SimpleSuspendAction<PartyPlayerQueryDispatcher, PartyPlayerData?> {
    override val performFunc = link(PartyPlayerQueryDispatcher::perform)
}

interface PartyPlayerQueryDispatcher :
    PartyIdGetSyntax,
    PartyPlayersSyntax,
    FindCallSignAction.Dispatcher {
    suspend fun perform(query: PartyPlayerQuery) = query.get()

    private suspend fun PartyPlayerQuery.get() = partyId.getData()
        .let { (party, players) ->
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

    private suspend fun PartyId.getData() = coroutineScope {
        await(async { get() }, async { getPlayerList() })
    }

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
