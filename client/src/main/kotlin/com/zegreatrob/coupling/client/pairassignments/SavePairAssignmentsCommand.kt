package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.sdk.PairAssignmentSaveSyntax
import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.TribeId

data class SavePairAssignmentsCommand(val tribeId: TribeId, val pairAssignments: PairAssignmentDocument) : Action

interface SavePairAssignmentsCommandDispatcher : ActionLoggingSyntax, PairAssignmentSaveSyntax {
    suspend fun SavePairAssignmentsCommand.perform() = logAsync { saveAsync(tribeId, pairAssignments) }
}
