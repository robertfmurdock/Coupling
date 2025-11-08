package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.defaultPlayer
import org.kotools.types.ExperimentalKotoolsTypesApi

fun PinnedPlayer.toSerializable() = GqlPlayerSnapshot(
    id = player.id,
    name = player.name,
    email = player.email,
    badge = player.badge.toSerializable(),
    callSignAdjective = player.callSignAdjective,
    callSignNoun = player.callSignNoun,
    imageURL = player.imageURL,
    avatarType = player.avatarType?.toSerializable(),
    unvalidatedEmails = player.additionalEmails.toList(),
    pins = pins.map(Pin::toSerializable),
)

@OptIn(ExperimentalKotoolsTypesApi::class)
fun GqlPlayerSnapshot.toModel() = PinnedPlayer(
    player = Player(
        id = id,
        badge = badge.toModel(),
        name = name,
        email = email,
        callSignAdjective = callSignAdjective,
        callSignNoun = callSignNoun,
        imageURL = imageURL,
        avatarType = avatarType?.toModel(),
        additionalEmails = unvalidatedEmails.toSet(),
    ),
    pins = pins.map(GqlPinSnapshot::toModel),
)

@OptIn(ExperimentalKotoolsTypesApi::class)
fun GqlPinnedPlayerInput.toModel() = PinnedPlayer(
    player = Player(
        id = id,
        badge = badge?.toModel() ?: defaultPlayer.badge,
        name = name,
        email = email,
        callSignAdjective = callSignAdjective,
        callSignNoun = callSignNoun,
        imageURL = imageURL,
        avatarType = avatarType?.toModel(),
        additionalEmails = unvalidatedEmails.toSet(),
    ),
    pins = pins.map(GqlPinInput::toModel),
)
