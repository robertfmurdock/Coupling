package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlin.js.json

interface TribeIdDynamoRecordJsonMapping : DynamoDatatypeSyntax {
    fun TribeId.recordJson() = json(
        "tribeId" to value,
        "timestamp" to DateTime.now().isoWithMillis()
    )
}