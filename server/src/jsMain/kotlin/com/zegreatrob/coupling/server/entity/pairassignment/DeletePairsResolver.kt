package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.perform
import com.zegreatrob.coupling.json.GqlDeletePairAssignmentsInput
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull
import kotools.types.text.toNotBlankString

val deletePairsResolver = dispatch(
    dispatcherFunc = requiredInput { request, _, args ->
        authorizedPartyDispatcher(
            request,
            PartyId(args.partyId),
        )
    },
    commandFunc = requiredInput { _: JsonNull, entity: GqlDeletePairAssignmentsInput -> entity.toModel() },
    fireFunc = ::perform,
    toSerializable = { true },
)

private fun GqlDeletePairAssignmentsInput.toModel(): DeletePairAssignmentsCommand? {
    return DeletePairAssignmentsCommand(
        partyId = PartyId(partyId),
        pairAssignmentDocumentId = PairAssignmentDocumentId(
            pairAssignmentsId.toNotBlankString().getOrNull() ?: return null,
        ),
    )
}
