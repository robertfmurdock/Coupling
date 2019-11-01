package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.sdk.PairAssignmentSaveSyntax
import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.TribeId

data class SavePairAssignmentsCommand(val tribeId: TribeId, val pairAssignments: PairAssignmentDocument) : Action

interface SavePairAssignmentsCommandDispatcher : ActionLoggingSyntax, PairAssignmentSaveSyntax {
    suspend fun SavePairAssignmentsCommand.perform() = logAsync { saveAsync(tribeId, pairAssignments) }
}
