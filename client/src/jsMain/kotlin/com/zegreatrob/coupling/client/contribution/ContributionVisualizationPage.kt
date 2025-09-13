package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.contribution.ContributionContentFrame
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.sdk.gql.graphQuery
import js.lazy.Lazy

@Lazy
val ContributionVisualizationPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                details()
                spinsUntilFullRotation()
            }
        },
        key = partyId.value.toString(),
    ) { _, _, queryResult ->
        val party = queryResult.party?.details?.data ?: return@CouplingQuery
        val spinsUntilFullRotation = queryResult.party?.spinsUntilFullRotation ?: return@CouplingQuery
        ContributionContentFrame(party = party) {
            ContributionVisualization(props.commander, party, spinsUntilFullRotation)
        }
    }
}
