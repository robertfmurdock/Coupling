package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserDetails

fun UserDetails.toSerializable() = GqlUserDetails(
    id = id,
    email = email,
    authorizedPartyIds = authorizedPartyIds.map { it.value },
)

fun GqlUserDetails.toModel() = UserDetails(
    id = id,
    email = email,
    authorizedPartyIds = authorizedPartyIds.map(::PartyId).toSet(),
    stripeCustomerId = null,
)
