package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.CouplingQueryResult
import kotlinx.serialization.json.JsonElement

fun GqlQuery.toDomain(raw: JsonElement) = CouplingQueryResult(
    raw = raw.toString(),
    partyList = partyList?.map(GqlParty::toModel),
    user = user?.toModel(),
    party = party?.toModel(),
    globalStats = globalStats?.toModel(),
    config = config?.toModel(),
)
