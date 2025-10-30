package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.gql.fragment.UserDetailsFragment
import com.zegreatrob.coupling.model.user.UserDetails

fun UserDetailsFragment.toModel(): UserDetails = UserDetails(
    id = id,
    email = email,
    connectSecretId = connectSecretId,
    connectedEmails = connectedEmails.toSet(),
    authorizedPartyIds = authorizedPartyIds.toSet(),
    stripeCustomerId = null,
)
