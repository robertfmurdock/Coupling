package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.model.PairAssignmentAdjustmentMessage
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPairAssignmentDocumentSaveSyntax
import com.zegreatrob.coupling.server.action.BroadcastAction
import com.zegreatrob.coupling.server.action.connection.CouplingConnectionGetSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax

interface ServerSavePairAssignmentDocumentCommandDispatcher :
    SavePairAssignmentsCommand.Dispatcher,
    PartyIdPairAssignmentDocumentSaveSyntax,
    CurrentPartyIdSyntax,
    CouplingConnectionGetSyntax,
    SuspendActionExecuteSyntax,
    BroadcastAction.Dispatcher {

    override suspend fun perform(command: SavePairAssignmentsCommand) = with(command) {
        currentPartyId.with(pairAssignments)
            .apply { save() }
            .apply { execute(broadcastAction()) }
            .let { VoidResult.Accepted }
    }

    suspend fun SavePairAssignmentsCommand.broadcastAction() = BroadcastAction(
        currentPartyId.loadConnections(),
        PairAssignmentAdjustmentMessage(pairAssignments),
    )
}
