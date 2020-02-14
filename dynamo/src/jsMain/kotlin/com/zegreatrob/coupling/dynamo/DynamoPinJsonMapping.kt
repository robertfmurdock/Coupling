package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.dynamo.DynamoPairAssignmentDocumentRepository.Companion.getDynamoStringValue
import com.zegreatrob.coupling.model.pin.Pin
import kotlin.js.Json
import kotlin.js.json

interface DynamoPinJsonMapping {

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
