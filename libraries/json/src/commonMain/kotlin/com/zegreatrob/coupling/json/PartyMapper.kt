package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Party
import com.zegreatrob.coupling.model.party.PartyId

fun GqlParty.toModel() = Party(
    id = id.let(::PartyId),
    details = details?.toModelRecord(),
    integration = integration?.toModelRecord(),
    pinList = pinList?.map(GqlPinDetails::toModel),
    playerList = playerList?.map(GqlPlayerDetails::toModel),
    retiredPlayers = retiredPlayers?.map(GqlPlayerDetails::toModel),
    secretList = secretList?.map(GqlPartySecret::toModel),
    pairAssignmentDocumentList = pairAssignmentDocumentList?.map(GqlPairAssignmentDocumentDetails::toModel),
    currentPairAssignmentDocument = currentPairAssignmentDocument?.toModel(),
    boost = boost?.toModelRecord(),
    pairs = pairs?.map(GqlPair::toModel),
    pair = pair?.let(GqlPair::toModel),
    contributionReport = contributionReport?.toModel(),
    medianSpinDuration = medianSpinDuration,
    spinsUntilFullRotation = spinsUntilFullRotation,
)
