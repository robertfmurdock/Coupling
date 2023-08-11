package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant
import kotlin.js.Json
import kotlin.js.json

interface DynamoBoostJsonMapping : DynamoRecordJsonMapping {

    fun Record<Boost>.asDynamoJson() = recordJson()
        .add(data.asDynamoJson())
        .add(json("timestamp+id" to "${timestamp.isoWithMillis()}+${data.userId}"))

    fun Boost.asDynamoJson() = json(
        "pk" to "user-$userId",
        "userId" to userId,
        "tribeIds" to partyIds.map { it.value }.toTypedArray(),
        "expirationDate" to expirationDate.toString(),
    )

    fun Json.toBoost() = Boost(
        this["userId"].toString(),
        this["tribeIds"]
            .unsafeCast<Array<String?>>()
            .mapNotNull { it?.let(::PartyId) }
            .toSet(),
        this["expirationDate"]?.toString()?.toInstant() ?: Instant.DISTANT_PAST,
    )

    fun Json.toBoostRecord() = toRecord(
        toBoost(),
    )
}
