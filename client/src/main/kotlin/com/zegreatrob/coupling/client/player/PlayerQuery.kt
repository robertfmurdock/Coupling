package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.Coupling
import com.zegreatrob.coupling.client.tribe.GetTribeSyntax
import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.player.callsign.FindCallSignAction
import com.zegreatrob.coupling.common.entity.player.callsign.FindCallSignActionDispatcher
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.Deferred

data class PlayerQuery(val tribeId: TribeId, val playerId: String?, val coupling: Coupling) : Action

interface PlayerQueryDispatcher : ActionLoggingSyntax, GetTribeSyntax, GetPlayerListSyntax,
        FindCallSignActionDispatcher {
    suspend fun PlayerQuery.perform() = logAsync {
        getData(tribeId)
                .let { (tribe, players) ->
                    Triple(
                            tribe,
                            players,
                            players.findOrDefaultNew(playerId)
                    )
                }
    }

    private suspend fun getData(tribeId: TribeId) = (getTribeAsync(tribeId) to getPlayerListAsync(tribeId))
            .await()

    private suspend fun Pair<Deferred<KtTribe>, Deferred<List<Player>>>.await() =
            first.await() to second.await()

    private fun List<Player>.findOrDefaultNew(playerId: String?) = firstOrNull { it.id == playerId }
            ?: defaultWithCallSign(this)

    private fun defaultWithCallSign(players: List<Player>) = FindCallSignAction(players, "")
            .perform()
            .let { callSign ->
                Player(
                        callSignAdjective = callSign.adjective,
                        callSignNoun = callSign.noun
                )
            }
}