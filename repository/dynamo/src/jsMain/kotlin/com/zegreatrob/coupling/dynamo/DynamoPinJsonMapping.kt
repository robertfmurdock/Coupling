package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pin.*
import kotlin.js.Json
import kotlin.js.json

interface DynamoPinJsonMapping : TribeIdDynamoRecordJsonMapping {

    fun Record<TribeIdPin>.asDynamoJson() = recordJson()
        .add(
            json(
                "tribeId" to data.tribeId.value,
                "timestamp+id" to "${timestamp.isoWithMillis()}+${data.pin._id}"
            )
        )
        .add(data.pin.toDynamoJson())

    fun Pin.toDynamoJson() = nullFreeJson(
        "id" to _id,
        "name" to name,
        "icon" to icon
    )

    fun Json.toPin() = Pin(
        _id = getDynamoStringValue("id"),
        name = getDynamoStringValue("name") ?: defaultPin.name,
        icon = getDynamoStringValue("icon") ?: defaultPin.icon
    )

}
