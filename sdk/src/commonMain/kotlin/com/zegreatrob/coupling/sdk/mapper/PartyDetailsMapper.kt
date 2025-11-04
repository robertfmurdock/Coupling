package com.zegreatrob.coupling.sdk.mapper

import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.sdk.schema.fragment.PartyDetails
import org.kotools.types.ExperimentalKotoolsTypesApi

@OptIn(ExperimentalKotoolsTypesApi::class)
fun PartyDetails.toDomain() = com.zegreatrob.coupling.model.party.PartyDetails(
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
