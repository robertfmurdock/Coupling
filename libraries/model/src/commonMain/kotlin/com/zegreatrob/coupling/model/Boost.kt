package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.party.PartyId
import kotlinx.datetime.Instant

data class Boost(
    val userId: String,
    val partyIds: Set<PartyId>,
    val expirationDate: Instant,
)
