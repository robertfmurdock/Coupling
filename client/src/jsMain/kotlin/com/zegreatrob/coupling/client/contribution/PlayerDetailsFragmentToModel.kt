package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.gql.fragment.PlayerDetailsFragment
import com.zegreatrob.coupling.model.player.Player

fun PlayerDetailsFragment.toModel() = Player(
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
