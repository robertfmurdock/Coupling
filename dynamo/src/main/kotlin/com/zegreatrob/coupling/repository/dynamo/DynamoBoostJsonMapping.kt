package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import kotlin.js.Json
import kotlin.js.json

interface DynamoBoostJsonMapping : DynamoRecordJsonMapping {

    fun Record<Boost>.asDynamoJson() = recordJson()
        .add(data.asDynamoJson())
        .add(json("timestamp+id" to "${timestamp.isoWithMillis()}+${data.userId}"))

    fun Boost.asDynamoJson() = json(
        "pk" to "user-$userId",
        "userId" to userId,
        "tribeIds" to partyIds.map { it.value }.toTypedArray()
    )

    fun Json.toBoost() = Boost(
        this["userId"].toString(),
        this["tribeIds"]
            .unsafeCast<Array<String?>>()
            .mapNotNull { it?.let(::PartyId) }
            .toSet()
    )

    fun Json.toBoostRecord() = toRecord(
        toBoost()
    )
}
