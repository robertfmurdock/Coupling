package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentDeleter
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId

data class DeletePairAssignmentDocumentCommand(val id: PairAssignmentDocumentId) : Action

interface DeletePairAssignmentDocumentCommandDispatcher : ActionLoggingSyntax, PairAssignmentDocumentIdDeleteSyntax {

    suspend fun DeletePairAssignmentDocumentCommand.perform() = logAsync { id.delete() }

}

interface PairAssignmentDocumentIdDeleteSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentDeleter

    suspend fun PairAssignmentDocumentId.delete() = pairAssignmentDocumentRepository.delete(this)
}
