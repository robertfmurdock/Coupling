package com.zegreatrob.coupling.client.integration

import com.zegreatrob.coupling.client.gql.IntegrationPageQuery
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.party.PartyIntegration
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.schema.type.PartyInput
import com.zegreatrob.coupling.sdk.toModel
import js.lazy.Lazy

@Lazy
val IntegrationPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = GqlQuery(IntegrationPageQuery(PartyInput(partyId))),
        key = partyId.value.toString(),
    ) { _, _, result ->
        IntegrationContent(
            party = result.party?.partyDetails?.toModel() ?: return@CouplingQuery,
            integration = result.party.integration?.toModel(),
            addToSlackUrl = result.config?.addToSlackUrl ?: return@CouplingQuery,
        )
    }
}

fun IntegrationPageQuery.Integration.toModel() = PartyIntegration(
    slackTeam = slackTeam,
    slackChannel = slackChannel,
)
