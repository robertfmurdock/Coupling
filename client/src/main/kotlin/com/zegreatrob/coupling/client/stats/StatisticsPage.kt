package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.client.tribePageFunction
import react.RBuilder

private val LoadedPairAssignments by lazy { dataLoadWrapper(TribeStatistics) }
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.render(this)

val StatisticsPage = tribePageFunction { props, tribeId ->
    loadedPairAssignments(dataLoadProps(
        commander = props.commander,
        query = { StatisticsQuery(tribeId).perform() },
        toProps = { _, _, queryResult -> TribeStatisticsProps(queryResult, props.pathSetter) }
    ))
}
