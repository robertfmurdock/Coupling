package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserId
import kotlin.time.Instant

data class Boost(
    val userId: UserId,
    val partyIds: Set<PartyId>,
    val expirationDate: Instant,
)
