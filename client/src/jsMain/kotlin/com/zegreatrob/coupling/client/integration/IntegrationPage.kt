package com.zegreatrob.coupling.client.integration

import com.zegreatrob.coupling.client.gql.IntegrationPageQuery
import com.zegreatrob.coupling.client.party.toModel
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.party.PartyIntegration
import com.zegreatrob.coupling.sdk.gql.ApolloGraphQuery
import com.zegreatrob.coupling.sdk.schema.type.PartyInput
import js.lazy.Lazy

@Lazy
val IntegrationPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = ApolloGraphQuery(IntegrationPageQuery(PartyInput(partyId))),
        key = partyId.value.toString(),
    ) { _, _, result ->
        IntegrationContent(
            party = result.party?.details?.partyDetailsFragment?.toModel() ?: return@CouplingQuery,
            integration = result.party.integration?.toModel(),
            addToSlackUrl = result.config?.addToSlackUrl ?: return@CouplingQuery,
        )
    }
}

fun IntegrationPageQuery.Integration.toModel() = PartyIntegration(
    slackTeam = slackTeam,
    slackChannel = slackChannel,
)
