package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.action.NotFoundResult
import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.transform
import com.zegreatrob.coupling.client.pairassignments.spin.RequestSpinAction
import com.zegreatrob.coupling.client.pairassignments.spin.RequestSpinActionDispatcher
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPinsSyntax
import com.zegreatrob.coupling.repository.player.TribeIdPlayersSyntax
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

typealias DataPack = Triple<Tribe, List<Player>, PairAssignmentDocument>

data class NewPairAssignmentsQuery(val tribeId: TribeId, val playerIds: List<String>, val pinIds: List<String>) :
    SimpleSuspendResultAction<NewPairAssignmentsQueryDispatcher, DataPack> {
    override val performFunc = link(NewPairAssignmentsQueryDispatcher::perform)
}

interface NewPairAssignmentsQueryDispatcher : TribeIdGetSyntax,
    TribeIdPinsSyntax,
    TribeIdPlayersSyntax,
    SuspendActionExecuteSyntax,
    RequestSpinActionDispatcher {

    suspend fun perform(query: NewPairAssignmentsQuery): Result<DataPack> = with(query) {
        val (tribe, players, pins) = getData()
        if (tribe == null)
            NotFoundResult("Tribe")
        else {
            execute(requestSpinAction(players, pins))
                .transform(queryData(tribe, players))
        }
    }

    private fun queryData(tribe: Tribe, players: List<Player>) = { it: PairAssignmentDocument ->
        Triple(tribe, players, it)
    }

    private fun NewPairAssignmentsQuery.requestSpinAction(players: List<Player>, pins: List<Pin>): RequestSpinAction {
        val selectedPlayers = filterSelectedPlayers(players, playerIds)
        val selectedPins = filterSelectedPins(pins, pinIds)
        return RequestSpinAction(tribeId, selectedPlayers, selectedPins)
    }

    private fun filterSelectedPlayers(players: List<Player>, playerIds: List<String>) = players.filter {
        playerIds.contains(it.id)
    }

    private fun filterSelectedPins(pins: List<Pin>, pinIds: List<String>) = pins.filter { pinIds.contains(it._id) }

    private suspend fun NewPairAssignmentsQuery.getData() = coroutineScope {
        await(
            async { tribeId.get() },
            async { tribeId.getPlayerList() },
            async { tribeId.getPins() }
        )
    }

}
