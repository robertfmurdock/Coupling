package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.sdk.schema.fragment.UserDetailsFragment

fun UserDetailsFragment.toModel(): UserDetails = UserDetails(
    id = id,
    email = email,
    connectSecretId = connectSecretId,
    connectedEmails = connectedEmails.toSet(),
    authorizedPartyIds = authorizedPartyIds.toSet(),
    stripeCustomerId = null,
)
