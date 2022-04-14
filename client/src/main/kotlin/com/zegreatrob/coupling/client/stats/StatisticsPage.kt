package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.minreact.child

private val LoadedPairAssignments by lazy { couplingDataLoader<TribeStatistics>() }

val StatisticsPage = partyPageFunction { props, tribeId ->
    child(dataLoadProps(
        LoadedPairAssignments,
        commander = props.commander,
        query = StatisticsQuery(tribeId),
        toProps = { _, _, queryResult -> TribeStatistics(queryResult) }
    ), key = tribeId.value)
}
