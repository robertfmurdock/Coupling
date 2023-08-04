package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserDetails
import kotlin.js.Json
import kotlin.js.json

interface DynamoUserJsonMapping : DynamoRecordJsonMapping {

    fun Record<UserDetails>.asDynamoJson() = recordJson().add(data.asDynamoJson())

    fun UserDetails.asDynamoJson() = json(
        "id" to id,
        "email" to email,
        "stripeCustomerId" to stripeCustomerId,
        "authorizedTribeIds" to authorizedPartyIds.map { it.value }.toTypedArray(),
    )

    fun Json.toUser() = UserDetails(
        this["id"].toString(),
        this["email"].toString(),
        this["authorizedTribeIds"]
            .unsafeCast<Array<String?>>()
            .mapNotNull { it?.let(::PartyId) }
            .toSet(),
        getDynamoStringValue("stripeCustomerId"),
    )

    fun Json.toUserRecord() = toRecord(
        toUser(),
    )
}
