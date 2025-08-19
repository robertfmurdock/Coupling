package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.user.UserDetails

fun UserDetails.toSerializable() = GqlUserDetails(
    id = id,
    email = email,
    authorizedPartyIds = authorizedPartyIds.toList(),
    connectSecretId = connectSecretId,
)

fun GqlUserDetails.toModel() = UserDetails(
    id = id,
    email = email,
    connectedEmails = emptySet(),
    authorizedPartyIds = authorizedPartyIds.toSet(),
    stripeCustomerId = null,
    connectSecretId = connectSecretId,
)
