package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Coupling
import com.zegreatrob.coupling.client.player.GetPlayerListSyntax
import com.zegreatrob.coupling.client.spinAsync
import com.zegreatrob.coupling.client.tribe.GetTribeSyntax
import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.await

data class NewPairAssignmentsQuery(val tribeId: TribeId, val coupling: Coupling, val playerIds: List<String>) : Action

interface NewPairAssignmentsQueryDispatcher : ActionLoggingSyntax, GetTribeSyntax, GetPlayerListSyntax {
    suspend fun NewPairAssignmentsQuery.perform() = logAsync {
        getData(tribeId)
                .let { (tribe, players) ->
                    Triple(
                            tribe,
                            players,
                            performSpin(players, tribeId)
                    )
                }
    }

    private suspend fun NewPairAssignmentsQuery.performSpin(players: List<Player>, tribeId: TribeId) =
            coupling.spinAsync(
                    players.filter { playerIds.contains(it.id) },
                    tribeId
            ).await()

    private suspend fun getData(tribeId: TribeId) =
            Pair(getTribeAsync(tribeId), getPlayerListAsync(tribeId))
                    .await()

    private suspend fun Pair<Deferred<KtTribe>, Deferred<List<Player>>>.await() =
            Pair(
                    first.await(),
                    second.await()
            )
}