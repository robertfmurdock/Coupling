package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.tribe.TribeId
import kotlin.js.json

interface TribeIdDynamoRecordJsonMapping : DynamoRecordJsonMapping {
    fun TribeId.recordJson() = json(
        "tribeId" to value
    ).add(super.recordJson())
}

