package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.contribution.ContributionContentFrame
import com.zegreatrob.coupling.client.party.toModel
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.schema.ContributionVisualizationPageQuery
import com.zegreatrob.coupling.sdk.gql.ApolloGraphQuery
import js.lazy.Lazy

@Lazy
val ContributionVisualizationPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = ApolloGraphQuery(ContributionVisualizationPageQuery(partyId)),
        key = partyId.value.toString(),
    ) { _, _, queryResult ->
        val party = queryResult.party?.details?.partyDetailsFragment?.toModel() ?: return@CouplingQuery
        val spinsUntilFullRotation = queryResult.party.spinsUntilFullRotation ?: return@CouplingQuery
        ContributionContentFrame(party = party) {
            ContributionVisualization(props.commander, party, spinsUntilFullRotation)
        }
    }
}
