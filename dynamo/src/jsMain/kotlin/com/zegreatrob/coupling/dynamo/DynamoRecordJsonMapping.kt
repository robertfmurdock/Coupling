package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import kotlin.js.Json
import kotlin.js.json

interface DynamoRecordJsonMapping : DynamoDatatypeSyntax, UserEmailSyntax,
    ClockSyntax {

    fun <T> Record<T>.recordJson() = json(
        "timestamp" to timestamp.isoWithMillis(),
        "modifyingUserEmail" to modifyingUserEmail
    )

    fun recordJson(timestamp: DateTime) = json(
        "timestamp" to timestamp.isoWithMillis(),
        "modifyingUserEmail" to userEmail
    )

    fun <T> Json.toRecord(data: T) = Record(
        data,
        getDynamoStringValue("modifyingUserEmail")!!,
        getDynamoBoolValue("isDeleted") ?: false,
        getDynamoDateTimeValue("timestamp")?.utc!!
    )

}