package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import kotlin.js.Json
import kotlin.js.json

interface DynamoRecordJsonMapping : DynamoDatatypeSyntax, UserEmailSyntax,
    ClockSyntax {

    fun recordJson() = json(
        "timestamp" to now().isoWithMillis(),
        "modifyingUserEmail" to userEmail
    )

    fun <T> Json.toRecord(data: T) = Record(
        data,
        getDynamoDateTimeValue("timestamp")?.utc!!,
        false,
        getDynamoStringValue("modifyingUserEmail")!!
    )

}