package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.NotFoundResult
import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdHistorySyntax
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentTribeIdSyntax
import com.zegreatrob.testmints.action.ExecutableActionExecuteSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class ProposeNewPairsCommand(val players: List<Player>, val pins: List<Pin>) :
    SimpleSuspendResultAction<ProposeNewPairsCommandDispatcher, PairAssignmentDocument> {
    override val performFunc = link(ProposeNewPairsCommandDispatcher::perform)
}

interface ProposeNewPairsCommandDispatcher : ExecutableActionExecuteSyntax, RunGameActionDispatcher,
    TribeIdGetSyntax, TribeIdHistorySyntax, CurrentTribeIdSyntax {

    suspend fun perform(command: ProposeNewPairsCommand): Result<PairAssignmentDocument> = command.runGame()
        ?.successResult()
        ?: NotFoundResult("Tribe")

    private suspend fun ProposeNewPairsCommand.runGame() = loadData()
        ?.let { (history, tribe) -> execute(RunGameAction(players, pins, history, tribe)) }

    private suspend fun loadData() = coroutineScope {
        await(
            async { currentTribeId.loadHistory() },
            async { currentTribeId.get() }
        )
    }.let { (history, tribe) -> if (tribe == null) null else history to tribe }

}
