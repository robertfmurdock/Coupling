package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.user.UserDetails

fun UserDetails.toSerializable() = GqlUserDetails(
    id = id,
    email = email,
    authorizedPartyIds = authorizedPartyIds.toList(),
    connectedEmails = connectedEmails.toList(),
    connectSecretId = connectSecretId,
)

fun GqlUserDetails.toModel() = UserDetails(
    id = id,
    email = email,
    connectedEmails = connectedEmails.toSet(),
    authorizedPartyIds = authorizedPartyIds.toSet(),
    stripeCustomerId = null,
    connectSecretId = connectSecretId,
)
