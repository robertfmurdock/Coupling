package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.DateTime
import kotlin.js.json

interface DynamoRecordJsonMapping : DynamoDatatypeSyntax {

    fun recordJson() = json(
        "timestamp" to DateTime.now().isoWithMillis()
    )

}