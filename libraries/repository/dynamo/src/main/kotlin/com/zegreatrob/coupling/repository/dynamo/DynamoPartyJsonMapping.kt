package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.defaultParty
import kotlin.js.Json
import kotlin.js.json

interface DynamoPartyJsonMapping : DynamoDatatypeSyntax, DynamoRecordJsonMapping {

    fun Record<Party>.asDynamoJson() = recordJson().add(data.asDynamoJson())

    fun Json.toParty() = Party(
        id = PartyId(getDynamoStringValue("id") ?: ""),
        pairingRule = getDynamoNumberValue("pairingRule")?.toInt().let { PairingRule.fromValue(it) },
        badgesEnabled = getDynamoBoolValue("badgesEnabled") ?: defaultParty.badgesEnabled,
        defaultBadgeName = getDynamoStringValue("defaultBadgeName") ?: "",
        alternateBadgeName = getDynamoStringValue("alternateBadgeName") ?: "",
        email = getDynamoStringValue("email"),
        name = getDynamoStringValue("name"),
        callSignsEnabled = getDynamoBoolValue("callSignsEnabled") ?: defaultParty.callSignsEnabled,
        animationEnabled = getDynamoBoolValue("animationEnabled") ?: defaultParty.animationEnabled,
        animationSpeed = getDynamoNumberValue("animationSpeed")?.toDouble() ?: defaultParty.animationSpeed,
        slackChannel = getDynamoStringValue("slackChannel"),
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
        "animationSpeed" to animationSpeed,
        "slackChannel" to slackChannel,
    )
}
