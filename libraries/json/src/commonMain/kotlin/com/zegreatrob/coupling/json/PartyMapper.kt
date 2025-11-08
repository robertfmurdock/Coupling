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
            badgesEnabled = badgesEnabled,
            callSignsEnabled = callSignsEnabled,
            animationEnabled = animationsEnabled,
            animationSpeed = animationSpeed,
        ),
        modifyingUserId = modifyingUserEmail,
        isDeleted = isDeleted,
        timestamp = timestamp,
    ),
    integration = integration?.toModelRecord(),
    pinList = pinList.map(GqlPinDetails::toModel),
    playerList = playerList.map(GqlPlayer::toModel),
    retiredPlayers = retiredPlayers.map(GqlPlayer::toModel),
    secretList = secretList.map(GqlPartySecret::toModel),
    pairingSetList = pairingSetList.map(GqlPairingSet::toModel),
    currentPairingSet = currentPairingSet?.toModel(),
    boost = boost?.toModelRecord(),
    pairs = pairList.map(GqlPair::toModel),
    pair = pair?.let(GqlPair::toModel),
    contributionReport = contributionReport?.toModel(),
    medianSpinDuration = medianSpinDuration,
    spinsUntilFullRotation = spinsUntilFullRotation,
)

fun GqlAccessType.toModel() = when (this) {
    GqlAccessType.Owner -> AccessType.Owner
    GqlAccessType.Player -> AccessType.Player
}
