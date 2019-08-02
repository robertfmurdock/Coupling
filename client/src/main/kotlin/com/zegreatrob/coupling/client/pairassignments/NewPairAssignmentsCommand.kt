package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Coupling
import com.zegreatrob.coupling.client.getPlayerListAsync
import com.zegreatrob.coupling.client.getTribeAsync
import com.zegreatrob.coupling.client.spinAsync
import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.await
import kotlin.js.Promise

data class NewPairAssignmentsCommand(val tribeId: TribeId, val coupling: Coupling, val playerIds: List<String>) : Action

interface NewPairAssignmentsCommandDispatcher : ActionLoggingSyntax {
    suspend fun NewPairAssignmentsCommand.perform() = logAsync {
        coupling.getData(tribeId)
                .let { (tribe, players) ->
                    Triple(
                            tribe,
                            players,
                            performSpin(players, tribeId)
                    )
                }
    }

    private suspend fun NewPairAssignmentsCommand.performSpin(players: List<Player>, tribeId: TribeId) =
            coupling.spinAsync(
                    players.filter { playerIds.contains(it.id) },
                    tribeId
            ).await()

    private suspend fun Coupling.getData(tribeId: TribeId) =
            Pair(getTribeAsync(tribeId), getPlayerListAsync(tribeId))
                    .await()

    private suspend fun Pair<Promise<KtTribe>, Promise<List<Player>>>.await() =
            Pair(
                    first.await(),
                    second.await()
            )
}