package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.contribution.ContributionContentFrame
import com.zegreatrob.coupling.client.components.contribution.create
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.sdk.gql.graphQuery

val ContributionVisualizationPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                details()
                spinsUntilFullRotation()
            }
        },
        toNode = { _, _, queryResult ->
            val party = queryResult.party?.details?.data ?: return@CouplingQuery null
            val spinsUntilFullRotation = queryResult.party?.spinsUntilFullRotation ?: return@CouplingQuery null
            ContributionContentFrame.create(party = party) {
                ContributionVisualization(props.commander, party, spinsUntilFullRotation)
            }
        },
        key = partyId.value,
    )
}
