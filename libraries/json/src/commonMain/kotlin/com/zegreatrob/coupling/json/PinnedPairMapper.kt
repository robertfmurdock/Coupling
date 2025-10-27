package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.pin.Pin
import kotools.types.collection.toNotEmptyList

fun PinnedCouplingPair.toSerializable() = GqlPinnedPair(
    players = pinnedPlayers.map(PinnedPlayer::toSerializable).toList(),
    pins = pins.map(Pin::toSerializable),
)

fun GqlPinnedPair.toModel() = PinnedCouplingPair(
    pinnedPlayers = players.map(GqlPinnedPlayer::toModel).toNotEmptyList().getOrThrow(),
    pins = pins.map(GqlPin::toModel).toSet(),
)

fun GqlPinnedPairInput.toModel() = PinnedCouplingPair(
    pinnedPlayers = players.map(GqlPinnedPlayerInput::toModel).toNotEmptyList().getOrThrow(),
    pins = pins.map(GqlPinInput::toModel).toSet(),
)
