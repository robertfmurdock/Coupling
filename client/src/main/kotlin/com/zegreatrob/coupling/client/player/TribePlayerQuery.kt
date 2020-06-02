package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.entity.player.callsign.FindCallSignAction
import com.zegreatrob.coupling.action.entity.player.callsign.FindCallSignActionDispatcher
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.testmints.action.GeneralExecutableActionDispatcherSyntax
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.callsign.CallSign
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.player.TribeIdPlayersSyntax
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class TribePlayerQuery(val tribeId: TribeId, val playerId: String?) :
    SimpleSuspendResultAction<TribePlayerQueryDispatcher, Triple<Tribe?, List<Player>, Player>> {
    override val performFunc = link(TribePlayerQueryDispatcher::perform)
}

interface TribePlayerQueryDispatcher : TribeIdGetSyntax,
    TribeIdPlayersSyntax,
    FindCallSignActionDispatcher,
    GeneralExecutableActionDispatcherSyntax {
    suspend fun perform(query: TribePlayerQuery) = query.get().successResult()

    private suspend fun TribePlayerQuery.get() = tribeId.getData()
        .let { (tribe, players) ->
            Triple(
                tribe,
                players,
                players.findOrDefaultNew(playerId)
            )
        }

    private suspend fun TribeId.getData() = coroutineScope {
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
