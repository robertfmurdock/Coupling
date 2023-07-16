package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.party.PartyId
import kotlin.time.Duration

data class GlobalStats(
    val parties: List<PartyStats>,
    val totalParties: Int,
    val totalSpins: Int,
    val totalPlayers: Int,
    val totalAppliedPins: Int,
    val totalUniquePins: Int,
)

data class PartyStats(
    val name: String,
    val id: PartyId,
    val playerCount: Int,
    val spins: Int,
    val medianSpinDuration: Duration?,
    val appliedPinCount: Int,
    val uniquePinCount: Int,
)
