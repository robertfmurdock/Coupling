package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.perform
import com.zegreatrob.coupling.json.SavePairAssignmentsInput
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val savePairsResolver = dispatch(
    dispatcherFunc = { request, _: JsonNull, args ->
        authorizedPartyDispatcher(
            context = request,
            partyId = args.partyId.value,
        )
    },
    commandFunc = { _, input: SavePairAssignmentsInput -> input.command() },
    fireFunc = ::perform,
    toSerializable = { true },
)

private fun SavePairAssignmentsInput.command() = SavePairAssignmentsCommand(partyId, toModel())
