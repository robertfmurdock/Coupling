package com.zegreatrob.coupling.server.entity.pairassignmentdocument

import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId

data class DeletePairAssignmentDocumentCommand(val id: PairAssignmentDocumentId) : Action

interface DeletePairAssignmentDocumentCommandDispatcher : ActionLoggingSyntax, PairAssignmentDocumentIdDeleteSyntax {

    suspend fun DeletePairAssignmentDocumentCommand.perform() = logAsync { id.delete() }

}

interface PairAssignmentDocumentIdDeleteSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentDeleter

    suspend fun PairAssignmentDocumentId.delete() = pairAssignmentDocumentRepository.delete(this)
}
