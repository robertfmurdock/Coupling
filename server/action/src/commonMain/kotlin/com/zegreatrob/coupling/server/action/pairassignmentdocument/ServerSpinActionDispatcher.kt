package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdHistorySyntax
import com.zegreatrob.coupling.repository.party.PartyIdGetSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax
import com.zegreatrob.testmints.action.ExecutableActionExecuteSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

interface ServerSpinActionDispatcher :
    SpinAction.Dispatcher,
    ExecutableActionExecuteSyntax,
    RunGameAction.Dispatcher,
    PartyIdGetSyntax,
    PartyIdHistorySyntax,
    CurrentPartyIdSyntax {

    override suspend fun perform(action: SpinAction): PairAssignmentDocument? = action.runGame()

    private suspend fun SpinAction.runGame() = loadData()
        ?.let { (history, party) -> execute(RunGameAction(players, pins, history, party)) }

    private suspend fun loadData() = coroutineScope {
        await(
            async { currentPartyId.loadHistory() },
            async { currentPartyId.get() },
        )
    }.let { (history, party) -> if (party == null) null else history to party }
}
