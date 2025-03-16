package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Contributor
import com.zegreatrob.coupling.model.player.PlayerId
import kotools.types.text.NotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi

fun Contributor.toJson() = GqlContributor(
    email = email,
    playerId = playerId?.value,
)

@OptIn(ExperimentalKotoolsTypesApi::class)
fun GqlContributor.toModel() = Contributor(
    email = email,
    playerId = playerId?.let { PlayerId(NotBlankString.create(it)) },
)
