package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.PartyIntegration
import com.zegreatrob.coupling.model.party.defaultParty
import kotools.types.text.toNotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.js.Json
import kotlin.js.json

interface DynamoPartyJsonMapping :
    DynamoDatatypeSyntax,
    DynamoRecordJsonMapping {

    fun Record<PartyDetails>.asDynamoJson() = recordJson().add(data.asDynamoJson())

    fun PartyDetails.asDynamoJson() = json(
        "id" to id.value.toString(),
        "name" to name,
        "email" to email,
        "pairingRule" to PairingRule.toValue(pairingRule),
        "defaultBadgeName" to defaultBadgeName,
        "alternateBadgeName" to alternateBadgeName,
        "badgesEnabled" to badgesEnabled,
        "callSignsEnabled" to callSignsEnabled,
        "animationEnabled" to animationEnabled,
        "animationSpeed" to animationSpeed,
    )

    @OptIn(ExperimentalKotoolsTypesApi::class)
    fun Json.toParty() = PartyDetails(
        id = PartyId(getDynamoStringValue("id")?.toNotBlankString()?.getOrNull()!!),
        pairingRule = getDynamoNumberValue("pairingRule")?.toInt().let { PairingRule.fromValue(it) },
        badgesEnabled = getDynamoBoolValue("badgesEnabled") ?: defaultParty.badgesEnabled,
        defaultBadgeName = getDynamoStringValue("defaultBadgeName") ?: "",
        alternateBadgeName = getDynamoStringValue("alternateBadgeName") ?: "",
        email = getDynamoStringValue("email"),
        name = getDynamoStringValue("name"),
        callSignsEnabled = getDynamoBoolValue("callSignsEnabled") ?: defaultParty.callSignsEnabled,
        animationEnabled = getDynamoBoolValue("animationEnabled") ?: defaultParty.animationEnabled,
        animationSpeed = getDynamoNumberValue("animationSpeed")?.toDouble() ?: defaultParty.animationSpeed,
    )

    fun Record<PartyElement<PartyIntegration>>.asDynamoJson() = recordJson().add(data.asDynamoJson())

    companion object {
        const val INTEGRATION_CONSTANT = "INTEGRATION-"
    }

    fun PartyElement<PartyIntegration>.asDynamoJson() = json(
        "id" to "$INTEGRATION_CONSTANT${partyId.value}",
        "slackChannel" to this.element.slackChannel,
        "slackTeam" to this.element.slackTeam,
    )

    fun Json.toIntegration() = PartyIntegration(
        slackTeam = getDynamoStringValue("slackTeam"),
        slackChannel = getDynamoStringValue("slackChannel"),
    )
}
