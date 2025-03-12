package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.party.SecretId
import com.zegreatrob.coupling.model.party.with
import kotlinx.datetime.Instant
import kotools.types.text.NotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.js.Json
import kotlin.js.json

interface DynamoSecretJsonMapping : PartyIdDynamoRecordJsonMapping {

    fun Record<PartyElement<Secret>>.asDynamoJson() = recordJson()
        .add(
            json(
                "tribeId" to data.partyId.value.toString(),
                "timestamp+id" to "${timestamp.isoWithMillis()}+${data.element.id}",
            ),
        )
        .add(data.element.toDynamoJson())

    fun Secret.toDynamoJson() = nullFreeJson(
        "id" to id.value.toString(),
        "description" to description,
        "createdDate" to createdTimestamp.isoWithMillis(),
        "lastUsedTimestamp" to lastUsedTimestamp?.isoWithMillis(),
    )

    fun Json.toSecret(): Secret? {
        return Secret(
            id = getDynamoStringValue("id")?.let { SecretId(it) } ?: return null,
            description = getDynamoStringValue("description") ?: "",
            createdTimestamp = getDynamoDateTimeValue("createdDate") ?: Instant.DISTANT_PAST,
            lastUsedTimestamp = getDynamoDateTimeValue("lastUsedTimestamp"),
        )
    }

    @OptIn(ExperimentalKotoolsTypesApi::class)
    fun Json.toRecord(): Record<PartyElement<Secret>>? {
        val partyId = this["tribeId"].unsafeCast<String>().let(NotBlankString::create).let(::PartyId)
        return toRecord(partyId.with(toSecret() ?: return null))
    }
}
