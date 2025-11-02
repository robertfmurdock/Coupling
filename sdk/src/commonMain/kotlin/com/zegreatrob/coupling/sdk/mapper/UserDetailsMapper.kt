package com.zegreatrob.coupling.sdk.mapper

import com.zegreatrob.coupling.sdk.schema.fragment.UserDetails

fun UserDetails.toDomain(): com.zegreatrob.coupling.model.user.UserDetails = com.zegreatrob.coupling.model.user.UserDetails(
    id = id,
    email = email,
    connectSecretId = connectSecretId,
    connectedEmails = connectedEmails.toSet(),
    authorizedPartyIds = authorizedPartyIds.toSet(),
    stripeCustomerId = null,
)
