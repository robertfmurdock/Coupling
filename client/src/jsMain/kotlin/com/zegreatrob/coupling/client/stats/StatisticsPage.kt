package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.action.stats.StatisticsQuery
import com.zegreatrob.coupling.client.components.stats.PartyStatistics
import com.zegreatrob.coupling.client.components.stats.create
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery

val StatisticsPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = StatisticsQuery(partyId),
        toNode = { _, _, queryResult -> PartyStatistics.create(queryResult) },
        key = partyId.value,
    )
}
