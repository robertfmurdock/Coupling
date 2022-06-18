package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.minreact.add
import react.key

private val LoadedPairAssignments by lazy { couplingDataLoader<PartyStatistics>() }

val StatisticsPage = partyPageFunction { props, partyId ->
    add(
        dataLoadProps(
            LoadedPairAssignments,
            commander = props.commander,
            query = StatisticsQuery(partyId),
            toProps = { _, _, queryResult -> PartyStatistics(queryResult) }
        )
    ) {
        key = partyId.value
    }
}
