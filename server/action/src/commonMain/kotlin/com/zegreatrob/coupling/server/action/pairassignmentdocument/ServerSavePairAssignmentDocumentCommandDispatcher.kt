package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentDocumentCommand
import com.zegreatrob.coupling.model.PairAssignmentAdjustmentMessage
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPairAssignmentDocumentSaveSyntax
import com.zegreatrob.coupling.server.action.BroadcastAction
import com.zegreatrob.coupling.server.action.connection.CouplingConnectionGetSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax

interface ServerSavePairAssignmentDocumentCommandDispatcher :
    SavePairAssignmentDocumentCommand.Dispatcher,
    PartyIdPairAssignmentDocumentSaveSyntax,
    CurrentPartyIdSyntax,
    CouplingConnectionGetSyntax,
    SuspendActionExecuteSyntax,
    BroadcastAction.Dispatcher {

    override suspend fun perform(command: SavePairAssignmentDocumentCommand) = with(command) {
        currentPartyId.with(pairAssignmentDocument)
            .apply { save() }
            .apply { execute(broadcastAction()) }
            .let { VoidResult.Accepted }
    }

    suspend fun SavePairAssignmentDocumentCommand.broadcastAction() = BroadcastAction(
        currentPartyId.loadConnections(),
        PairAssignmentAdjustmentMessage(pairAssignmentDocument),
    )
}
