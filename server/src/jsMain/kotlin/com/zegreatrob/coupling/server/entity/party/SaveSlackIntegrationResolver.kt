package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SaveSlackIntegrationCommand
import com.zegreatrob.coupling.action.party.perform
import com.zegreatrob.coupling.json.SaveSlackIntegrationInput
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.entity.toJson
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val saveSlackIntegrationResolver = dispatch(
    dispatcherFunc = requiredInput { request, _, args -> authorizedPartyDispatcher(request, args.partyId) },
    commandFunc = requiredInput { _: JsonNull, input: SaveSlackIntegrationInput -> input.toCommand() },
    fireFunc = ::perform,
    toSerializable = VoidResult::toJson,
)

private fun SaveSlackIntegrationInput.toCommand() = SaveSlackIntegrationCommand(
    partyId = PartyId(partyId),
    team = team,
    channel = channel,
)
