@file:UseSerializers(PartyIdSerializer::class)

package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Contributor
import kotlinx.serialization.UseSerializers

fun Contributor.toJson() = GqlContributor(
    email = email,
    details = details?.toSerializable(),
)

fun GqlContributor.toModel() = Contributor(
    email = email,
    details = details?.toModel(),
)
