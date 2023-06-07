package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.json.SavePairAssignmentsInput
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val savePairsResolver = dispatch(
    { request, _: JsonNull, args ->
        authorizedDispatcher(
            request = request,
            partyId = args.partyId.value,
        )
    },
    { _, input: SavePairAssignmentsInput -> input.command() },
    { true },
)

private fun SavePairAssignmentsInput.command() = SavePairAssignmentsCommand(partyId, toModel())
