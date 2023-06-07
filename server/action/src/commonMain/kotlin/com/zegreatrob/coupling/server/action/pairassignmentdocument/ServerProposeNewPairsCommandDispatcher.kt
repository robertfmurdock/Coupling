package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.pairassignmentdocument.ProposeNewPairsCommand
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdHistorySyntax
import com.zegreatrob.coupling.repository.party.PartyIdGetSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax
import com.zegreatrob.testmints.action.ExecutableActionExecuteSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

interface ServerProposeNewPairsCommandDispatcher :
    ProposeNewPairsCommand.Dispatcher,
    ExecutableActionExecuteSyntax,
    RunGameAction.Dispatcher,
    PartyIdGetSyntax,
    PartyIdHistorySyntax,
    CurrentPartyIdSyntax {

    override suspend fun perform(command: ProposeNewPairsCommand): PairAssignmentDocument? = command.runGame()

    private suspend fun ProposeNewPairsCommand.runGame() = loadData()
        ?.let { (history, party) -> execute(RunGameAction(players, pins, history, party)) }

    private suspend fun loadData() = coroutineScope {
        await(
            async { currentPartyId.loadHistory() },
            async { currentPartyId.get() },
        )
    }.let { (history, party) -> if (party == null) null else history to party }
}
