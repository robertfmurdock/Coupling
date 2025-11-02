package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.adapter.toModel
import com.zegreatrob.coupling.sdk.schema.fragment.PlayerDetails

fun PlayerDetails.toModel() = Player(
    id = id,
    name = name,
    email = email,
    badge = badge.toModel(),
    callSignAdjective = callSignAdjective,
    callSignNoun = callSignNoun,
    imageURL = imageURL,
    avatarType = avatarType?.toModel(),
    additionalEmails = unvalidatedEmails.toSet(),
)
