package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentIdDeleteSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocumentId
import com.zegreatrob.coupling.model.tribe.TribeId

data class DeletePairAssignmentDocumentCommand(val tribeId: TribeId, val id: PairAssignmentDocumentId) : Action

interface DeletePairAssignmentDocumentCommandDispatcher : ActionLoggingSyntax, PairAssignmentDocumentIdDeleteSyntax {

    suspend fun DeletePairAssignmentDocumentCommand.perform() = logAsync {
        TribeIdPairAssignmentDocumentId(tribeId, id).delete()
    }

}

