package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocumentId
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentIdDeleteSyntax

data class DeletePairAssignmentsCommand(val tribeId: TribeId, val pairAssignmentDocumentId: PairAssignmentDocumentId) :
    Action

interface DeletePairAssignmentsCommandDispatcher : ActionLoggingSyntax, PairAssignmentDocumentIdDeleteSyntax {
    suspend fun DeletePairAssignmentsCommand.perform() = logAsync {
        TribeIdPairAssignmentDocumentId(tribeId, pairAssignmentDocumentId).delete()
    }
}