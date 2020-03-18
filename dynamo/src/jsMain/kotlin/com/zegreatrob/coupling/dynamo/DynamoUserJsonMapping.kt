package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import kotlin.js.Json
import kotlin.js.json

interface DynamoUserJsonMapping : DynamoRecordJsonMapping {

    fun Record<User>.asDynamoJson() = recordJson().add(data.asDynamoJson())

    fun User.asDynamoJson() = json(
        "id" to id,
        "email" to email,
        "authorizedTribeIds" to authorizedTribeIds.map { it.value }.toTypedArray()
    )

    fun Json.toUser() = User(
        this["id"].toString(),
        this["email"].toString(),
        this["authorizedTribeIds"].unsafeCast<Array<String>>()
            .map { TribeId(it) }
            .toSet()
    )

    fun Json.toUserRecord() = toRecord(
        toUser()
    )
}
