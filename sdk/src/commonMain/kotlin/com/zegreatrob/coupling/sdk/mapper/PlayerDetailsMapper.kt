package com.zegreatrob.coupling.sdk.mapper

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.schema.fragment.PlayerDetails

fun PlayerDetails.toDomain() = Player(
    id = id,
    name = name,
    email = email,
    badge = badge.toDomain(),
    callSignAdjective = callSignAdjective,
    callSignNoun = callSignNoun,
    imageURL = imageURL,
    avatarType = avatarType?.toDomain(),
    additionalEmails = unvalidatedEmails.toSet(),
)
