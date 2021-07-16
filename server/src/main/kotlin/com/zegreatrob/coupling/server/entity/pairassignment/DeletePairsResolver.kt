package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.server.action.pairassignmentdocument.DeletePairAssignmentDocumentCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.Serializable

val deletePairsResolver = dispatch(
    tribeCommand,
    { _, entity: DeletePairAssignmentsInput ->
        DeletePairAssignmentDocumentCommand(PairAssignmentDocumentId(entity.pairAssignmentsId))
    },
    { true }
)

@Serializable
data class DeletePairAssignmentsInput(val pairAssignmentsId: String)