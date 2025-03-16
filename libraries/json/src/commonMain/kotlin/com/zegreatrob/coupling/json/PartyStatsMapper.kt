package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.PartyStats
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

fun GqlPartyStats.toModel() = PartyStats(
    name = name,
    id = id,
    playerCount = playerCount,
    spins = spins,
    medianSpinDuration = medianSpinDurationMillis?.milliseconds,
    appliedPinCount = appliedPinCount,
    uniquePinCount = uniquePinCount,
)

fun PartyStats.toJson() = GqlPartyStats(
    name = name,
    id = id,
    playerCount = playerCount,
    appliedPinCount = appliedPinCount,
    uniquePinCount = uniquePinCount,
    spins = spins,
    medianSpinDuration = medianSpinDuration,
    medianSpinDurationMillis = medianSpinDuration?.toDouble(DurationUnit.MILLISECONDS),
)
