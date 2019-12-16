package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.client.pairassignments.spin.RequestSpinAction
import com.zegreatrob.coupling.client.pairassignments.spin.RequestSpinActionDispatcher
import com.zegreatrob.coupling.model.await
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.player.TribeIdPlayersSyntax
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class NewPairAssignmentsQuery(val tribeId: TribeId, val playerIds: List<String>) : Action

interface NewPairAssignmentsQueryDispatcher : ActionLoggingSyntax,
    TribeIdGetSyntax,
    TribeIdPlayersSyntax,
    RequestSpinActionDispatcher {
    suspend fun NewPairAssignmentsQuery.perform() = logAsync {
        val (tribe, players) = tribeId.getData()
        val selectedPlayers = filterSelectedPlayers(players, playerIds)
        Triple(
            tribe,
            players,
            performSpin(tribeId, selectedPlayers)
        )
    }

    private suspend fun performSpin(tribeId: TribeId, players: List<Player>) = RequestSpinAction(tribeId, players)
        .perform()

    private fun filterSelectedPlayers(players: List<Player>, playerIds: List<String>) = players.filter {
        playerIds.contains(it.id)
    }

    private suspend fun TribeId.getData() = coroutineScope {
        await(
            async { load() },
            async { loadPlayers() }
        )
    }

}