package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.defaultParty

fun SaveTribeInput.toModel() = Party(
    id = tribeId,
    name = name,
    email = email,
    pairingRule = PairingRule.fromValue(pairingRule),
    defaultBadgeName = defaultBadgeName ?: defaultParty.defaultBadgeName,
    alternateBadgeName = alternateBadgeName ?: defaultParty.alternateBadgeName,
    badgesEnabled = badgesEnabled ?: defaultParty.badgesEnabled,
    callSignsEnabled = callSignsEnabled ?: defaultParty.callSignsEnabled,
    animationEnabled = animationsEnabled ?: defaultParty.animationEnabled,
    animationSpeed = animationSpeed ?: defaultParty.animationSpeed
)
