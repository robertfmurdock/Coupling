package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.server.action.pairassignmentdocument.DeletePairAssignmentDocumentCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import com.zegreatrob.minjson.at
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromDynamic

val deletePairsResolver = dispatch(
    tribeCommand,
    { _, entity ->
        couplingJsonFormat.decodeFromDynamic<DeletePairAssignmentsInput>(entity.at("/input"))
            .let { PairAssignmentDocumentId(it.pairAssignmentsId) }
            .let(::DeletePairAssignmentDocumentCommand)
    },
    { true }
)

@Serializable
data class DeletePairAssignmentsInput(val pairAssignmentsId: String)