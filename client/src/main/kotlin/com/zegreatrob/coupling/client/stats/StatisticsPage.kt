package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.couplingDataLoadWrapper
import com.zegreatrob.coupling.client.tribePageFunction
import com.zegreatrob.minreact.child

private val LoadedPairAssignments by lazy { couplingDataLoadWrapper(TribeStatistics) }

val StatisticsPage = tribePageFunction { props, tribeId ->
    child(LoadedPairAssignments, dataLoadProps(
        commander = props.commander,
        query = StatisticsQuery(tribeId),
        toProps = { _, _, queryResult -> TribeStatisticsProps(queryResult, props.pathSetter) }
    ))
}
