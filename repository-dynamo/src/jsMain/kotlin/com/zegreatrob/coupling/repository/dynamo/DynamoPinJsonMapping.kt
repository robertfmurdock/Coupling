package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.pin.pin
import com.zegreatrob.coupling.model.pin.tribeId
import kotlin.js.Json
import kotlin.js.json

interface DynamoPinJsonMapping : TribeIdDynamoRecordJsonMapping {

    fun Record<TribeIdPin>.asDynamoJson() = recordJson()
        .add(
            json(
                "tribeId" to data.tribeId.value,
                "timestamp+id" to "${timestamp.isoWithMillis()}+${data.pin.id}"
            )
        )
        .add(data.pin.toDynamoJson())

    fun Pin.toDynamoJson() = nullFreeJson(
        "id" to id,
        "name" to name,
        "icon" to icon
    )

    fun Json.toPin() = Pin(
        id = getDynamoStringValue("id"),
        name = getDynamoStringValue("name") ?: "",
        icon = getDynamoStringValue("icon") ?: ""
    )

}
