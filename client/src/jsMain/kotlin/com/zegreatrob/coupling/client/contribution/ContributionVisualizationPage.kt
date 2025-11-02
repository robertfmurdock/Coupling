package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.contribution.ContributionContentFrame
import com.zegreatrob.coupling.client.gql.ContributionVisualizationPageQuery
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import js.lazy.Lazy

@Lazy
val ContributionVisualizationPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = GqlQuery(ContributionVisualizationPageQuery(partyId)),
        key = partyId.value.toString(),
    ) { _, _, queryResult ->
        val party = queryResult.party?.partyDetails?.toDomain() ?: return@CouplingQuery
        val spinsUntilFullRotation = queryResult.party.spinsUntilFullRotation ?: return@CouplingQuery
        ContributionContentFrame(party = party) {
            ContributionVisualization(props.commander, party, spinsUntilFullRotation)
        }
    }
}
