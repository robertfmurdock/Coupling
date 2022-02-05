package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.defaultTribe

fun SaveTribeInput.toModel() = Tribe(
    id = tribeId,
    name = name,
    email = email,
    pairingRule = PairingRule.fromValue(pairingRule),
    defaultBadgeName = defaultBadgeName ?: defaultTribe.defaultBadgeName,
    alternateBadgeName = alternateBadgeName ?: defaultTribe.alternateBadgeName,
    badgesEnabled = badgesEnabled ?: defaultTribe.badgesEnabled,
    callSignsEnabled = callSignsEnabled ?: defaultTribe.callSignsEnabled,
    animationEnabled = animationsEnabled ?: defaultTribe.animationEnabled,
    animationSpeed = animationSpeed ?: defaultTribe.animationSpeed
)
