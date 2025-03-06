package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.ClockProvider
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.UserIdProvider
import kotlinx.datetime.Instant
import kotlin.js.Json
import kotlin.js.json

interface DynamoRecordJsonMapping :
    DynamoDatatypeSyntax,
    UserIdProvider,
    ClockProvider {

    fun <T> Record<T>.recordJson() = json(
        "timestamp" to timestamp.isoWithMillis(),
        "modifyingUserEmail" to modifyingUserId,
        "isDeleted" to isDeleted,
    )

    fun recordJson(timestamp: Instant) = json(
        "timestamp" to timestamp.isoWithMillis(),
        "modifyingUserEmail" to userId,
    )

    fun <T> Json.toRecord(data: T) = Record(
        data,
        getDynamoStringValue("modifyingUserEmail") ?: "",
        getDynamoBoolValue("isDeleted") == true,
        getDynamoDateTimeValue("timestamp")!!,
    )
}
