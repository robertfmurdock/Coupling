package com.zegreatrob.coupling.model

data class GlobalStats(
    val parties: List<PartyStats>,
    val totalParties: Int,
    val totalSpins: Int,
    val totalPlayers: Int,
    val totalAppliedPins: Int,
    val totalUniquePins: Int,
)
