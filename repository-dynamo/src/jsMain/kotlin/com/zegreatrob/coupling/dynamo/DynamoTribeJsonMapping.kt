package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.defaultTribe
import kotlin.js.Json
import kotlin.js.json

interface DynamoTribeJsonMapping : DynamoDatatypeSyntax, DynamoRecordJsonMapping {

    fun Record<Tribe>.asDynamoJson() = recordJson().add(data.asDynamoJson())

    fun Json.toTribe() = Tribe(
        id = TribeId(getDynamoStringValue("id") ?: ""),
        name = getDynamoStringValue("name"),
        email = getDynamoStringValue("email"),
        pairingRule = getDynamoNumberValue("pairingRule")?.toInt().let { PairingRule.fromValue(it) },
        defaultBadgeName = getDynamoStringValue("defaultBadgeName") ?: "",
        alternateBadgeName = getDynamoStringValue("alternateBadgeName") ?: "",
        badgesEnabled = getDynamoBoolValue("badgesEnabled") ?: defaultTribe.badgesEnabled,
        callSignsEnabled = getDynamoBoolValue("callSignsEnabled") ?: defaultTribe.callSignsEnabled,
        animationEnabled = getDynamoBoolValue("animationEnabled") ?: defaultTribe.animationEnabled,
        animationSpeed = getDynamoNumberValue("animationSpeed")?.toDouble() ?: defaultTribe.animationSpeed
    )

    fun Tribe.asDynamoJson() = json(
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
