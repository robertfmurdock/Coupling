package com.zegreatrob.coupling.client.stats

import com.zegreatrob.minreact.child
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.client.tribePageFunction

private val LoadedPairAssignments by lazy { dataLoadWrapper(TribeStatistics) }

val StatisticsPage = tribePageFunction { props, tribeId ->
    child(LoadedPairAssignments, dataLoadProps(
        commander = props.commander,
        query = StatisticsQuery(tribeId),
        toProps = { _, _, queryResult -> TribeStatisticsProps(queryResult, props.pathSetter) }
    ))
}
