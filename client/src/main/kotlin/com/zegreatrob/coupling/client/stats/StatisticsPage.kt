package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import react.RBuilder

private val LoadedPairAssignments by lazy { dataLoadWrapper(TribeStatistics) }
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.render(this)

val StatisticsPage = reactFunction<PageProps> { props ->
    val tribeId = props.tribeId
    if (tribeId != null) {
        loadedPairAssignments(dataLoadProps(
            commander = props.commander,
            query = { StatisticsQuery(tribeId).perform() },
            toProps = { _, _, queryResult -> TribeStatisticsProps(queryResult, props.pathSetter) }
        ))
    } else throw Exception("WHAT")
}
