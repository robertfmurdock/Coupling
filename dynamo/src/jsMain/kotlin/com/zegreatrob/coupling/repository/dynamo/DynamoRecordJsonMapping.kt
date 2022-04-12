package com.zegreatrob.coupling.repository.dynamo

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.UserIdSyntax
import kotlin.js.Json
import kotlin.js.json

interface DynamoRecordJsonMapping : DynamoDatatypeSyntax, UserIdSyntax, ClockSyntax {

    fun <T> Record<T>.recordJson() = json(
        "timestamp" to timestamp.isoWithMillis(),
        "modifyingUserEmail" to modifyingUserId,
        "isDeleted" to isDeleted
    )

    fun recordJson(timestamp: DateTime) = json(
        "timestamp" to timestamp.isoWithMillis(),
        "modifyingUserEmail" to userId
    )

    fun <T> Json.toRecord(data: T) = Record(
        data,
        getDynamoStringValue("modifyingUserEmail") ?: "",
        getDynamoBoolValue("isDeleted") ?: false,
        getDynamoDateTimeValue("timestamp")?.utc!!
    )

}
