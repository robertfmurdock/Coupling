package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.sdk.schema.fragment.PartyDetailsFragment
import org.kotools.types.ExperimentalKotoolsTypesApi

@OptIn(ExperimentalKotoolsTypesApi::class)
fun PartyDetailsFragment.toModel(): PartyDetails = PartyDetails(
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
)
