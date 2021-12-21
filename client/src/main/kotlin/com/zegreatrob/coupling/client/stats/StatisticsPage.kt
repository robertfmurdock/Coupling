package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.tribePageFunction

private val LoadedPairAssignments by lazy { couplingDataLoader<TribeStatistics>() }

val StatisticsPage = tribePageFunction { props, tribeId ->
    child(dataLoadProps(
        LoadedPairAssignments,
        commander = props.commander,
        query = StatisticsQuery(tribeId),
        toProps = { _, _, queryResult -> TribeStatistics(queryResult) }
    ), key = tribeId.value)
}
