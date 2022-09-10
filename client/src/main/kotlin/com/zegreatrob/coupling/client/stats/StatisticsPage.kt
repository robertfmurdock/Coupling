package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.minreact.create

val StatisticsPage = partyPageFunction { props, partyId ->
    +CouplingQuery(
        commander = props.commander,
        query = StatisticsQuery(partyId),
        toDataprops = { _, _, queryResult -> PartyStatistics(queryResult) }
    ).create(key = partyId.value)
}
