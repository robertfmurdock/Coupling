package com.zegreatrob.coupling.model.user

import com.zegreatrob.coupling.model.tribe.PartyId

data class User(
    val id: String,
    val email: String,
    val authorizedPartyIds: Set<PartyId>
)
