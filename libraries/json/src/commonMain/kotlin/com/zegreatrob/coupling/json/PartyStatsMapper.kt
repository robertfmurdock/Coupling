package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.PartyStats
import com.zegreatrob.coupling.model.party.PartyId
import kotools.types.text.NotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

@OptIn(ExperimentalKotoolsTypesApi::class)
fun GqlPartyStats.toModel() = PartyStats(
    name = name,
    id = PartyId(NotBlankString.create(id)),
    playerCount = playerCount,
    spins = spins,
    medianSpinDuration = medianSpinDurationMillis?.milliseconds,
    appliedPinCount = appliedPinCount,
    uniquePinCount = uniquePinCount,
)

fun PartyStats.toJson() = GqlPartyStats(
    name = name,
    id = id.value,
    playerCount = playerCount,
    appliedPinCount = appliedPinCount,
    uniquePinCount = uniquePinCount,
    spins = spins,
    medianSpinDuration = medianSpinDuration,
    medianSpinDurationMillis = medianSpinDuration?.toDouble(DurationUnit.MILLISECONDS),
)
