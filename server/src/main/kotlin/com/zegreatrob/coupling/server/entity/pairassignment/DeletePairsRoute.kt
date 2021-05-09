package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.server.action.pairassignmentdocument.DeletePairAssignmentDocumentCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlin.js.Json

val deletePairsRoute = dispatch(
    tribeCommand,
    { _, entity ->
        val input = entity["input"].unsafeCast<Json>()
        val pairAssignmentsId = input["pairAssignmentsId"].toString().let(::PairAssignmentDocumentId)
        DeletePairAssignmentDocumentCommand(pairAssignmentsId)
    }, { true }
)
