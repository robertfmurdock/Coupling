package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.json.DeletePairAssignmentsInput
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val deletePairsResolver = dispatch(
    { request, _: JsonNull, args -> authorizedDispatcher(request, args.partyId.value) },
    { _, entity: DeletePairAssignmentsInput -> entity.toModel() },
    { true },
)

private fun DeletePairAssignmentsInput.toModel() = DeletePairAssignmentsCommand(
    partyId = partyId,
    pairAssignmentDocumentId = pairAssignmentsId.let(::PairAssignmentDocumentId),
)
