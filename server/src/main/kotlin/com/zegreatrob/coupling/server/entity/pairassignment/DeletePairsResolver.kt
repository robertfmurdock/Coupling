package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.DeletePairAssignmentsInput
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.server.action.pairassignmentdocument.DeletePairAssignmentDocumentCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.partyCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val deletePairsResolver = dispatch(
    partyCommand,
    { _, entity: DeletePairAssignmentsInput ->
        DeletePairAssignmentDocumentCommand(entity.pairAssignmentsId.let(::PairAssignmentDocumentId))
    },
    { true }
)
