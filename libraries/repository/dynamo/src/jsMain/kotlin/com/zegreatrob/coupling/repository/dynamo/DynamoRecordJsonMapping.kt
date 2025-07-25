package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.ClockProvider
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.UserIdProvider
import kotools.types.text.toNotBlankString
import kotlin.js.Json
import kotlin.js.json
import kotlin.time.Instant

interface DynamoRecordJsonMapping :
    DynamoDatatypeSyntax,
    UserIdProvider,
    ClockProvider {

    fun <T> Record<T>.recordJson() = json(
        "timestamp" to timestamp.isoWithMillis(),
        "modifyingUserEmail" to modifyingUserId?.toString(),
        "isDeleted" to isDeleted,
    )

    fun recordJson(timestamp: Instant) = json(
        "timestamp" to timestamp.isoWithMillis(),
        "modifyingUserEmail" to userId.value.toString(),
    )

    fun <T> Json.toRecord(data: T) = Record(
        data,
        getDynamoStringValue("modifyingUserEmail")?.toNotBlankString()?.getOrNull(),
        getDynamoBoolValue("isDeleted") == true,
        getDynamoDateTimeValue("timestamp")!!,
    )
}
