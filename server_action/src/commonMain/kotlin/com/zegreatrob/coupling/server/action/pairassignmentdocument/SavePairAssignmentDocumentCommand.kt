package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.PairAssignmentAdjustmentMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPairAssignmentDocumentSaveSyntax
import com.zegreatrob.coupling.server.action.BroadcastAction
import com.zegreatrob.coupling.server.action.BroadcastActionDispatcher
import com.zegreatrob.coupling.server.action.connection.CouplingConnectionGetSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentTribeIdSyntax
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax

data class SavePairAssignmentDocumentCommand(val pairAssignmentDocument: PairAssignmentDocument) :
    SimpleSuspendResultAction<SavePairAssignmentDocumentCommandDispatcher, TribeIdPairAssignmentDocument> {
    override val performFunc = link(SavePairAssignmentDocumentCommandDispatcher::perform)
}

interface SavePairAssignmentDocumentCommandDispatcher : TribeIdPairAssignmentDocumentSaveSyntax, CurrentTribeIdSyntax,
    CouplingConnectionGetSyntax, SuspendActionExecuteSyntax, BroadcastActionDispatcher {
    suspend fun perform(command: SavePairAssignmentDocumentCommand) = with(command) {
        currentTribeId.with(pairAssignmentDocument)
            .apply { save() }
            .apply { execute(broadcastAction()) }
            .successResult()
    }

    suspend fun SavePairAssignmentDocumentCommand.broadcastAction() = BroadcastAction(
        currentTribeId.loadConnections(),
        PairAssignmentAdjustmentMessage(pairAssignmentDocument)
    )
}
