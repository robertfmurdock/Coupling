package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.action.stats.StatisticsQuery
import com.zegreatrob.coupling.client.components.stats.PartyStatistics
import com.zegreatrob.coupling.client.components.stats.create
import com.zegreatrob.coupling.client.memory.ClientStatisticsAction
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.sdk.gql.graphQuery
import react.useEffect
import react.useState

val StatisticsPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = graphQuery { party(partyId) { party(); playerList(); pairAssignmentDocumentList() } },
        toNode = { _, dispatchFunc, queryResult ->
            val party = queryResult.party?.details?.data ?: return@CouplingQuery null
            val players = queryResult.party?.playerList?.elements ?: return@CouplingQuery null
            val history = queryResult.party?.pairAssignmentDocumentList?.elements ?: return@CouplingQuery null
            var results by useState<StatisticsQuery.Results?>(null)
            val calculate = dispatchFunc { results = fire(ClientStatisticsAction(party, players, history)) }
            useEffect { calculate() }
            results?.let { PartyStatistics.create(it) }
        },
        key = partyId.value,
    )
}
