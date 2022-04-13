package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.PairAssignmentAdjustmentMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPairAssignmentDocumentSaveSyntax
import com.zegreatrob.coupling.server.action.BroadcastAction
import com.zegreatrob.coupling.server.action.BroadcastActionDispatcher
import com.zegreatrob.coupling.server.action.connection.CouplingConnectionGetSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentTribeIdSyntax
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax

data class SavePairAssignmentDocumentCommand(val pairAssignmentDocument: PairAssignmentDocument) :
    SimpleSuspendResultAction<SavePairAssignmentDocumentCommandDispatcher, PartyElement<PairAssignmentDocument>> {
    override val performFunc = link(SavePairAssignmentDocumentCommandDispatcher::perform)
}

interface SavePairAssignmentDocumentCommandDispatcher : PartyIdPairAssignmentDocumentSaveSyntax, CurrentTribeIdSyntax,
    CouplingConnectionGetSyntax, SuspendActionExecuteSyntax, BroadcastActionDispatcher {
    suspend fun perform(command: SavePairAssignmentDocumentCommand) = with(command) {
        currentPartyId.with(pairAssignmentDocument)
            .apply { save() }
            .apply { execute(broadcastAction()) }
            .successResult()
    }

    suspend fun SavePairAssignmentDocumentCommand.broadcastAction() = BroadcastAction(
        currentPartyId.loadConnections(),
        PairAssignmentAdjustmentMessage(pairAssignmentDocument)
    )
}
