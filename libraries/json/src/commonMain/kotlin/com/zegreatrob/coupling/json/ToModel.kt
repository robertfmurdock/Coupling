package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.defaultParty

fun SavePartyInput.toModel() = PartyDetails(
    id = partyId,
    pairingRule = PairingRule.fromValue(pairingRule),
    badgesEnabled = badgesEnabled ?: defaultParty.badgesEnabled,
    defaultBadgeName = defaultBadgeName ?: defaultParty.defaultBadgeName,
    alternateBadgeName = alternateBadgeName ?: defaultParty.alternateBadgeName,
    email = email,
    name = name,
    callSignsEnabled = callSignsEnabled ?: defaultParty.callSignsEnabled,
    animationEnabled = animationsEnabled ?: defaultParty.animationEnabled,
    animationSpeed = animationSpeed ?: defaultParty.animationSpeed,
)
