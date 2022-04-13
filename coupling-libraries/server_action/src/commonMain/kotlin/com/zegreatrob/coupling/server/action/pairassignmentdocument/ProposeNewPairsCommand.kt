package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.NotFoundResult
import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdHistorySyntax
import com.zegreatrob.coupling.repository.party.PartyIdGetSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax
import com.zegreatrob.testmints.action.ExecutableActionExecuteSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class ProposeNewPairsCommand(val players: List<Player>, val pins: List<Pin>) :
    SimpleSuspendResultAction<ProposeNewPairsCommandDispatcher, PairAssignmentDocument> {
    override val performFunc = link(ProposeNewPairsCommandDispatcher::perform)
}

interface ProposeNewPairsCommandDispatcher : ExecutableActionExecuteSyntax, RunGameActionDispatcher,
    PartyIdGetSyntax, PartyIdHistorySyntax, CurrentPartyIdSyntax {

    suspend fun perform(command: ProposeNewPairsCommand): Result<PairAssignmentDocument> = command.runGame()
        ?.successResult()
        ?: NotFoundResult("Party")

    private suspend fun ProposeNewPairsCommand.runGame() = loadData()
        ?.let { (history, party) -> execute(RunGameAction(players, pins, history, party)) }

    private suspend fun loadData() = coroutineScope {
        await(
            async { currentPartyId.loadHistory() },
            async { currentPartyId.get() }
        )
    }.let { (history, party) -> if (party == null) null else history to party }

}
