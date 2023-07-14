package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.fire
import com.zegreatrob.coupling.json.DeletePairAssignmentsInput
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatchAction
import kotlinx.serialization.json.JsonNull

val deletePairsResolver = dispatchAction(
    dispatcherFunc = { request, _: JsonNull, args -> authorizedPartyDispatcher(request, args.partyId.value) },
    commandFunc = { _, entity: DeletePairAssignmentsInput -> entity.toModel() },
    fireFunc = ::fire,
    toSerializable = { true },
)

private fun DeletePairAssignmentsInput.toModel() = DeletePairAssignmentsCommand(
    partyId = partyId,
    pairAssignmentDocumentId = pairAssignmentsId.let(::PairAssignmentDocumentId),
)
