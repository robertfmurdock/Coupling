package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserDetails
import kotools.types.text.toNotBlankString

fun UserDetails.toSerializable() = GqlUserDetails(
    id = id,
    email = email.toString(),
    authorizedPartyIds = authorizedPartyIds.map { it.value.toString() },
)

fun GqlUserDetails.toModel() = UserDetails(
    id = id,
    email = email.toNotBlankString().getOrThrow(),
    authorizedPartyIds = authorizedPartyIds.map(::PartyId).toSet(),
    stripeCustomerId = null,
)
