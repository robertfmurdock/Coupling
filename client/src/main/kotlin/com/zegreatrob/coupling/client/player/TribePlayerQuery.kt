package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.action.entity.player.callsign.FindCallSignAction
import com.zegreatrob.coupling.action.entity.player.callsign.FindCallSignActionDispatcher
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.callsign.CallSign
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.player.PartyPlayersSyntax
import com.zegreatrob.coupling.repository.party.PartyIdGetSyntax
import com.zegreatrob.testmints.action.ExecutableActionExecuteSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

typealias TribePlayerData = Triple<Party, List<Player>, Player>

data class TribePlayerQuery(val tribeId: PartyId, val playerId: String?) :
    SimpleSuspendAction<PartyPlayerQueryDispatcher, TribePlayerData?> {
    override val performFunc = link(PartyPlayerQueryDispatcher::perform)
}

interface PartyPlayerQueryDispatcher : PartyIdGetSyntax,
    PartyPlayersSyntax,
    FindCallSignActionDispatcher,
    ExecutableActionExecuteSyntax {
    suspend fun perform(query: TribePlayerQuery) = query.get()

    private suspend fun TribePlayerQuery.get() = tribeId.getData()
        .let { (tribe, players) ->
            if (tribe == null)
                null
            else
                Triple(
                    tribe,
                    players,
                    players.findOrDefaultNew(playerId)
                )
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
        callSignNoun = callSign.noun
    )
}
