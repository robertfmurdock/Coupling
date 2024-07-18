package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.sdk.gql.graphQuery

val ContributionOverviewPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                details()
                contributions(limit = 5)
            }
        },
        toNode = { _, _, queryResult ->
            val party = queryResult.party?.details?.data ?: return@CouplingQuery null
            val contributions = queryResult.party?.contributions ?: return@CouplingQuery null
            ContributionContentFrame.create(party = party) {
                +"TBD"
                contributions.forEach { contribution ->
                    +"$contribution"
                }
            }
        },
        key = partyId.value,
    )
}
