package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.perform
import com.zegreatrob.coupling.json.GqlSavePairAssignmentsInput
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val savePairsResolver = dispatch(
    dispatcherFunc = requiredInput { request, _, args ->
        authorizedPartyDispatcher(
            context = request,
            partyId = PartyId(args.partyId),
        )
    },
    commandFunc = requiredInput { _: JsonNull, input: GqlSavePairAssignmentsInput -> input.command() },
    fireFunc = ::perform,
    toSerializable = { true },
)

private fun GqlSavePairAssignmentsInput.command() = SavePairAssignmentsCommand(PartyId(partyId), toModel())
