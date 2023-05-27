package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.party.PartyId
import korlibs.time.TimeSpan

data class GlobalStats(
    val parties: List<PartyStats>,
)

data class PartyStats(
    val name: String,
    val id: PartyId,
    val playerCount: Int,
    val spins: Int,
    val medianSpinDuration: TimeSpan?,
)
