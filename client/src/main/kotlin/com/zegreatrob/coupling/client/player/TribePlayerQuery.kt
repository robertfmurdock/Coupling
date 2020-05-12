package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.action.SimpleSuspendAction
import com.zegreatrob.coupling.action.entity.player.callsign.FindCallSignAction
import com.zegreatrob.coupling.action.entity.player.callsign.FindCallSignActionDispatcher
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.player.TribeIdPlayersSyntax
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class TribePlayerQuery(val tribeId: TribeId, val playerId: String?) :
    SimpleSuspendAction<TribePlayerQuery, TribePlayerQueryDispatcher, Triple<Tribe?, List<Player>, Player>> {
    override val perform = link(TribePlayerQueryDispatcher::perform)
}

interface TribePlayerQueryDispatcher : TribeIdGetSyntax,
    TribeIdPlayersSyntax,
    FindCallSignActionDispatcher {
    suspend fun perform(query: TribePlayerQuery) = query.sdkfj().successResult()

    private suspend fun TribePlayerQuery.sdkfj() = tribeId.getData()
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

    private fun List<Player>.defaultWithCallSign() = FindCallSignAction(this, "")
        .perform()
        .let { callSign ->
            Player(
                callSignAdjective = callSign.adjective,
                callSignNoun = callSign.noun
            )
        }
}