package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.party.PartyId
import kotlin.time.Duration

data class PartyStats(
    val name: String,
    val id: PartyId,
    val playerCount: Int,
    val spins: Int,
    val medianSpinDuration: Duration?,
    val appliedPinCount: Int,
    val uniquePinCount: Int,
)
