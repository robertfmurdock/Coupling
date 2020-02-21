package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.pin.pin
import com.zegreatrob.coupling.model.pin.tribeId
import kotlin.js.Json

interface DynamoPinJsonMapping : TribeIdDynamoRecordJsonMapping {

    fun TribeIdPin.toDynamoJson() = tribeId.recordJson(pin._id)
        .add(pin.toDynamoJson())

    fun Pin.toDynamoJson() = nullFreeJson(
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
