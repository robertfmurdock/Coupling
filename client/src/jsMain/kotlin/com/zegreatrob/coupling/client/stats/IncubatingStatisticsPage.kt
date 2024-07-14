package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.sdk.gql.graphQuery

val IncubatingStatisticsPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = graphQuery { party(partyId) { details() } },
        toNode = { _, _, queryResult ->
            val party = queryResult.party?.details?.data ?: return@CouplingQuery null
            IncubatingPartyStatisticsContent.create(party = party) {
                IncubatingPartyStatisticsLoadingFrame(props.commander, party)
            }
        },
        key = partyId.value,
    )
}
