package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.json.GqlAccessType
import com.zegreatrob.coupling.json.GqlParty
import com.zegreatrob.coupling.json.GqlPartyDetails
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.server.action.party.PartyListQuery
import com.zegreatrob.coupling.server.action.party.PartyListResult
import com.zegreatrob.coupling.server.action.party.perform
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val partyListResolve = dispatch(
    dispatcherFunc = command(),
    commandFunc = { _: JsonNull, _: JsonNull? -> PartyListQuery },
    fireFunc = ::perform,
    toSerializable = ::toJson,
)

private fun toJson(records: PartyListResult) = records.ownedParties
    .map(Record<PartyDetails>::toSerializable)
    .map { gqlParty(it, GqlAccessType.Owner) } + records.playerParties.map(Record<PartyDetails>::toSerializable)
    .map { gqlParty(it, GqlAccessType.Player) }

private fun gqlParty(details: GqlPartyDetails, accessType: GqlAccessType): GqlParty = GqlParty(
    id = details.id,
    accessType = accessType,
    details = details,
    boost = null,
    contributionReport = null,
    currentPairAssignmentDocument = null,
    integration = null,
    medianSpinDuration = null,
    pair = null,
    pairAssignmentDocumentList = null,
    pairs = null,
    pinList = null,
    playerList = null,
    retiredPlayers = null,
    secretList = null,
    spinsUntilFullRotation = null,
)
