package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.perform
import com.zegreatrob.coupling.json.DeletePairAssignmentsInput
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.server.CurrentPartyDispatcher
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val deletePairsResolver =
    dispatch<JsonNull, DeletePairAssignmentsInput, CurrentPartyDispatcher, DeletePairAssignmentsCommand, VoidResult, Boolean>(
        dispatcherFunc = requiredInput { request, _: JsonNull, args ->
            authorizedPartyDispatcher(
                request,
                args.partyId.value,
            )
        },
        commandFunc = requiredInput { _, entity: DeletePairAssignmentsInput -> entity.toModel() },
        fireFunc = ::perform,
        toSerializable = { true },
    )

private fun DeletePairAssignmentsInput.toModel() = DeletePairAssignmentsCommand(
    partyId = partyId,
    pairAssignmentDocumentId = pairAssignmentsId.let(::PairAssignmentDocumentId),
)
