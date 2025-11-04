package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserDetails

fun User.toSerializable() = GqlUser(
    id = id,
    email = details.email,
    authorizedPartyIds = details.authorizedPartyIds.toList(),
    connectedEmails = details.connectedEmails.toList(),
    connectSecretId = details.connectSecretId,
    boost = boost?.toSerializable(),
    subscription = subscription?.toJson(),
    players = emptyList(),
)

fun GqlUser.toModel() = User(
    id = id,
    details = UserDetails(
        id = id,
        email = email,
        connectedEmails = connectedEmails.toSet(),
        authorizedPartyIds = authorizedPartyIds.toSet(),
        stripeCustomerId = null,
        connectSecretId = connectSecretId,
    ),
    boost = boost?.toModelRecord(),
    subscription = subscription?.toModel(),
    players = players.map { it.toModel() },
)
