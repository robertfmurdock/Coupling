package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.server.action.pairassignmentdocument.DeletePairAssignmentDocumentCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import com.zegreatrob.minjson.at

val deletePairsResolver = dispatch(
    tribeCommand,
    { _, entity ->
        PairAssignmentDocumentId(entity.at<String>("/input/pairAssignmentsId") ?: "")
            .let(::DeletePairAssignmentDocumentCommand)
    },
    { true }
)
