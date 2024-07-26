package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.GlobalStats
import com.zegreatrob.coupling.model.PartyStats

fun GqlGlobalStats.toModel() = GlobalStats(
    parties = parties.map(GqlPartyStats::toModel),
    totalSpins = totalSpins,
    totalPlayers = totalPlayers,
    totalAppliedPins = totalAppliedPins,
    totalUniquePins = totalUniquePins,
    totalParties = totalParties,
)

fun GlobalStats.toJson() = GqlGlobalStats(
    parties = parties.map(PartyStats::toJson),
    totalSpins = totalSpins,
    totalPlayers = totalPlayers,
    totalAppliedPins = totalAppliedPins,
    totalUniquePins = totalUniquePins,
    totalParties = totalParties,
)
