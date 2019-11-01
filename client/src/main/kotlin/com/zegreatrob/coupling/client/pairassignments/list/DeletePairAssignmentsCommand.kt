package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.client.pairassignments.PairAssignmentDocDeleteSyntax
import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.tribe.TribeId

data class DeletePairAssignmentsCommand(val tribeId: TribeId, val pairAssignmentDocumentId: PairAssignmentDocumentId) :
    Action

interface DeletePairAssignmentsCommandDispatcher : ActionLoggingSyntax, PairAssignmentDocDeleteSyntax {
    suspend fun DeletePairAssignmentsCommand.perform() = logAsync {
        deleteAsync(tribeId, pairAssignmentDocumentId).await()
    }
}