package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.tribe.TribeId
import kotlin.js.Json
import kotlin.js.json

interface TribeIdDynamoRecordJsonMapping : DynamoRecordJsonMapping {
    fun TribeId.recordJson(id: String?): Json {
        val timestamp = now()
        return json(
            "tribeId" to value,
            "timestamp+id" to "${timestamp.isoWithMillis()}+$id"
        ).add(super.recordJson(timestamp))
    }
}

