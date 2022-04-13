package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.defaultParty
import kotlin.js.Json
import kotlin.js.json

interface DynamoTribeJsonMapping : DynamoDatatypeSyntax, DynamoRecordJsonMapping {

    fun Record<Party>.asDynamoJson() = recordJson().add(data.asDynamoJson())

    fun Json.toParty() = Party(
        id = PartyId(getDynamoStringValue("id") ?: ""),
        name = getDynamoStringValue("name"),
        email = getDynamoStringValue("email"),
        pairingRule = getDynamoNumberValue("pairingRule")?.toInt().let { PairingRule.fromValue(it) },
        defaultBadgeName = getDynamoStringValue("defaultBadgeName") ?: "",
        alternateBadgeName = getDynamoStringValue("alternateBadgeName") ?: "",
        badgesEnabled = getDynamoBoolValue("badgesEnabled") ?: defaultParty.badgesEnabled,
        callSignsEnabled = getDynamoBoolValue("callSignsEnabled") ?: defaultParty.callSignsEnabled,
        animationEnabled = getDynamoBoolValue("animationEnabled") ?: defaultParty.animationEnabled,
        animationSpeed = getDynamoNumberValue("animationSpeed")?.toDouble() ?: defaultParty.animationSpeed
    )

    fun Party.asDynamoJson() = json(
        "id" to id.value,
        "name" to name,
        "email" to email,
        "pairingRule" to PairingRule.toValue(pairingRule),
        "defaultBadgeName" to defaultBadgeName,
        "alternateBadgeName" to alternateBadgeName,
        "badgesEnabled" to badgesEnabled,
        "callSignsEnabled" to callSignsEnabled,
        "animationEnabled" to animationEnabled,
        "animationSpeed" to animationSpeed
    )

}
