package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Coupling
import com.zegreatrob.coupling.client.player.GetPlayerListSyntax
import com.zegreatrob.coupling.client.tribe.GetTribeSyntax
import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.Deferred

data class NewPairAssignmentsQuery(val tribeId: TribeId, val coupling: Coupling, val playerIds: List<String>) : Action

interface NewPairAssignmentsQueryDispatcher : ActionLoggingSyntax, GetTribeSyntax, GetPlayerListSyntax, RequestSpinActionDispatcher {
    suspend fun NewPairAssignmentsQuery.perform() = logAsync {
        tribeId.getData()
                .let { (tribe, players) ->
                    val selectedPlayers = filterSelectedPlayers(players, playerIds)
                    Triple(
                            tribe,
                            players,
                            performSpin(tribeId, selectedPlayers)
                    )
                }
    }

    private suspend fun performSpin(tribeId: TribeId, players: List<Player>) = RequestSpinAction(tribeId, players)
            .perform()

    private fun filterSelectedPlayers(players: List<Player>, playerIds: List<String>) = players.filter {
        playerIds.contains(it.id)
    }

    private suspend fun TribeId.getData() =
            Pair(getTribeAsync(), getPlayerListAsync())
                    .await()

    private suspend fun Pair<Deferred<KtTribe>, Deferred<List<Player>>>.await() =
            Pair(
                    first.await(),
                    second.await()
            )
}