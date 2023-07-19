package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.action.stats.StatisticsQuery
import com.zegreatrob.coupling.client.ClientDispatcher
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.stats.PartyStatistics
import com.zegreatrob.coupling.client.memory.ClientStatisticsAction
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.nfc
import react.Fragment
import react.PropsWithValue
import react.create
import react.useEffectOnce
import react.useState

val StatisticsPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = graphQuery { party(partyId) { party(); playerList(); pairAssignmentDocumentList() } },
        toNode = { _, dispatchFunc, queryResult ->
            val party = queryResult.party?.details?.data ?: return@CouplingQuery null
            val players = queryResult.party?.playerList?.elements ?: return@CouplingQuery null
            val history = queryResult.party?.pairAssignmentDocumentList?.elements ?: return@CouplingQuery null
            val action = ClientStatisticsAction(party, players, history)
            Fragment.create { CalculatingPartyStats(action to dispatchFunc) }
        },
        key = partyId.value,
    )
}

val CalculatingPartyStats by nfc<PropsWithValue<Pair<ClientStatisticsAction, DispatchFunc<ClientDispatcher>>>> { props ->
    var results by useState<StatisticsQuery.Results?>(null)
    val calculate = props.value.second { results = fire(props.value.first) }
    useEffectOnce { calculate() }
    results?.let { PartyStatistics(it) }
}
