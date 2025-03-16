package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserId
import kotlinx.datetime.Instant
import kotools.types.text.NotBlankString
import kotools.types.text.toNotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.js.Json
import kotlin.js.json

interface DynamoBoostJsonMapping : DynamoRecordJsonMapping {

    fun Record<Boost>.asDynamoJson() = recordJson()
        .add(data.asDynamoJson())
        .add(json("timestamp+id" to "${timestamp.isoWithMillis()}+${data.userId.value}"))

    fun Boost.asDynamoJson() = json(
        "pk" to "user-${userId.value}",
        "userId" to userId.value.toString(),
        "tribeIds" to partyIds.map { it.value.toString() }.toTypedArray(),
        "expirationDate" to expirationDate.toString(),
    )

    @OptIn(ExperimentalKotoolsTypesApi::class)
    fun Json.toBoost() = Boost(
        UserId(this["userId"].toString().toNotBlankString().getOrThrow()),
        this["tribeIds"]
            .unsafeCast<Array<String?>>()
            .mapNotNull { it?.let(NotBlankString::create)?.let(::PartyId) }
            .toSet(),
        this["expirationDate"]?.toString()?.let { Instant.parse(it) } ?: Instant.DISTANT_PAST,
    )

    fun Json.toBoostRecord() = toRecord(
        toBoost(),
    )
}
