package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentDocumentCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentDocumentCommandDispatcher
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.PairAssignmentAdjustmentMessage
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPairAssignmentDocumentSaveSyntax
import com.zegreatrob.coupling.server.action.BroadcastAction
import com.zegreatrob.coupling.server.action.BroadcastActionDispatcher
import com.zegreatrob.coupling.server.action.connection.CouplingConnectionGetSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax

interface ServerSavePairAssignmentDocumentCommandDispatcher :
    SavePairAssignmentDocumentCommandDispatcher,
    PartyIdPairAssignmentDocumentSaveSyntax,
    CurrentPartyIdSyntax,
    CouplingConnectionGetSyntax,
    SuspendActionExecuteSyntax,
    BroadcastActionDispatcher {

    override suspend fun perform(command: SavePairAssignmentDocumentCommand) = with(command) {
        currentPartyId.with(pairAssignmentDocument)
            .apply { save() }
            .apply { execute(broadcastAction()) }
            .successResult()
    }

    suspend fun SavePairAssignmentDocumentCommand.broadcastAction() = BroadcastAction(
        currentPartyId.loadConnections(),
        PairAssignmentAdjustmentMessage(pairAssignmentDocument),
    )
}
