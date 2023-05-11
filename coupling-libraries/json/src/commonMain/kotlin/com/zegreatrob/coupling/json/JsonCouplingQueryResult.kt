package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.Party
import kotlinx.serialization.Serializable

@Serializable
data class JsonCouplingQueryResult(
    val partyList: List<JsonPartyRecord>?,
)

fun JsonCouplingQueryResult.toDomain() = CouplingQueryResult(
    partyList = partyList?.map(JsonPartyRecord::toModelRecord),
)

data class CouplingQueryResult(
    val partyList: List<Record<Party>>? = null,
)
