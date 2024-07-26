@file:UseSerializers(PartyIdSerializer::class)

package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Contributor
import kotlinx.serialization.UseSerializers

fun Contributor.toJson() = JsonContributor(
    email = email,
    playerId = playerId,
    details = details?.toSerializable(),
)

fun JsonContributor.toModel() = Contributor(
    email = email,
    playerId = playerId,
    details = details?.toModel(),
)
