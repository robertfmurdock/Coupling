package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SaveSlackIntegrationCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.json.SaveSlackIntegrationInput
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.entity.toJson
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatchAction
import kotlinx.serialization.json.JsonNull

val saveSlackIntegrationResolver = dispatchAction(
    dispatcherFunc = { request, _: JsonNull, args -> authorizedPartyDispatcher(request, args.partyId) },
    commandFunc = { _, input: SaveSlackIntegrationInput -> input.toCommand() },
    fireCommand = ::fire,
    toSerializable = VoidResult::toJson,
)

private fun SaveSlackIntegrationInput.toCommand() = SaveSlackIntegrationCommand(
    partyId = PartyId(partyId),
    team = team,
    channel = channel,
)
