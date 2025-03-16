package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.party.PartyId
import kotlinx.datetime.Instant
import kotools.types.text.NotBlankString

data class Boost(
    val userId: NotBlankString,
    val partyIds: Set<PartyId>,
    val expirationDate: Instant,
)
