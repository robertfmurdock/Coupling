package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.party.with
import kotlinx.datetime.Instant
import kotlin.js.Json
import kotlin.js.json

interface DynamoSecretJsonMapping : PartyIdDynamoRecordJsonMapping {

    fun Record<PartyElement<Secret>>.asDynamoJson() = recordJson()
        .add(
            json(
                "tribeId" to data.partyId.value,
                "timestamp+id" to "${timestamp.isoWithMillis()}+${data.element.id}",
            ),
        )
        .add(data.element.toDynamoJson())

    fun Secret.toDynamoJson() = nullFreeJson(
        "id" to id,
        "description" to description,
        "createdDate" to createdTimestamp.isoWithMillis(),
        "lastUsedTimestamp" to lastUsedTimestamp?.isoWithMillis(),
    )

    fun Json.toSecret() = Secret(
        id = getDynamoStringValue("id") ?: "",
        description = getDynamoStringValue("description") ?: "",
        createdTimestamp = getDynamoDateTimeValue("createdDate") ?: Instant.DISTANT_PAST,
        lastUsedTimestamp = getDynamoDateTimeValue("lastUsedTimestamp"),
    )

    fun Json.toRecord(): Record<PartyElement<Secret>> {
        val partyId = this["tribeId"].unsafeCast<String>().let(::PartyId)
        return toRecord(partyId.with(toSecret()))
    }
}
