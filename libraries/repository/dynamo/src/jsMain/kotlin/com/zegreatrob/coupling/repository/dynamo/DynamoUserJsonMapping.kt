package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.model.user.UserId
import kotools.types.text.toNotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.js.Json
import kotlin.js.json

interface DynamoUserJsonMapping : DynamoRecordJsonMapping {

    fun Record<UserDetails>.asDynamoJson() = recordJson().add(data.asDynamoJson())

    fun UserDetails.asDynamoJson() = json(
        "id" to id.value.toString(),
        "email" to email.toString(),
        "stripeCustomerId" to stripeCustomerId,
        "authorizedTribeIds" to authorizedPartyIds.map { it.value.toString() }.toTypedArray(),
    )

    @OptIn(ExperimentalKotoolsTypesApi::class)
    fun Json.toUser() = UserDetails(
        UserId(this["id"].toString().toNotBlankString().getOrThrow()),
        this["email"].toString().toNotBlankString().getOrThrow(),
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
