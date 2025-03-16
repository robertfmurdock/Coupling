package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Contributor
import org.kotools.types.ExperimentalKotoolsTypesApi

fun Contributor.toJson() = GqlContributor(
    email = email,
    playerId = playerId,
)

@OptIn(ExperimentalKotoolsTypesApi::class)
fun GqlContributor.toModel() = Contributor(
    email = email,
    playerId = playerId,
)
