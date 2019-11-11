package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.client.pairassignments.spin.RequestSpinAction
import com.zegreatrob.coupling.client.pairassignments.spin.RequestSpinActionDispatcher
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayersSyntax
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.TribeIdGetSyntax
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

data class NewPairAssignmentsQuery(val tribeId: TribeId, val playerIds: List<String>) : Action

interface NewPairAssignmentsQueryDispatcher : ActionLoggingSyntax, TribeIdGetSyntax, TribeIdPlayersSyntax,
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

    private suspend fun TribeId.getData() = Pair(GlobalScope.async { load() }, GlobalScope.async { loadPlayers() })
        .await()

    private suspend fun Pair<Deferred<KtTribe?>, Deferred<List<Player>>>.await() =
        Pair(
            first.await(),
            second.await()
        )
}