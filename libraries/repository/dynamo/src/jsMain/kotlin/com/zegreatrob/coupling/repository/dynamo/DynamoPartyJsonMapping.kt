package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyIntegration
import kotlin.js.Json
import kotlin.js.json

interface DynamoPartyJsonMapping :
    DynamoDatatypeSyntax,
    DynamoRecordJsonMapping,
    LegacyPartyConfigurationMapping {

    fun Record<PartyDetails>.asDynamoJson() = recordJson().add(data.asDynamoJson())

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
