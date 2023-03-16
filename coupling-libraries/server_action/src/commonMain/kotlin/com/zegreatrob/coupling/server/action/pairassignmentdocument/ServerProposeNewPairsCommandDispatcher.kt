package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.NotFoundResult
import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdHistorySyntax
import com.zegreatrob.coupling.repository.party.PartyIdGetSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax
import com.zegreatrob.testmints.action.ExecutableActionExecuteSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

interface ServerProposeNewPairsCommandDispatcher :
    ProposeNewPairsCommandDispatcher,
    ExecutableActionExecuteSyntax,
    RunGameActionDispatcher,
    PartyIdGetSyntax,
    PartyIdHistorySyntax,
    CurrentPartyIdSyntax {

    override suspend fun perform(command: ProposeNewPairsCommand): Result<PairAssignmentDocument> = command.runGame()
        ?.successResult()
        ?: NotFoundResult("Party")

    private suspend fun ProposeNewPairsCommand.runGame() = loadData()
        ?.let { (history, party) -> execute(RunGameAction(players, pins, history, party)) }

    private suspend fun loadData() = coroutineScope {
        await(
            async { currentPartyId.loadHistory() },
            async { currentPartyId.get() },
        )
    }.let { (history, party) -> if (party == null) null else history to party }
}
