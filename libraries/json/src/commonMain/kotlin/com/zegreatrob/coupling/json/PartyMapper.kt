package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.AccessType
import com.zegreatrob.coupling.model.Party
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails

fun GqlParty.toModel() = Party(
    id = id,
    accessType = accessType.toModel(),
    details = Record(
        data = PartyDetails(
            id = id,
            pairingRule = PairingRule.fromValue(pairingRule),
            defaultBadgeName = defaultBadgeName,
            alternateBadgeName = alternateBadgeName,
            email = email,
            name = name,
            badgesEnabled = badgesEnabled == true,
            callSignsEnabled = callSignsEnabled == true,
            animationEnabled = animationsEnabled != false,
            animationSpeed = animationSpeed ?: 1.0,
        ),
        modifyingUserId = modifyingUserEmail,
        isDeleted = isDeleted,
        timestamp = timestamp,
    ),
    integration = integration?.toModelRecord(),
    pinList = pinList.map(GqlPinDetails::toModel),
    playerList = playerList.map(GqlPlayerDetails::toModel),
    retiredPlayers = retiredPlayers.map(GqlPlayerDetails::toModel),
    secretList = secretList.map(GqlPartySecret::toModel),
    pairAssignmentDocumentList = pairAssignmentDocumentList.map(GqlPairAssignmentDocumentDetails::toModel),
    currentPairAssignmentDocument = currentPairAssignmentDocument?.toModel(),
    boost = boost?.toModelRecord(),
    pairs = pairs.map(GqlPair::toModel),
    pair = pair?.let(GqlPair::toModel),
    contributionReport = contributionReport?.toModel(),
    medianSpinDuration = medianSpinDuration,
    spinsUntilFullRotation = spinsUntilFullRotation,
)

fun GqlAccessType.toModel() = when (this) {
    GqlAccessType.Owner -> AccessType.Owner
    GqlAccessType.Player -> AccessType.Player
}
