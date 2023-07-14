package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.components.stats.PartyStatistics
import com.zegreatrob.coupling.client.components.stats.create
import com.zegreatrob.coupling.client.memory.ClientStatisticsQueryDispatcher
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.sdk.gql.graphQuery

val StatisticsPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = graphQuery { party(partyId) { party(); playerList(); pairAssignmentDocumentList() } },
        toNode = { _, _, queryResult ->

            val party = queryResult.party?.details?.data ?: return@CouplingQuery null
            val players = queryResult.party?.playerList?.elements ?: return@CouplingQuery null
            val history = queryResult.party?.pairAssignmentDocumentList?.elements ?: return@CouplingQuery null

            val results = object : ClientStatisticsQueryDispatcher {}
                .calculate(party, players, history)

            PartyStatistics.create(results)
        },
        key = partyId.value,
    )
}
