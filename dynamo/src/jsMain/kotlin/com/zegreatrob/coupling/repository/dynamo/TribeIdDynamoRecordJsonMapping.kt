package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.party.PartyId
import kotlin.js.Json
import kotlin.js.json

interface TribeIdDynamoRecordJsonMapping : DynamoRecordJsonMapping {
    fun PartyId.recordJson(id: String?): Json {
        val timestamp = now()
        return json(
            "tribeId" to value,
            "timestamp+id" to "${timestamp.isoWithMillis()}+$id"
        ).add(super.recordJson(timestamp))
    }
}

