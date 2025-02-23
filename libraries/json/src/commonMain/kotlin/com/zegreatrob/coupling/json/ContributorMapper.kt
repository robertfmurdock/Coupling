package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Contributor

fun Contributor.toJson() = GqlContributor(
    email = email,
    playerId = playerId,
)

fun GqlContributor.toModel() = Contributor(
    email = email,
    playerId = playerId,
)
