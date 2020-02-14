package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import kotlin.js.Json
import kotlin.js.json

interface DynamoPinJsonMapping : TribeIdDynamoRecordJsonMapping {

    fun TribeIdPin.toDynamoJson() = tribeId.recordJson()
        .add(pin.toDynamoJson())

    fun Pin.toDynamoJson() = json(
        "id" to _id,
        "name" to name,
        "icon" to icon
    )

    fun Json.toPin() = Pin(
        _id = getDynamoStringValue("id"),
        name = getDynamoStringValue("name"),
        icon = getDynamoStringValue("icon")
    )

}
