package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.actionFunc.DispatchSyntax
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdHistorySyntax
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class ProposeNewPairsCommand(
    val tribeId: TribeId,
    val players: List<Player>,
    val pins: List<Pin>
) : SimpleSuspendResultAction<ProposeNewPairsCommandDispatcher, PairAssignmentDocument> {
    override val performFunc = link(ProposeNewPairsCommandDispatcher::perform)
}

interface ProposeNewPairsCommandDispatcher : DispatchSyntax, RunGameActionDispatcher, TribeIdGetSyntax,
    TribeIdHistorySyntax {

    suspend fun perform(command: ProposeNewPairsCommand) = command.runGame().successResult()

    private suspend fun ProposeNewPairsCommand.runGame() = loadData()
        .let { (history, tribe) -> execute(RunGameAction(players, pins, history, tribe)) }

    private suspend fun ProposeNewPairsCommand.loadData() = coroutineScope {
        await(
            async { tribeId.loadHistory() },
            async { tribeId.get()!! }
        )
    }

}
