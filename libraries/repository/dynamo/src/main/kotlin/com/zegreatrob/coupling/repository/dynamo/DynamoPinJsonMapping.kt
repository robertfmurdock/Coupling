package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.partyId
import com.zegreatrob.coupling.model.pin.pin
import kotlin.js.Json
import kotlin.js.json

interface DynamoPinJsonMapping : PartyIdDynamoRecordJsonMapping {

    fun Record<PartyElement<Pin>>.asDynamoJson() = recordJson()
        .add(
            json(
                "tribeId" to data.partyId.value,
                "timestamp+id" to "${timestamp.isoWithMillis()}+${data.pin.id}",
            ),
        )
        .add(data.pin.toDynamoJson())

    fun Pin.toDynamoJson() = nullFreeJson(
        "id" to id,
        "name" to name,
        "icon" to icon,
    )

    fun Json.toPin() = Pin(
        id = getDynamoStringValue("id"),
        name = getDynamoStringValue("name") ?: "",
        icon = getDynamoStringValue("icon") ?: "",
    )
}
